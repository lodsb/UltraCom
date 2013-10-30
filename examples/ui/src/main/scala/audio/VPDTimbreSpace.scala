package ui.audio

import ui.audio.TimbreSpace
import de.sciss.synth.SynthDef
import processing.core.PImage
import org.lodsb.VPDSynth._
import de.sciss.synth.ugen.Impulse
import org.mt4j.output.audio.AudioServer._
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.Ops._
import org.mt4j.output.audio.{Synthesizer, AudioServer}
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.Ops._

import org.mt4j.util.MTColor


class VPDTimbreSpace extends TimbreSpace {

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

  private val presetBank = new PresetBank("gtm_result_preset_clustered.csv", mappingJitter = 0.02f)



  /**
   * Returns the synth definition of this timbre space.
   */
  def synthDefinition: SynthDef = buildSynth(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)

  def buildSynth(cfmp: Float, mfmp: Float, freqp: Float,
                  aetp: Float, cvpyp: Float, mvpyp: Float, cvpxp: Float,
                  mvpxp: Float, cvpywp: Float, mvpywp: Float, cvpxwp: Float,mvpxwp: Float, fmtp: Float,
                fmidxp: Float, nap: Float, fxrtp: Float): SynthDef = SynthDef("VPDTimbreSynth"){

        val gr    = "gateRate".kr(1.0)
        val cfm   = "cleanFmRingmod".kr(cfmp)
        val mfm   = "modFreqMult".kr(mfmp)
        val freq  = "frequency".kr(freqp)
        val aEt   = "ampEnvType".kr(aetp)
        val cvpY  = "carrierVPSYType".kr(cvpyp)
        val mvpY  = "modulatorVPSYType".kr(mvpyp)
        val cvpX  = "carrierVPSXType".kr(cvpxp)
        val mvpX  = "modulatorVPSXType".kr(mvpxp)
        val cvpYW = "carrierVPSYWeight".kr(cvpywp)
        val mvpYW = "modulatorVPSYWeight".kr(mvpywp)
        val cvpXW = "carrierVPSXWeight".kr(cvpxwp)
        val mvpXW = "modulatorVPSXWeight".kr(mvpxwp)
        val fmT   = "fmModType".kr(fmtp)
        val fmIdx = "fmModIdx".kr(fmidxp)
        val nA    = "noiseAmount".kr(nap)
        val fxRT  = "fxRouteType".kr(fxrtp)

        val out = VPDSynth.ar(Impulse.kr(gr),
          cfm, mfm, freq, aEt, cvpY, mvpY, cvpX, mvpX, cvpYW, mvpYW, cvpXW, mvpXW, fmT, fmIdx, nA, fxRT
        )

        AudioServer attach out
      }


  private def convertCoords(x: Float, y:Float) : (Float, Float) = {
    ((x-0.5f)*2f, (y-0.5f)*2f)
  }

  /**
   * Returns the synth definition of this timbre space with the given parameter values as default.
   */
  def synthDefinition(x: Float, y: Float, pitch: Float, volume: Float): SynthDef = {
    val mySynthDef = synthDefinition

    val xy = convertCoords(x,y)

    val coordsAndParms = presetBank.parameterRelCoordInterp(xy._1,xy._2, 2); // two nearest neighbors for interpolation
    val p = coordsAndParms._2

    val sd = buildSynth(p(1), p(2), p(0), p(3), p(4), p(5), p(6), p(7), p(8), p(9),p(10), p(11),p(13),p(12),p(14),p(15));

    sd

  }

  /**
   * Returns - as an Option - a two-dimensional visual representation of this timbre space, or None if it is not defined.
   */
  def visualRepresentation: Option[PImage] = {
    val image = presetBank.generateMappingPImage(1920,1080,0xffffffff)
    val newImage = new PImage(image.width, image.height)
    
    //set background to white
    for(x <- 0 until newImage.width) {
      for(y <- 0 until newImage.height) {
        newImage.set(x,y,0xffffffff)
      }
    }    
    
    //draw points in space
    for(x <- 0 until newImage.width) {
      for(y <- 0 until newImage.height) {
        val argb = image.get(x,y)
        if (argb != 0xffffffff) {
          val color = this.argbToColor(argb)
          val radius = 7
          this.drawCircle(newImage, x, y, color, radius)
        }
      }
    }
    Some(newImage)
  }

  /**
   * Updates the parameters of the synthesizer associated with this timbre space.
   */
  def updateParameters(synth: Synthesizer, x: Float, y: Float, pitch: Float, volume: Float) {
    val xy = convertCoords(x,y)

    val coordsAndParms = presetBank.parameterRelCoordInterp(xy._1,xy._2,8);

       val params = coordsAndParms._2

    params.zipWithIndex.foreach( {
      x =>
      synth.parameters() = (parameterMapping(x._2)._1 -> x._1)

    })
  }
  
  /**
  * Draws a circle on the specified image at the specified coordinates, with the given color and radius.
  */
  private def drawCircle(image: PImage, xCoord: Int, yCoord: Int, color: MTColor, radius: Int) = {
    for(x <- xCoord - radius to xCoord + radius) {
      for(y <- yCoord - radius to yCoord + radius) {
        val xDiff = x - xCoord
        val yDiff = y - yCoord
        if (xDiff*xDiff + yDiff*yDiff <= radius*radius) {
          val argb = this.colorToArgb(color)
          image.set(x,y,argb)
        }
      }
    }    
    image.updatePixels()
  }
  
  private def argbToColor(argb: Int): MTColor = {
    val a = (argb >> 24) & 0xFF
    val r = (argb >> 16) & 0xFF
    val g = (argb >> 8) & 0xFF
    val b = argb & 0xFF
    new MTColor(r,g,b,a)
  }  

  
  private def colorToArgb(color: MTColor): Int = {
    (color.getAlpha.toInt << 24) | (color.getR.toInt << 16) | (color.getG.toInt << 8) | color.getB.toInt
  }    
  
}
