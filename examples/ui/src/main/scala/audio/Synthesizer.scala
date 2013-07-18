package ui.audio

import org.mt4j.util.Color

import scala.actors._

object Synthesizer extends Actor {
  
  /**
  * The number of output channels.
  */
  val Channels = 4
  
  
  this.start
  
  def act() { 
    while (true) {
      receive {
        case event: AudioEvent => {
          //println("received audio event: "+ event)
        }
      }
    }
  }
  
}
