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
import java.awt.event.{KeyEvent, KeyListener}
import org.lodsb.reakt.sync.ValS
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

  var currentOctave: Int = 0;
  var isPercussive = false;

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

  def buildSynth() : SynthDef = SynthDef("VPDTestSynthGated"){

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
      val vol   = Lag.kr("volume".kr, clag)

      val out = VPDSynthGated.ar(gr,
        cfm, mfm, freq, aEt, cvpY, mvpY, cvpX, mvpX, cvpYW, mvpYW, cvpXW, mvpXW, fmT, fmIdx, nA, fxRT, vol
      )

      AudioServer attach out
    }


  showTracer(true)

  val presetBank = new PresetBank("gtm_result_withfreqs_62k_presets_lat22500_rbf100_beta0.200000.csv_extracted.csv", mappingJitter = 0.002f)

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
    l.setStrokeColor(Color(150,150,150,150))
    l.endPosition <~ mySynthTracer.globalPosition
    l
  })

  var linesDisplay = Array.fill(lines.size){false}

  canvas += mySynthTracer

  val mySynthDef = buildSynth()
  val mySynth = mySynthDef.play()

  /*
  val gateSlider = Slider(0.25f, 2.0f)
  gateSlider.setPositionGlobal(Vec3d(200,200))
  gateSlider.setFillColor(Color(103,20,20))
  canvas() += gateSlider

*/
  val neighborSlider = Slider(1.1f, lines.size.toFloat)
  neighborSlider.setPositionGlobal(Vec3d(200,250))
  neighborSlider.setFillColor(Color(20,20,123))
  canvas() += neighborSlider

  /*
  val pwSlider = Slider(0.01f, 1.0f)
  pwSlider.setPositionGlobal(Vec3d(200,300))
  pwSlider.setFillColor(Color(0,123,20))
  canvas() += pwSlider
   */
  val lagSlider = Slider(0.01f, 1.0f)
  lagSlider.setPositionGlobal(Vec3d(200,300))
  lagSlider.setFillColor(Color(0,123,20))
  canvas() += lagSlider

  /*
  mySynth.parameters <~ gateSlider.value.map({ x => ("gateRate" -> x)})
  mySynth.parameters <~ pwSlider.value.map({ x => ("pulseWidth" -> x)})
  */
  mySynth.parameters <~ lagSlider.value.map({ x => ("cLag" -> x)})



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

    params.slice(0,16).zipWithIndex.foreach( {
      x =>
      mySynth.parameters() = (parameterMapping(x._2)._1 -> x._1)

    })


    currentOctave = params(17).toInt
    println(currentOctave+" CURRENT OCTAVE")
    isPercussive = params(18).toInt == 1
    println(isPercussive+" IS PERCUSSIVE")

    true;

  })

  mySynth.parameters.observe({x => println("Change -> "+x); true})


  val keysUp = new ValS[KeyEvent](null)
  val keysDown = new ValS[KeyEvent](null)

  this.app.addKeyListener( new KeyListener {
    def keyTyped(p1: KeyEvent) {
    }

    def keyPressed(p1: KeyEvent) {
      keysDown.emit(p1)
      println("KEY PRESSED"+p1.getKeyCode)
      println("KEY PRESSED"+p1.getKeyChar)
    }

    def keyReleased(p1: KeyEvent) {
      p1.getKeyCode
      keysUp.emit(p1)
    }
  });

  val note2FreqMap = Map[Char, Float](
    'y' -> 1,
    's' -> 2,
    'x' -> 3,
    'd' -> 4,
    'c' -> 5,
    'v' -> 6,
    'b' -> 7,
    'h' -> 8,
    'n' -> 9,
    'j' -> 10,
    'm' -> 11,
    'k' -> 12,
    ',' -> 13
  )

  //var octavemult = 1;

  keysDown.observe {x =>
    /*
    if(x.getKeyChar == '-')
          {octavemult = octavemult - 1}
    else if (x.getKeyChar == '+')
          {octavemult = octavemult + 1};

    */
    val m = note2FreqMap.get(x.getKeyChar);
    if(!m.isEmpty && !isPercussive) { // transpose only if the sound is not percussive
      val frequency = (m.get+(12*currentOctave)+60).midicps // middle C + octave + offset via keyboard

      mySynth.parameters() = ("frequency" -> frequency)
	mySynth.parameters() = ("volume" -> 1.0)
    }


    true}


  mySynth.parameters <~ keysDown.map({ x => ("gate" -> 1.0)})
  mySynth.parameters <~ keysUp.map({ x => ("gate" -> 0.0)})





}







