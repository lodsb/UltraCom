package basic.helloWorld

//import de.sciss.synth._
//import ugen._

import org.mt4j.util.math.Vector3D
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.util.MTColor

import java.net.InetSocketAddress

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

class HelloWorldScene2(mtApplication: MTApplication, name: String)
	extends SimpleAbstractScene(mtApplication, name)

	with TrEventListener[MTOSCControllerInputEvt]
	with StateMachine {

	val clazz = this.getClass
	var white = new MTColor(255, 255, 255);
	this.setClearColor(MTColor.BLACK);

	//Show touches
	var tracer = new CursorTracer(this, mtApplication);

	//var proc = new GlobalOSCInputProcessor(this,"/4/xy");

	//this.registerGlobalInputProcessor(proc);

	var fontArial = FontManager.getInstance().createFont(mtApplication, "arial",
		150, //Font size
		white, //Font fill color
		white);
	//Font outline color
	//Create a textfield
	var textField = new MTTextArea(mtApplication); //, fontArial);

	//textField.setNoStroke(true);
	textField.setNoFill(true);

	textField.setText("test");
	//Center the textfield on the screen

	textField.setPositionGlobal(new Vector3D(mtApplication.width / 2f, mtApplication.height / 2f));


	//val win = new MTWindow(mtApplication,30,30,20,600,400,20,30);
	var txt2 = new MTTextArea(mtApplication);

	this.getCanvas().addChild(txt2)
	textField.setPadding(20)
	//textField.rotateZ(new Vector3D(0.0f,0.3f,0.4f), 30.5f)

	var cp = new MTColorPicker(mtApplication, 120, 120, 300, 300)
	this.getCanvas.addChild(cp)

	var sl = new MTSlider(mtApplication, 200, 10, 200, 50, 1, 200);

	var sl2 = new MTSlider(mtApplication, 500, 10, 800, 50, 1, 2000);
	var sl3 = new MTSlider(mtApplication, 500, 100, 800, 50, 1, 1000);

	var scope = new Scope(mtApplication, 500, -100, 800, 200, 200);
	scope.setPickable(false);

	//Add the textfield to our canvas
	//this.getCanvas().addChild(win);
	this.getCanvas().addChild(textField);
	textField.addChild(sl);
	textField.addChild(sl2);
	textField.addChild(sl3);
	textField.addChild(scope);


	/*cp.colorPicked.observe {
		x => txt2.fillColor() = x;
		true;
	} */

	cp.colorPicked ~> txt2.fillColor


	sl.value.observe {
		x => txt2.text() = x + "f"; true
	}


	cp.colorPicked.start
	textField.text.start

	System.err.println("sadefsdfsd " + cp.colorPicked);

	textField.text <~ cp.colorPicked + ""


	val anim = new InterpolatingAnimation(0f, 100f, 100f, SineInOut)

	textField.height <~ sl.value         //dsgfsdfsdfsdfsdfsdfsdfsfdsdfsfd





	//var test = OSCCommunication.createOSCReceiver(UDP, new InetSocketAddress("127.0.0.1", 57000));
	//test.start;

	var midiOut = MidiCommunication.createMidiOutput("BCR2000, USB MIDI, BCR2000")

	var zz: Int = 0;
	/*
	test.receipt.observe {
		x => {
			System.out.println("sdfsdfsdf sdf sdf" + x);
			val col = new MTColor(x._1.args.head.toString.toFloat, 0, 0);
			//println(x)
			txt2.fillColor() = col;
			txt2.relativePositionToParent() = new Vector3D(0, x._1.args.head.toString.toFloat, 0)
			txt2.text() = "WHAT ??? <--- " + x;
			//println("GOT MSG! "+zz)
			zz = zz + 1;
			true
		}
	}
      */

	//txt2.localRotation <~ (anim.map(x => Rotation(degreeZ = x)))

	textField.text <~ anim + "ssdf"
	var ctrler = 0;
	/*	midiOut.get.send <~ anim.map(x => {val y = (x*100) % 100 ; println(y); ctrler = 70+ctrler+1; MidiCtrlMsg(0,ctrler%100, y/100)} )
  midiOut.get.send <~ anim.map(x => {val y = (x*100) % 100 ; println(y); ctrler = 80+ctrler+1; MidiCtrlMsg(0,ctrler%100, 1.0f-(y/100))} )
*/

	anim.start

	var midiIn = MidiCommunication.createMidiInput("BCR2000, USB MIDI, BCR2000")
	var oscIn = OSCCommunication.createOSCReceiver(OSCCommunication.UDP,new InetSocketAddress("127.0.0.1", 57000) )
	oscIn.observe({x=>println(x);true})

	var kinect = new KinectSkeletonSource(new InetSocketAddress("127.0.0.1", 7110));
	midiIn match {
		case Some(midictrl) =>
			midictrl.receipt.observe {
				m => m match {
					case MidiCtrlMsg(chan, num, v) => {
						val value: Float = (v.toFloat) * 255f
						txt2.fillColor() = new MTColor(value, value, value)
					}
				}
				true
			}

		case _ => println("NO DEVICE FOUND!")
	}

	/*	sl.value.observe {
	  x => midiOut.get.send() = { val y = x.floatValue/200;
								  MidiCtrlMsg(0,90,y)} ; true
  }
*/
	var viewer = new KinectSkeletonViewer(mtApplication, 100, 100, 300, 300, 300, kinect.skeletons(1));

	kinect.start

	this.getCanvas().addChild(viewer);

	kinect.start

	AudioServer.start(() => {
		val test = AudioServer.tt()
		val synth = test.play
		//txt2.localRotation <~ synth.amplitude.map(x => Rotation(degreeX = 100*x));     // (anim.map( x => Rotation(degreeZ = x)))
		txt2.globalPosition <~ synth.amplitude.map(x => new Vector3D(x+100,200.0f))
		scope.plot <~ synth.amplitude
		synth.parameters <~ sl.value.map({x => println(x);("amp"->x.toFloat)})
		synth.parameters <~ sl2.value.map({x => println(x);("freq"->x.toFloat)})
		synth.parameters <~ sl3.value.map({x => println(x);("freq2"->x.toFloat)})
	})

	//textField.text <~ synth.amplitude+""


	////////////////


	//so.deviceName = Some( "MOTU 828mk2" )
	/*	val so = Server.Config();
	so.programPath = "/Applications/SuperCollider3/common/build/scsynth"
	Server.test(so) { s =>
	   // your ScalaCollider code here...
	   play {
		  val f = LFSaw.kr(0.4).madd(24, LFSaw.kr(List(8, 7.23)).madd(3, 80)).midicps
		  CombN.ar(SinOsc.ar(f)*0.04, 0.2, 0.2, 4)
	   }
	}
	///////////////
*/

	/*kinect.skeletons(1).alive.observe {
		x => if (x) {
			txt2.text() = "ALIVE!!"
		} else {
			"DEAD!!!"
		}; true;
	}   */


	/*observe(kinect.skeletons(1).head) {
		x => textField.text() = x + ""; true;
	}*/

	//proc.register({(e:MTOSCControllerInputEvt) => textField.setText(e.getControllerMessage().getArg(0).toString()); true});
	//proc.register(this);

	override def processEvent(sdf: MTOSCControllerInputEvt): Boolean = {

		val x = 300 * (sdf.getControllerMessage.getArg(0).toString().toFloat);
		val y = 300 * (sdf.getControllerMessage.getArg(1).toString().toFloat);
		textField.setPositionGlobal(new Vector3D(x, y, 0));
		sendMsg(Command('Test2, null))
		sendMsg((x, y))
		//textField.setText(x+ " "+y);
		//textField.rotateY(new Vector3D(0,0,0),y)
		//println("x "+x)
		//println("y "+y)
		true
	};

	statemachine {

		import scala.actors.Actor._

		S('A)

		'A is {
			println("-----State A");
			react {
				case (x, y) => println("TUPLED");
				textField.setText(x + " " + y);
				->('A)
				case Command('Test2, _) => println("HAHAHHAHAHAHAAH");
				txt2.setText("sdfsdfsdf");
				->('A)
				case x: Int => println(x);
				transition('B);
				case x: foo => println("FOO");
				->('A);
				case x: bar => println("BAR");
				->('B);
				case "death" => ->('End)
				case x: String => println("sdklfsdklfj!!!!!" + x);
				this transition 'B;
			}
		}

		'B is {
			println("---------State B");
			react {
				case x: Int => println("NUMBERWHUMBA");
				->('A);
				case _ => println("ha");
				->('B);
			}
		}
	}

	private val serialVersionUID = 1L;
}

object Test extends MTApplication {

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		System.out.println("SADSDFSDF");
		val fsm = new HelloWorldScene2(this, "FPPPP");
		this.addScene(fsm)
	}

}
