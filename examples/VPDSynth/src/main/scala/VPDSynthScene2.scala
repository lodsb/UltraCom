/*
++1>>  This source code is licensed as GPLv3 if not stated otherwise.
>>  NO responsibility taken for ANY harm, damage done
>>  to you, your data, animals, etc.
>>
+2>>
>>
+3>>
>>  Copyright (c) 2012:
>>
>>     |             |     |
>>     |    ,---.,---|,---.|---.
>>     |    |   ||   |`---.|   |
>>     `---'`---'`---'`---'`---'
>>                    // Niklas Klügel
>>
+4>>
>>  Made in Bavaria by fat little elves - since 1983.
*/


import collection.immutable.IndexedSeq
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.Ellipse
import org.mt4j.components.visibleComponents.widgets.{TextField, Slider, TextArea}
import org.mt4j.util.Color
import org.mt4j.{Scene, Application}
import org.mt4j.types.{Vec3d}
import org.mt4j.components.ComponentImplicits._
import org.lodsb.reakt.Implicits._

import org.mt4j.components.visibleComponents.shapes.{Line, MTLine}

import org.mt4j.output.audio.AudioServer._


import org.lodsb.VPDSynth._
import de.sciss.synth._
import de.sciss.synth.ugen.{MouseY, SinOsc, MouseX, Impulse}
import org.mt4j.output.audio.AudioServer
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.Ops._
import scala.util.Random


object VPDSynthApp2 extends Application {

  def main(args: Array[String]) {


    AudioServer.start(true)

    this.execute(false)

  }


  override def startUp() = {
    this.addScene(new VPDSynthScene2(this, "VPDSynthApp2"))
  }


}


class VPDSynthScene2(app: Application, name: String) extends Scene(app,name) {

  val parameterMapping = Seq[(String, (Float, Float))](
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


  def unwrapParameterString(parms: Array[Float]) : String = {
    val s: String = parms.foldLeft("")((x,y) => x+",\n"+(y+""))

    s
  }

  def buildSynth() : SynthDef = SynthDef("VPDTestSynth"){

      val gr    = "gateRate".kr
      val cfm   = "cleanFmRingmod".kr
      val mfm   = "modFreqMult".kr
      val freq  = "frequency".kr
      val aEt   = "ampEnvType".kr
      val cvpY  = "carrierVPSYType".kr
      val mvpY  = "modulatorVPSYType".kr
      val cvpX  = "carrierVPSXType".kr
      val mvpX  = "modulatorVPSXType".kr
      val cvpYW = "carrierVPSYWeight".kr
      val mvpYW = "modulatorVPSYWeight".kr
      val cvpXW = "carrierVPSXWeight".kr
      val mvpXW = "modulatorVPSXWeight".kr
      val fmT   = "fmModType".kr
      val fmIdx = "fmModIdx".kr
      val nA    = "noiseAmount".kr
      val fxRT  = "fxRouteType".kr

      val out = VPDSynth.ar(Impulse.kr(gr),
        cfm, mfm, freq, aEt, cvpY, mvpY, cvpX, mvpX, cvpYW, mvpYW, cvpXW, mvpXW, fmT, fmIdx, nA, fxRT
      )

      AudioServer attach out
    }


  showTracer(true)

  val presetBank = new PresetBank("gtm_result_preset_extended_extracted.csv", mappingJitter = 0.02f)

  val image = presetBank.generateMappingImage(app)
  image.setPickable(false)
  canvas += image
  //image.setPositionGlobal(Vec3d(0,0))

  val ellipse = Ellipse(10,10)
  ellipse.setPickable(false)
  ellipse.setFillColor(Color.RED)
  canvas += ellipse



  val mySynthParameters = TextArea()
  mySynthParameters.setStrokeColor(Color.RED)
  canvas += mySynthParameters

  val mySynthTracer = TextArea()
  mySynthTracer.setStrokeColor(Color.RED)
  mySynthTracer.text() = "synth"


  val lines = (0 to 20).map({ xIdx =>
    val l = Line()
    l.setPickable(false)
    l.setStrokeColor(Color(150,150,150,50))
    l.endPosition <~ mySynthTracer.globalPosition
    l
  })

  var linesDisplay = Array.fill(lines.size){false}

  canvas += mySynthTracer

  val mySynthDef = buildSynth()
  val mySynth = mySynthDef.play()



  val gateSlider = Slider(0.25f, 2.0f)
  gateSlider.setPositionGlobal(Vec3d(200,200))
  gateSlider.setFillColor(Color(103,20,20))
  canvas() += gateSlider


  val neighborSlider = Slider(1.1f, lines.size.toFloat)
  neighborSlider.setPositionGlobal(Vec3d(200,250))
  neighborSlider.setFillColor(Color(20,20,123))
  canvas() += neighborSlider

  mySynth.parameters <~ gateSlider.value.map({ x => ("gateRate" -> x)})



  mySynthTracer.globalPosition.observe( {
    x =>

    val coordsAndParms = presetBank.parameterAppCoordInterp(x, app, neighborSlider.value().toInt)

    val params = coordsAndParms._2
    val coords = coordsAndParms._1

    val currentDisplayLines = linesDisplay.clone()

    linesDisplay = Array.fill(linesDisplay.size){false}

    mySynthParameters.text() = unwrapParameterString(params)
    val ellipsePos = Vec3d(coords(0)._1, coords(0)._2)

    coords.zipWithIndex.foreach({
      cIdx =>
        val l = lines(cIdx._2)
        val v = Vec3d(cIdx._1._1, cIdx._1._2);

        linesDisplay(cIdx._2) = true;

        l.startPosition() = v

    })

    currentDisplayLines.zipWithIndex.foreach({
      disp =>
        val dispIdx = disp._2
        if(disp._1 != linesDisplay(dispIdx)) {
          if(linesDisplay(dispIdx)) {
            canvas += lines(dispIdx)
          } else {
            canvas -= lines(dispIdx)
          }
        }
    })


    ellipse.setPositionGlobal(ellipsePos)

    params.zipWithIndex.foreach( {
      x =>
      mySynth.parameters() = (parameterMapping(x._2)._1 -> x._1)

    })



    true;

  })

  mySynth.parameters.observe({x => println("Change -> "+x); true})


}







