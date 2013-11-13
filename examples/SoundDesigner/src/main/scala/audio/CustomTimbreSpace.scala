package ui.audio

import org.mt4j.util.MT4jSettings

import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.{Synthesizer, AudioServer}
import org.mt4j.output.audio.AudioServer._

import ui._

/**
* This object realizes a custom timbre space.
*/
object CustomTimbreSpace extends TimbreSpace {

  private val (minFrequency, maxFrequency) = (220f, 440f)
  private val (minVolume, maxVolume) = (0f, 1f) 
  private val (minModFrequency, maxModFrequency) = (20f, 2000f)  
  private val (minModVolume, maxModVolume) = (0f, 100f)
  private val lpfFrequency = 10000
  
  override def synthDefinition = {    
    this.synthDefinition(0f, 0f, 440f, 0.5f)
  }
  
  override def synthDefinition(initX: Float, initY: Float, initPitch: Float, initVolume: Float) = {
    SynthDef("customTimbreSpace") {
      val x = "x".kr(initX)
      val y = "y".kr(initY)
      val parameterizedPitch = "parameterizedPitch".kr(initPitch)
      val parameterizedVolume = "parameterizedVolume".kr(initVolume)
      val pitch = parameterizedPitch * (maxFrequency - minFrequency) + minFrequency
      val volume = parameterizedVolume * (maxVolume - minVolume) + minVolume
      
      val modFrequency = (1f-x) * (maxModFrequency - minModFrequency) + minModFrequency
      val modVolume = x * (maxModVolume - minModVolume) + minModVolume 
      
      val modulationIndex = 1.0f
      
      val mod = SinOsc.ar(modFrequency) * modVolume
    
      val sig1 = SinOsc.ar(pitch + mod*modulationIndex) * volume
      val sig2 = Pulse.ar(pitch + mod*modulationIndex) * volume
      var sig = sig1 * sig2
    
      sig = LPF.ar(sig, y * lpfFrequency)
      sig = FreeVerb.ar(sig, 0.66, (1-y) * 0.8, 0.2)
    
      Out.ar(0, sig)
      Out.ar(1, sig)
      
      //AudioServer attach signal    
    }
  }
  
  override def visualRepresentation = {
    Some(Ui.loadImage(MT4jSettings.getInstance.getDefaultImagesPath + "customTimbreSpace.jpg"))  
  }

  /**
   * Updates the parameters of the synthesizer associated with this timbre space.
   */
  def updateParameters(synth: Synthesizer, x: Float, y: Float, midiNote: Int,  pitch: Float, volume: Float, channels: Array[Int])  = { Int.MaxValue}

  def noteOn(synth: Synth, octave: Int, midiNote: Int, relativePitch: Float) {}

  def noteOff(synth: Synth) {}
}
