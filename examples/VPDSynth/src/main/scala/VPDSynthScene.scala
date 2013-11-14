/*
++1>>  This source code is licensed as GPLv3 if not stated otherwise.
>>  NO responsibility taken for ANY harm, damage done
>>  to you, your data, animals, etc.
>>
+2>>
>>
+3>>
>>  Copyright (c) 2012:
>>
>>     |             |     |
>>     |    ,---.,---|,---.|---.
>>     |    |   ||   |`---.|   |
>>     `---'`---'`---'`---'`---'
>>                    // Niklas Klügel
>>
+4>>
>>  Made in Bavaria by fat little elves - since 1983.
*/

import org.mt4j.components.visibleComponents.widgets.{Slider, TextArea}
import org.mt4j.input.midi.{MidiNoteOffMsg, MidiNoteOnMsg, MidiCommunication}
import org.mt4j.{Scene, Application}
import org.mt4j.types.{Vec3d}
import org.mt4j.components.ComponentImplicits._
import org.lodsb.reakt.Implicits._

import org.mt4j.output.audio.AudioServer._


import de.sciss.synth._
import de.sciss.synth.ugen.{MouseY, SinOsc, MouseX, Impulse}
import org.mt4j.output.audio.{Synthesizer, AudioServer}
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.Ops._
import org.lodsb.VPDSynth._


object VPDSynthApp extends Application {

  def main(args: Array[String]) {


    AudioServer.start(true)

    this.execute(false)

  }


  override def startUp() = {
    this.addScene(new VPDSynthScene(this, "VPDSynthApp"))
  }


}


class VPDSynthScene(app: Application, name: String) extends Scene(app,name) {

  def buildSynth(cfmp: Float, mfmp: Float, freqp: Float,
                 aetp: Float, cvpyp: Float, mvpyp: Float, cvpxp: Float,
                 mvpxp: Float, cvpywp: Float, mvpywp: Float, cvpxwp: Float,mvpxwp: Float, fmtp: Float,
                 fmidxp: Float, nap: Float, fxrtp: Float): SynthDef = SynthDef("VPDTestSynthGated"){

    val clag  = "cLag".kr(0.01)
    val gr    = "gate".kr
    val cfm   = Lag.kr("cleanFmRingmod".kr, clag)
    val mfm   = Lag.kr("modFreqMult".kr, clag)
    val freq  = "frequency".kr
    val aEt   = Lag.kr("ampEnvType".kr, clag)
    val cvpY  = Lag.kr("carrierVPSYType".kr, clag)
    val mvpY  = Lag.kr("modulatorVPSYType".kr, clag)
    val cvpX  = Lag.kr("carrierVPSXType".kr, clag)
    val mvpX  = Lag.kr("modulatorVPSXType".kr, clag)
    val cvpYW = Lag.kr("carrierVPSYWeight".kr, clag)
    val mvpYW = Lag.kr("modulatorVPSYWeight".kr, clag)
    val cvpXW = Lag.kr("carrierVPSXWeight".kr, clag)
    val mvpXW = Lag.kr("modulatorVPSXWeight".kr, clag)
    val fmT   = Lag.kr("fmModType".kr, clag)
    val fmIdx = Lag.kr("fmModIdx".kr, clag)
    val nA    = Lag.kr("noiseAmount".kr, clag)
    val fxRT  = Lag.kr("fxRouteType".kr, clag)
    val vol   = "volume".kr(0.8)

    val out = VPDSynthGated.ar(gr,
      cfm, mfm, freq, aEt, cvpY, mvpY, cvpX, mvpX, cvpYW, mvpYW, cvpXW, mvpXW, fmT, fmIdx, nA, fxRT, vol
    )


      AudioServer attach out
    }



  showTracer(true)

  private val parameterMapping = Seq[(String, (Float, Float))](
    "frequency"-> (2.0f,1000.0f),
    "cleanFmRingmod" -> (-0.25f, 1.0f),
    "modFreqMult" -> (0.0f,1.0f),
    "ampEnvType"-> (0.0f,1.0f),
    "carrierVPSYType"-> (0.0f,1.0f),
    "modulatorVPSYType"-> (0.0f,1.0f),
    "carrierVPSXType"-> (0.0f,1.0f),
    "modulatorVPSXType"-> (0.0f,1.0f),
    "carrierVPSYWeight"-> (0.0f,20.0f),
    "modulatorVPSYWeight"-> (0.0f,20.0f),
    "carrierVPSXWeight"-> (0.0f,20.0f),
    "modulatorVPSXWeight"-> (0.0f,20.0f),
    "fmModIdx" -> (0.0f,1000.0f),
    "fmModType"-> (0.0f,1.0f),
    "noiseAmount"-> (0.0f,1.0f),
    "fxRouteType"-> (0.0f,1.0f)
  );


  val midiDeviceName = "BCR2000, USB MIDI, BCR2000"

  var mySynth: Option[Synthesizer] = None

  var currentChannel = 0;
  var currentOctave = 5;


  val midiInput = MidiCommunication.createMidiInputByDeviceIndex(2)
  if(midiInput.isDefined) {

    midiInput.get.receipt.observe( { x => println(x)

    x match {
      case m:MidiNoteOnMsg => noteOn(m.channel,m.note)
      case m:MidiNoteOffMsg =>noteOff(m.channel, m.note)
      case _ => println("Message dropped")
    }

    true;
    })

  }

  def noteOn(midiChan: Int, midiNote: Int) {
    if (mySynth.isDefined) {
      if (midiChan == currentChannel) {

        val frequency = ((12*currentOctave)+( midiNote % 12 ) + 60).midicps // middle C + octave + offset via keyboard

        mySynth.get.parameters() = ("frequency" -> frequency)
        mySynth.get.parameters() = ("gate" -> 1.0)
      }
    }

  }

  def noteOff(midiChan: Int, midiNote: Int) {
    if (mySynth.isDefined) {
      if (midiChan == currentChannel) {
        mySynth.get.parameters() = ("gate" -> 0.0)
      }
    }
  }

  val mySynthDef = buildSynth(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1)


  mySynth = Some(mySynthDef.play())


  mySynth.get.parameters.observe({x => println(x); true})

  val xoffset = 200
  val yoffset = 300

  for (i <- 0 to parameterMapping.size-1) {
    val xcoord: Int = (i % 8) * xoffset + 180
    val ycoord: Int = (i / 8) * yoffset + 250

    val parmName  = parameterMapping(i)._1
    val parmRange = parameterMapping(i)._2

    val text = TextArea()
    text.setPositionGlobal(Vec3d(xcoord, ycoord-200))
    text.setText(parmName)
    text.setPickable(false)

    val slider = Slider(parmRange._1, parmRange._2, 150, 40)
    slider.setPositionGlobal(Vec3d(xcoord, ycoord))

    val info = TextArea()
    info.setPositionGlobal(Vec3d(xcoord, ycoord-100))
    info.text <~ slider.value+""
    info.setPickable(false)

    mySynth.get.parameters <~ slider.value.map({x => parmName -> x})

    slider.value() = (parmRange._1+parmRange._2)/2.0f

    canvas += text ++ slider ++ info

  }

  val octaveSlider = Slider(-5, 8, 150, 50)
  octaveSlider.setPositionGlobal(Vec3d(800, 700))

  octaveSlider.value.observe({x => currentOctave = x.toInt; true})

  val patternSlider = Slider(0, 6, 150, 50)
  patternSlider.setPositionGlobal(Vec3d(800, 800))

  patternSlider.value.observe({x => currentChannel = x.toInt; true})

  canvas += octaveSlider ++ patternSlider


}







