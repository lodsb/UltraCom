package ui.audio

import org.mt4j.util.Color

import scala.actors._


object SpatialSynthesizer {
  
  def apply(timbreSpace: TimbreSpace) = {
    new SpatialSynthesizer(timbreSpace)
  }
  
}

/**
* This class realizes a spatial synthesizer.
* A spatial synthesizer is associated with a timbre space and allows for playback of timbres of said space.
* 
*/
class SpatialSynthesizer(val timbreSpace: TimbreSpace) extends Actor {
  
  /**
  * The number of output channels.
  */
  val Channels = 4
  
  this.start
  
  def act() { 
    while (true) {
      receive {
        case event: AudioEvent => {
          this.timbreSpace.process(event)
          //println("received audio event: "+ event)
        }
      }
    }
  }
  
}
