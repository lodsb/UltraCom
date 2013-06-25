package ui.audio

import scala.actors._

object Synthesizer extends Actor {
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
