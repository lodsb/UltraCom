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

import org.mt4j.components.visibleComponents.widgets.{Slider, TextArea}
import org.mt4j.{Scene, Application}
import org.mt4j.types.{Vec3d}
import org.mt4j.components.ComponentImplicits._
import org.lodsb.reakt.Implicits._

import org.mt4j.output.audio.AudioServer._


import de.sciss.synth._
import de.sciss.synth.ugen.{MouseY, SinOsc, MouseX, Impulse}
import org.mt4j.output.audio.AudioServer
import de.sciss.synth._
import de.sciss.synth.ugen._
import de.sciss.synth.Ops._


object VPDSynthApp extends Application {

  def main(args: Array[String]) {


    AudioServer.start(true)

    this.execute(false)

  }


  override def startUp() = {
    this.addScene(new VPDSynthScene(this, "VPDSynthApp"))
  }


}


class VPDSynthScene(app: Application, name: String) extends Scene(app,name) {

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

  val parameters = Seq[(String, (Float, Float))](
    "gateRate" -> (0.01f, 2.0f),
    "cleanFmRingmod" -> (-0.25f, 1.0f),
    "modFreqMult" -> (0.0f,1.0f),
    "frequency"-> (2.0f,1000.0f),
    "ampEnvType"-> (0.0f,1.0f),
    "carrierVPSYType"-> (0.0f,1.0f),
    "modulatorVPSYType"-> (0.0f,1.0f),
    "carrierVPSXType"-> (0.0f,1.0f),
    "modulatorVPSXType"-> (0.0f,1.0f),
    "carrierVPSYWeight"-> (0.0f,20.0f),
    "modulatorVPSYWeight"-> (0.0f,20.0f),
    "carrierVPSXWeight"-> (0.0f,20.0f),
    "modulatorVPSXWeight"-> (0.0f,20.0f),
    "fmModType"-> (0.0f,1.0f),
    "fmModIdx" -> (0.0f,1000.0f),
    "noiseAmount"-> (0.0f,1.0f),
    "fxRouteType"-> (0.0f,1.0f)
  );


  val mySynthDef = buildSynth()
  val mySynth = mySynthDef.play()
  mySynth.parameters.observe({x => println(x); true})

  val xoffset = 200
  val yoffset = 300

  for (i <- 0 to parameters.size-1) {
    val xcoord: Int = (i % 8) * xoffset + 180
    val ycoord: Int = (i / 8) * yoffset + 250

    val parmName  = parameters(i)._1
    val parmRange = parameters(i)._2

    val text = TextArea()
    text.setPositionGlobal(Vec3d(xcoord, ycoord-200))
    text.setText(parmName)
    text.setPickable(false)

    val slider = Slider(parmRange._1, parmRange._2, 150, 40)
    slider.setPositionGlobal(Vec3d(xcoord, ycoord))

    val info = TextArea()
    info.setPositionGlobal(Vec3d(xcoord, ycoord-100))
    info.text <~ slider.value+""
    info.setPickable(false)

    mySynth.parameters <~ slider.value.map({x => parmName -> x})

    slider.value() = (parmRange._1+parmRange._2)/2.0f

    canvas += text ++ slider ++ info

  }

}







