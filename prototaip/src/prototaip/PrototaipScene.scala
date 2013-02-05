/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 10 - 16 :: 10 : 16
    >>  Origin: mt4j (project) / prototaip (module)
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

package prototaip

import org.mt4j.MTApplication
import org.mt4j.sceneManagement.SimpleAbstractScene
import org.mt4j.util.MTColor
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.components.visibleComponents.widgets._
import org.lodsb.reaktExt.animation.{SineInOut, InterpolatingAnimation}
import org.mt4j.input.midi.MidiCommunication
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vector3D
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.util.MTColor

import java.net.InetSocketAddress
import org.mt4j.components.ComponentImplicits._

import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt
import org.mt4j.MTApplication
import org.mt4j.sceneManagement.SimpleAbstractScene
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer
import org.mt4j.stateMachine.StateMachine
import org.mt4j.eventSystem.{foo, bar, TrEventListener}
import org.mt4j.commandSystem.Command


import org.mt4j.input.midi.{MidiCommunication, MidiCtrlMsg}
import org.mt4j.input.kinect.KinectSkeletonSource
import org.mt4j.components.visibleComponents.widgets._

import org.lodsb.reakt.ConstantSignal._
import org.lodsb.reaktExt.animation._
import org.mt4j.output.audio.AudioServer
import de.sciss.synth.SynthDef
import de.sciss.synth.ugen._
import AudioServer._
import org.mt4j.types.{Rotation}
import org.mt4j.input.osc.OSCCommunication
import scala.util.Random

class PrototaipScene(mtApplication: MTApplication, name: String)
	extends SimpleAbstractScene(mtApplication, name) {

	//Show touches
	var tracer = new CursorTracer(this, mtApplication);

	/* rev
	val dataset = TimbreDataSetLoader.load("/home/lodsb/testsettings.csv")
	val cp = new TimbreSurface(	mtApplication, dataset, 0, 0,
								mtApplication.getSize.getWidth.toInt,
								mtApplication.getSize.getHeight.toInt)
	canvas += cp;
	*/



	var textField = new MTTextArea(Prototaip);
	textField.setNoFill(true);
	textField.setPositionGlobal(new Vector3D(mtApplication.width / 2f, mtApplication.height / 2f));

	/* rev
	textField.text <~ cp.soundPicked +""
	*/

	//Add the textfield to our canvas
	canvas += textField;

	var rand = new Random();

	/* rev
	AudioServer.start(() => {


		val ws = WaveSynth(dataset._1);
		val wsSynth = ws.play

		wsSynth.parameters <~ cp.soundPicked.map( x => "pos" -> x._1.toFloat)  // just use the position data for now
		wsSynth.parameters <~ cp.soundPicked.map( x => "trig" -> rand.nextFloat())
	})
	*/

	AudioServer.start(() => {
		var midiIn = MidiCommunication.createMidiInput("BCR2000, USB MIDI, BCR2000")
		val test = AudioServer.tt2()
		val synth = test.play




		midiIn match {
			case Some(midictrl) =>
				midictrl.receipt.observe {
					m => m match {
						case MidiCtrlMsg(chan, num, v) => {
							println((num,v));
							val value: Float = (v.toFloat)
							//sl.value.map({x => println(x);("amp"->x.toFloat)2}
							num match {
								case 73 => {
									synth.parameters() = ("fmod" -> 0.98*500);
									synth.parameters() = ("fcar" -> 0.039*1000);
									synth.parameters() = ("idx" -> 0.76*2000);
									synth.parameters() = ("attack" -> 0.1*0.09);
									synth.parameters() = ("decay" -> 0.81);
									synth.parameters() = ("operator" -> 0.03);
									synth.parameters() = ("speed" -> 10*0.04);

									synth.parameters() = ("fmod2" -> 0.98*1000);
									synth.parameters() = ("fcar2" -> 0.24*1000);
									synth.parameters() = ("idx2" -> 0.27*2000);
									synth.parameters() = ("attack2" -> 0.1*0.03);
									synth.parameters() = ("decay2" -> 0.99);
									synth.parameters() = ("operator2" -> 0.023);
									synth.parameters() = ("speed2" -> 10*0.33);



								}
								case 1 => synth.parameters() = ("fmod" -> value*500);         // 1
								case 2 => synth.parameters() = ("fcar" -> value*1000);         // 2
								case 3 => synth.parameters() = ("idx" -> value*2000);          // 3
								case 4 => synth.parameters() = ("attack" -> value*0.1);        // 4
								case 5 => synth.parameters() = ("decay" -> value);             // 5
								case 6 => synth.parameters() = ("operator" -> value);          // 6
								case 7 => synth.parameters() = ("speed" -> value*10);          // 7
									                                                            //
								case 81 => synth.parameters() = ("fmod2" -> value*1000);        // 81
								case 82 => synth.parameters() = ("fcar2" -> value*1000);        // 82
								case 83 => synth.parameters() = ("idx2" -> value*2000);         // 83
								case 84 => synth.parameters() = ("attack2" -> value*0.1);       // 84
								case 85 => synth.parameters() = ("decay2" -> value);            // 85
								case 86 => synth.parameters() = ("operator2" -> value);         // 86
								case 87 => synth.parameters() = ("speed2" -> value*10);         // 87

								case 97 => synth.parameters() = ("filtertype" -> value);         // 97
								case 98 => synth.parameters() = ("filterfreq" -> 10000*value);         // 97
								case 99 => synth.parameters() = ("fdecay" -> value);         // 83
								case 100 => synth.parameters() = ("fattack" -> value*0.1);       // 84
								case 101 => synth.parameters() = ("filtermod" -> value);       // 84



								case _ => Unit
							}
							textField.fillColor() = new MTColor(value, value, value)
						}
					}
					true
				}

			case _ => println("NO DEVICE FOUND!")
		}
	})
}

object Prototaip extends MTApplication {

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		System.out.println("SADSDFSDF");
		val fsm = new PrototaipScene(this, "FPPPP");
		this.addScene(fsm)
	}

}
