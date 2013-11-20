package ui.audio

import org.mt4j.util.Color

import scala.actors._
import org.mt4j.input.midi.{MidiNoteOffMsg, MidiNoteOnMsg, MidiCtrlMsg, MidiCommunication}

//import ugen._
import org.mt4j.output.audio.AudioServer
import org.mt4j.output.audio.AudioServer._
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.Ops._


object AudioInterface {
  
  def apply(timbreSpace: TimbreSpace) = {
    new AudioInterface(timbreSpace)
  }
  
}

/**
* This class represents an audio interface.
* An audio interface is associated with a timbre space and may receive audio events.
* These events are then processed and may trigger actions on the actual audio server.
* 
*/
class AudioInterface(val timbreSpace: TimbreSpace) extends Actor {

  //////////
  // MIDI handling
  /////////


  abstract class UINoteEvent

  case class NoteOn(midiChannel: Int, note: Int, velocity: Float) extends UINoteEvent
  case class NoteOff(midiChannel: Int, note: Int) extends UINoteEvent

  /**
   * midi device name
   */
  val midiDeviceName = "BCR2000, USB MIDI, BCR2000"


  val midiInput = MidiCommunication.createMidiInputByDeviceIndex(1)
  if(midiInput.isDefined) {

    midiInput.get.receipt.observe( { x => println(x)

      x match {
        case m:MidiNoteOnMsg => this ! NoteOn(m.channel,m.note,m.velocity)
        case m:MidiNoteOffMsg => this ! NoteOff(m.channel, m.note)
        case _ => println("Message dropped")
      }

      true;
    })

  }

  val midiOutput = MidiCommunication.createMidiOutputByDeviceIndex(4)
  

  // controller id should be larger than 20 and < 40
  def sendControlMessage(controllerChannel: Int, controllerNumber: Int, controllerValue: Float) : Unit = {
    println("called send control")
    if(this.midiOutput.isDefined) {
      print(">>")
      this.midiOutput.foreach(output => {

        val ctrlMsg = MidiCtrlMsg(controllerChannel, controllerNumber, controllerValue)
        println(ctrlMsg)
        output.senderAction(ctrlMsg ) //MidiCtrlMsg(channel: Int, num: Int, data: Float)
      })
    }
  }

  /*
  val myThread = new Thread(new Runnable {
    def run() {
      while (true) {
       Thread.sleep(300)
       AudioInterface.this ! NoteOn(1, 66, 127)
       Thread.sleep(500)
       AudioInterface.this ! NoteOff(1, 66)
      }
    }
  })

  myThread.start()
  */







  case class SynthInfo(synth: Synth, midiChan: Int, octave: Int, currentPitch: Int,  relativePitch: Float)

  
  /**
  * The number of output channels.
  */
  val Channels = 4
  
  /**
  * Maps caller ids to synths.
  */
  var synthMap = Map[Int, SynthInfo]()
  
  this.start
  
  def play(event: PlayAudioEvent) = {
    this.synchronized {
      //println("in play")
      if (this.synthMap.contains(event.callerID)) {
        //println("updating synth with caller id " + event.callerID)
        val synthInfo = this.synthMap(event.callerID)

        val synth = synthInfo.synth

        val synthOctave = this.updateParameters(synthInfo, event)

        //TODO: also update current frequency @ midihandling
        val updatedInfo = SynthInfo(synthInfo.synth, event.midiChan, synthOctave, synthInfo.currentPitch,
                                    event.pitch)

        println(updatedInfo)

        this.synthMap += (event.callerID -> updatedInfo)

        //synth.parameters() = ("volume" -> 1)

      }
      else {
        val synthDef = this.timbreSpace.synthDefinition(event.x, event.y, event.pitch, event.volume)
        println("starting synth with caller id " + event)

        val synth = synthDef.play()
        val synthInfo = SynthInfo(synth, 0, 0, 60, event.pitch)

        this.synthMap += (event.callerID -> synthInfo)

        //synth.parameters.observe{x => println(x); true}
        Thread.sleep(50)
        /* 
        the sleep call is actually a workaround for cases where play and stop are called in rapid succession;
        apparently, if a synth is freed too quickly after it is instantiated, Super Collider does not have a Node
        for it yet and thus the free call fails; since the synth is nonetheless removed from the synth map of this class, 
        there is then no way to stop the synth again, which is highly undesirable behaviour
        */
      }    
      //println(this.synthMap.toString)
   }
  }
  
  def pause(event: PauseAudioEvent) = {
    this.synchronized {
      if (this.synthMap.contains(event.callerID)) {
        val synth = this.synthMap(event.callerID).synth
        synth.parameters() = ("volume" -> 0)
      }    
      else {println("synth does not exist")}
    }    
  }
  
  def resume(event: ResumeAudioEvent) = {
    this.synchronized {
      if (this.synthMap.contains(event.callerID)) {
        val synth = this.synthMap(event.callerID).synth
        synth.parameters() = ("volume" -> 1)
      }    
      else {println("synth does not exist")}
    }    
  }  
  
  def stop(event: StopAudioEvent) = {
    this.synchronized {
      if (this.synthMap.contains(event.callerID)) {
        val synth = this.synthMap(event.callerID).synth
        this.synthMap -= event.callerID
        synth.free 
      }    
      else {println("synth does not exist")}
    }
  }
  
  
  def act() { 
    while (true) {
      receive {
        //println("received audio event: "+ event)
        case event: PlayAudioEvent => {
          this.play(event)
        }
        case event: StopAudioEvent => {
          this.stop(event)
        }
        case event: PauseAudioEvent => {
          this.pause(event)
        }
        case event: ResumeAudioEvent => {
          this.resume(event)
        }
        case event: MIDIControlEvent => {
          this.sendControlMessage(event.controllerChannel, event.controllerNumber, event.controllerValue)
        }
        case NoteOn(ch, note, velocity) => {
          this.noteOn(ch, note)
        }
        case NoteOff(ch, note) => {
          this.noteOff(ch, note)
        }

        case otherEvent => {}
      }
    }
  }
  
  
  def updateParameters(synthInfo: SynthInfo, event: PlayAudioEvent) : Int= {
    this.timbreSpace.updateParameters(synthInfo.synth, event.x, event.y, synthInfo.currentPitch, event.pitch, event.volume, event.channels)
  }

  def noteOn(midiChan: Int, midiNote: Int) {
    print(".")

    var list = List[(Int, SynthInfo)]()

    synthMap.foreach({ kv =>
      val x = kv._2
      if(midiChan == x.midiChan) {
        this.timbreSpace.noteOn(x.synth, x.octave, midiNote, x.relativePitch)

        list = list :+ (kv._1, SynthInfo(x.synth, x.midiChan, x.octave, midiNote, x.relativePitch))

      }
    })

    list.foreach({x =>
      synthMap = synthMap + x
    })

  }

  /*
  def noteOffSynth(x: Synth) {
    this.timbreSpace.noteOff(x)
  }
  */

  def noteOff(midiChan: Int, midiNote: Int) {
    print("-")
    synthMap.values.foreach({ x =>
      if(midiChan == x.midiChan) {
        this.timbreSpace.noteOff(x.synth)
      }
    })
  }


}
