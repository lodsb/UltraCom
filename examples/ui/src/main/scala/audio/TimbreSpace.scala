package ui.audio

import processing.core.PImage
import de.sciss.synth._

/**
* This abstract class represents a timbre space.
* 
*/
abstract class TimbreSpace {

  /**
  * Returns the synth definition of this timbre space.
  */
  def synthDefinition: SynthDef
  

  /**
  * Returns the synth definition of this timbre space with the given parameter values as default.
  */
  def synthDefinition(x: Float, y: Float, pitch: Float, volume: Float): SynthDef  
 
  /**
  * Returns - as an Option - a two-dimensional visual representation of this timbre space, or None if it is not defined.
  */
  def visualRepresentation: Option[PImage]
  
}
