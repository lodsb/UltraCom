/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 30.10.13
 * Time: 10:14
 */


package de.ghagerer.FugueGenerator

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import java.lang.Thread
import org.lodsb.scales.Conversions._
import org.lodsb.scales.Transformations._
import org.lodsb.scales._
import scala.collection.mutable.ArrayBuffer
import org.lodsb.reakt.sync.VarS
import org.mt4j.components.visibleComponents.widgets.MTSlider


// wrap the unique metronome-thread, which is an akka.Actor, within this object
object Metronome {
  private val system = ActorSystem("MetronomeSystem")
  private val metronome = system.actorOf(Props[Metronome], name = "metronome")
  def apply(): ActorRef = metronome
  def totalSteps = counter.totalSteps

  // how much time between 16th steps
  val duration = new VarS[Float](100f)

}


// This is the Metronome Thread
class Metronome extends Actor {

  // with this variable the Metronome can be stopped
  private var running = false

  // this is the metronome thread/actor actually
  def receive = {
    case "start" =>
      running = true
      startLoopIteration
    case "stop" =>
      running = false
    case _ =>
  }

  // here takes place the iteration through all controllers for each beat
  def startLoopIteration {
    while (running) {
      counter.increment
      if(counter() == 1) {
        Harmony.nextHarmony
      }
      app.allControllerCanvas.foreach( controllerCanvas =>
        controllerCanvas.playNext(counter())
      )
      Thread.sleep(Metronome.duration().toInt)
    }
  }
}

// the counter for the sixteenth out of ++ coolness....
object counter {

  // 0 means inactive, 1 to 16 are the possible steps
  private var counter = 0

  val totalSteps = 32

  def increment {
    counter = if(counter==totalSteps) 1 else counter+1
  }

  def apply(): Int = {
    counter
  }

}

object Harmony {
  private var harmonies = new ArrayBuffer[Pitch]()
  harmonies += Tonic
  harmonies += Tonic
  harmonies += Subdominant
  harmonies += Dominant

  private var _activeHarmony = 0
  def activeHarmony: Pitch = {
    _activeHarmony %= harmonies.size
    harmonies(_activeHarmony)
  }
  def nextHarmony {
    _activeHarmony += 1
    if(_activeHarmony == harmonies.size) {
      _activeHarmony = 0
    }
  }

  def activeChord: Chord[TunedPitch] = {
    tune(scale(Triad(Harmony.activeHarmony),Scales.activeScale),Scales.tuning)
  }

  /**
   * 0 - low compexity
   * 1 - high complexity
   * @param value
   */
  def progressionComplexity(value: Float) {
    var numberOfHarmonies = math.round(value * 7f)
    harmonies = new ArrayBuffer[Pitch]()
    while (0 <= numberOfHarmonies) {
      harmonies += Pitch((numberOfHarmonies*4)%7)
      numberOfHarmonies -= 1
    }
    harmonies.size match {
      case 3 => harmonies += Pitch(0)
      case 5 => harmonies += Pitch(0);harmonies += Pitch(0);harmonies += Pitch(0);
      case 7 => harmonies += Pitch(0)
      case _ =>
    }
  }

  /**
   * 0 - low compexity
   * 1 - high complexity
   * @param value
   */
  def tonalComplexity(value: Float) {

  }

}

object Scales {
  val scales = new ArrayBuffer[Scale]()
  scales += Scale("Phrygian").get
  scales += Scale("Minor").get
  scales += Scale("Dorian").get
  scales += Scale("Mixolydian").get
  scales += Scale("Major").get
  scales += Scale("Lydian").get

  val tuning = Tuning("Equal").get

  private var _activeScale = 0
  def activeScale = scales(_activeScale)
  def activeScale(value: Int) {
    if(0 <= value && value < scales.size) {
      _activeScale = value
    }
  }
  def size = scales.size
}