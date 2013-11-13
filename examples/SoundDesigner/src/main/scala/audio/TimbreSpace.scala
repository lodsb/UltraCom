package ui.audio

import processing.core.PImage
import de.sciss.synth._
import org.mt4j.output.audio.Synthesizer

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
   * Updates the parameters of the synthesizer associated with this timbre space.
   * Return Octave
   */
  def updateParameters(synth: Synthesizer, x: Float, y: Float, midiNote: Int,  pitch: Float, volume: Float, channels: Array[Int]) : Int
 
  /**
  * Returns - as an Option - a two-dimensional visual representation of this timbre space, or None if it is not defined.
  */
  def visualRepresentation: Option[PImage]

  def noteOn(synth: Synth, octave: Int, note: Int, relativePitch: Float)

  def noteOff(synth: Synth)
  
}
