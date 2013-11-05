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

import ui.util.Functions


class VPDTimbreSpace extends TimbreSpace {

  private val pentatonic = List(2,2,3,2,3) //e.g. C -> D -> E -> G -> A -> C
  
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

  private val presetBank = new PresetBank("gtm_result_withfreqs_62k_presets_lat22500_rbf100_beta0.200000.csv_extracted.csv", mappingJitter = 0.02f)



  /**
   * Returns the synth definition of this timbre space.
   */
  def synthDefinition: SynthDef = buildSynth(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)

  def buildSynth(cfmp: Float, mfmp: Float, freqp: Float,
                  aetp: Float, cvpyp: Float, mvpyp: Float, cvpxp: Float,
                  mvpxp: Float, cvpywp: Float, mvpywp: Float, cvpxwp: Float,mvpxwp: Float, fmtp: Float,
                fmidxp: Float, nap: Float, fxrtp: Float): SynthDef = SynthDef("VPDTestSynthGated"){
    val clag  = "cLag".kr
    val gr    = Lag.kr("gate".kr, clag)
    val pwidth= Lag.kr("pulseWidth".kr, clag)
    val cfm   = Lag.kr("cleanFmRingmod".kr, clag)
    val mfm   = Lag.kr("modFreqMult".kr, clag)
    val freq  = Lag.kr("frequency".kr, clag)
    val aEt   = Lag.kr("ampEnvType".kr, clag)
    val cvpY  = Lag.kr("carrierVPSYType".kr, clag)
    val mvpY  = Lag.kr("modulatorVPSYType".kr, clag)
    val cvpX  = Lag.kr("carrierVPSXType".kr, clag)
    val mvpX  = Lag.kr("modulatorVPSXType".kr, clag)
    val cvpYW = Lag.kr("carrierVPSYWeight".kr, clag)
    val mvpYW = Lag.kr("modulatorVPSYWeight".kr, clag)
    val cvpXW = Lag.kr("carrierVPSXWeight".kr, clag)
    val mvpXW = Lag.kr("modulatorVPSXWeight".kr, clag)
    val fmT   = Lag.kr("fmModType".kr, clag)
    val fmIdx = Lag.kr("fmModIdx".kr, clag)
    val nA    = Lag.kr("noiseAmount".kr, clag)
    val fxRT  = Lag.kr("fxRouteType".kr, clag)

    val out = VPDSynthGated.ar(gr,
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
    val data = presetBank.getFormattedData(1920,1080) //a sequence of tuples of (x,y) value pairs and associated tuples of the form (ClusterID, Octave, isPercussive).
    val image = new PImage(1920, 1080)
    
    //set background to white
    for(x <- 0 until image.width) {
      for(y <- 0 until image.height) {
        image.set(x,y,0xffffffff)
      }
    }    
    
    //draw points in space
    data.foreach(entry => {
      val x = entry._1._1
      val y = entry._1._2
      val isPercussive = entry._2._3
      val color = this.colorFromData(entry._2)
      val radius = 7
      if (isPercussive) this.drawDiamond(image, x, y, color, 2*radius) else this.drawCircle(image, x, y, color, radius) //diamond if percussive, circle if synth
    })
    Some(image)
  }

  /**
   * Updates the parameters of the synthesizer associated with this timbre space.
   */
  def updateParameters(synth: Synthesizer, x: Float, y: Float, pitch: Float, volume: Float) {
    val xy = convertCoords(x,y)
    val coordsAndParms = presetBank.parameterRelCoordInterp(xy._1,xy._2,8)
    val params = coordsAndParms._2
    
    params.slice(0,15).zipWithIndex.foreach( {
      x =>
      synth.parameters() = (parameterMapping(x._2)._1 -> x._1)
    })    
    
    val octave = params(17).toInt
    
    val relativeNote = this.relativeNoteFromRelativePitch(this.pentatonic, 6, pitch) //at max 3 halftones down and 3 halftones up
    val frequency = (relativeNote+(12*octave)+60).midicps // middle C + octave + offset via keyboard
    synth.parameters() = ("frequency" -> frequency)
    synth.parameters() = ("volume" -> volume)
    
  }
  
  /**
  * Returns for a relative pitch value between 0 and 1 the corresponding relative note in halftones,
  * that is, the number of halftones that is to be added or subtracted.
  * The obtained note, in this regard, lies on the specified scale where 0.5 is the tonic keynote, 
  * and [0,1] is mapped to [-range/2, range/2] both discretely and non-linearly.
  */
  def relativeNoteFromRelativePitch(scale: List[Int], range: Int, pitch: Float) = {
    //0 -> -range/2, 0.5 -> 0, 1 -> range/2
    this.nearestScaleValue(scale, pitch*range - range/2)
  }
  
  /**
  * Returns for a specified musical scale and a continuous value a corresponding scale value.
  * For instance, if a pentatonic scale is given and the continuous value is -3.2, the returned scale value is -3 (or, put in other words, 
  * exactly 3 halftones below the tonic keynote since this note is part of the pentatonic scale).
  */
  def nearestScaleValue(scale: List[Int], value: Float) = {
    var halftones = 0
    var index = 0
    if (value >= scale.head) {
      while (halftones < value) {
        halftones = halftones + scale(index)
        index = (index + 1) % scale.size
      }
      halftones
    }
    else if (math.abs(value) >= scale.last) {
      while (halftones < value) {
        halftones = halftones + scale(scale.size - 1 - index)
        index = (index + 1) % scale.size
      }   
      halftones
    }
    else 0
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
          val imgColor = this.argbToColor(image.get(x,y))
          val r = (imgColor.getR + color.getR)/2
          val g = (imgColor.getG + color.getG)/2
          val b = (imgColor.getB + color.getB)/2
          val a = (imgColor.getAlpha + color.getAlpha)/2
          val argb = this.colorToArgb(new MTColor(r,g,b,a))
          image.set(x,y,argb)
        }
      }
    }
  }
  
  
     /**
  * Draws a diamond on the specified image at the specified coordinates, with the given color and radius.
  */
  private def drawDiamond(image: PImage, xCoord: Int, yCoord: Int, color: MTColor, floatRadius: Float) = {
    val radius: Int = floatRadius.toInt
    for(x <- xCoord - radius to xCoord + radius) {
      for(y <- yCoord - radius to yCoord + radius) {
        val dist = math.abs(x - xCoord) + math.abs(y - yCoord)
        if (dist <= floatRadius) {
          val imgColor = this.argbToColor(image.get(x,y))
          val r = (imgColor.getR + color.getR)/2
          val g = (imgColor.getG + color.getG)/2
          val b = (imgColor.getB + color.getB)/2
          val argb = this.colorToArgb(new MTColor(r,g,b))
          image.set(x,y,argb)
        }
      }
    }    
  }
  
  
  private def colorFromData(data: (Int, Int, Boolean)) = { //data: (ClusterID, Octave, isPercussive)
    val clusters = 99f //too lazy to count clusters algorithmically; 100 seems correct :P
    val octaves = 7f //again determined by manual examination of data
    val h = data._1/clusters
    val s = 0.5f
    val l = (0.8f*(octaves + data._2 - 1)/octaves) + 0.2f //luminance between 0.2 and 1.0 depending on the octave, with higher octaves being lighter
    val (r,g,b) = Functions.hslToRgb(h,s,l)
    /*if (r > 255 || g > 255 || b > 255) {
      println("colorFromData: h: " + h + " s: " + s + " l: " + l)
      println("colorFromData: r: " + r + " g: " + g + " b: " + b)
    }*/
    val a = 50
    new MTColor(r,g,b,a)    
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
