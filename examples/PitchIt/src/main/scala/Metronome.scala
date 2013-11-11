/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 30.10.13
 * Time: 10:14
 */


package PitchIt

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import java.lang.Thread


// wrap the unique metronome-thread, which is an akka.Actor, within this object
object Metronome {
  private val system = ActorSystem("MetronomeSystem")
  private val metronome = system.actorOf(Props[Metronome], name = "metronome")
  def apply(): ActorRef = metronome
  def totalSteps = counter.totalSteps
}


// This is the Metronome Thread
class Metronome extends Actor {

  // with this variable the Metronome can be stopped
  private var running = false

  // how much time between 16th steps
  private var duration = 100

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
      counter++;
      app.allControllerCanvas.foreach( controllerCanvas =>
        Sound().play(controllerCanvas.setStep(counter()))
      )
      Thread.sleep(duration)
    }
  }
}

// the counter for the sixteenth out of ++ coolness....
object counter {

  // 0 means inactive, 1 to 16 are the possible steps
  private var counter = 0

  val totalSteps = 16

  def ++ {
    counter = if(counter==totalSteps) 1 else counter+1
  }

  def apply(): Int = counter

}