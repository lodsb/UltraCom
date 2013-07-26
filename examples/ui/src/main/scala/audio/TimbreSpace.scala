package ui.audio

import processing.core.PImage

/**
* This abstract class represents a timbre space.
* A timbre space is a spatial structure whose coordinates are each associated with a timbre.
* A timbre may be played back by calling the play method with an AudioEvent.
* 
*/
abstract class TimbreSpace {

  /**
  * Processes the specified audio event.
  */
  def process(event: AudioEvent)
 
  /**
  * Returns - as an Option - a two-dimensional visualization of this timbre space, or None if there is none.
  */
  def visualization: Option[PImage]
  
}
