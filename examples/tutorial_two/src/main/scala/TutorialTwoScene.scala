/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 3 - 19 :: 0 : 54
    >>  Origin: mt4j (project) / UltraCom (module)
    >>
  +3>>
    >>  Copyright (c) 2013:
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

package TutorialTwo

import org.mt4j.{Scene, Application}
import de.sciss.synth._

import ugen._
import org.mt4j.output.audio.AudioServer
import org.mt4j.output.audio.AudioServer._
import org.mt4j.components.visibleComponents.widgets.{Scope, Slider}
import org.mt4j.components.visibleComponents.shapes.Ellipse
import org.mt4j.types.Vec3d
import de.sciss.synth._
import de.sciss.synth.Ops._ 
import org.mt4j.components.ComponentImplicits._
import org.mt4j.util.Color


object TutorialTwo extends Application {
	/*
			Settings, such as <b>the application name<b>, display properties, etc are set in Settings.txt
	 */

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		val scene = new TutorialTwoScene(this, "Scene of Tutorial One");
		this.addScene(scene)
	}

}


class TutorialTwoScene(app: Application, name: String) extends Scene(app,name) {

	// Show touches
	showTracer(true)

	// create a _definition_ of simple fm synth
	// this definition can then be used to create
	// actual instances of a synth
	val mySynthDef = SynthDef("mySynth") {
		// simple synthesizer parameters, oscillator frequency, modulator frequency, coupling
		// these can be lateron controlled, .kr for controlrate
    val array = Array[String]("oscfreq")
		val oscFreq = array(0).kr(440)
		val modFreq = "modfreq".kr(440)
		val coupling= "coupling".kr(0.0)

		// use a repeating envelope to create a rhythmic effect
		// the impulse (2Hz = 120 BPM) triggers the envelope
		val imp = Impulse.kr(2);
		val envelope = EnvGen.ar(Env.perc(0.1, 0.6, 1.0, Curve.parametric(-4)),imp );

		// the modulator has a fixed frequency, the amplitude of the modulator is scaled
		// by the envelope and the amount of coupling
		val modulation = SinOsc.ar(modFreq)*coupling*envelope


		val signal = SinOsc.ar(oscFreq + modulation)

		// this function is called to make sure the produced audio signal lands
		// on the supercollider output, it also ensures, that the audio signal is
		// fed back to UltraCom (low sampling rate = 10Hz) for UI updates!
		//
		// if you leave it out, you can still use the synth, just make sure you
		// put the output on an audio bus; you won't get the audio feedback to
		// UltraCom though!
		AudioServer attach signal
	}


	// graphical controls
	val couplingSlider = Slider(0,1000.0f)
	val freqmodEllipse = Ellipse(25f,25f)
	freqmodEllipse.fillColor() = Color(255,0,0)

	// ui feedback
	val scope = Scope(10,100,600,150)


	canvas += couplingSlider++freqmodEllipse++scope

	// The audio is running in a different process, so before we
	// can use the audio server we have to wait until it booted.
	// The function given as startargument is executed once the server did this.
	AudioServer.start({
		//create an actual instance of the synthesizer defined before
		val mySynth = mySynthDef.play()

		// to update the parameters of a synth, send tuples
		// these muse be formated as ("parametername", value); the shorthand writing is "parametername"->value
		mySynth.parameters <~ couplingSlider.value.map { x => ( "oscfreq" -> x ) }
		mySynth.parameters.observe({x => println(x); true})

		freqmodEllipse.globalPosition.observe {
			pos =>{ mySynth.parameters() = ("modfreq" -> pos.x);
					mySynth.parameters() = ("coupling" -> 5*pos.y);
					true }
		}


		// plot the output
		scope.plot <~ mySynth.amplitude

		// faster update, to see more of the waveform
		mySynth.setAmplitudeUpdateDivisions(1)
	})











}
