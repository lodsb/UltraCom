package basic.helloWorld

import org.mt4j.util.math.Vector3D
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.components.visibleComponents.font.IFont
import org.mt4j.input.inputSources.osc.OSCInputSource
import org.mt4j.util.MTColor

import org.mt4j.input.osc.OSCCommunication
import java.net.{SocketAddress, InetSocketAddress}
import de.sciss.osc._

import java.net.InetAddress
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt
import org.mt4j.MTApplication
import org.mt4j.sceneManagement.SimpleAbstractScene
import org.mt4j.input.IMTInputEventListener
import org.mt4j.input.inputProcessors.globalProcessors.{AbstractGlobalInputProcessor, GlobalOSCInputProcessor, CursorTracer}
import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.stateMachine.StateMachine
import org.mt4j.components.interfaces.IMTComponent3D
import org.mt4j.eventSystem.{foo, bar, TrEventListener}
import org.mt4j.commandSystem.Command
import react.Observing
import org.mt4j.input.midi.{MidiMsg, MidiCommunication, MidiCtrlMsg}
import org.mt4j.input.kinect.KinectSkeletonSource
import org.mt4j.components.visibleComponents.widgets._

class HelloWorldScene2(mtApplication: MTApplication, name: String)
	extends SimpleAbstractScene(mtApplication, name)

	with TrEventListener[MTOSCControllerInputEvt]
	with StateMachine
	with Observing {

	import react._

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

	textField.addChild(txt2)
	textField.setPadding(20)
	//textField.rotateZ(new Vector3D(0.0f,0.3f,0.4f), 30.5f)

	var cp = new MTColorPicker(mtApplication, 120, 120, 300, 300)
	this.getCanvas.addChild(cp)

	var sl = new MTSlider(mtApplication, 200, 10, 200, 50, 1, 200);

	//Add the textfield to our canvas
	//this.getCanvas().addChild(win);
	this.getCanvas().addChild(textField);
	this.getCanvas().addChild(sl);

	Signal {
		txt2.fillColor() = cp.colorPicked();
		true;
	}.setAlwaysActive
	observe(sl.value) {
		x => txt2.text() = x + "f"; true
	}

	Signal {
		textField.text() = cp.colorPicked() + "sdf"
	}.setAlwaysActive


	Signal {
		textField.height() = sl.value()
	}.setAlwaysActive


	var test = OSCCommunication.createOSCReceiver(UDP, new InetSocketAddress("127.0.0.1", 57000));
	test.start;


	var kinect = new KinectSkeletonSource(new InetSocketAddress("127.0.0.1", 7110));

	var midiOut = MidiCommunication.createMidiOutput("BCR2000, USB MIDI, BCR2000")

	var zz: Int = 0;
	observe(test.receipt) {
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
	Signal {
		textField.strokeColor() = cp.colorPicked()
	}.setAlwaysActive

	var midiIn = MidiCommunication.createMidiInput("BCR2000, USB MIDI, BCR2000")

	midiIn match {
		case Some(midictrl) =>
			observe(midictrl.receipt) {
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

	observe(sl.value) {
		x => midiOut.get.send() = { val y = x.floatValue/200;
									MidiCtrlMsg(0,90,y)} ; true
	}

	var viewer = new KinectSkeletonViewer(mtApplication,100,100 , 300,300,300, kinect.skeletons(1));

	this.getCanvas().addChild(viewer);

	kinect.start

	observe(kinect.skeletons(1).alive) {
		x => if (x) {
			txt2.text() = "ALIVE!!"
		} else {
			"DEAD!!!"
		}; true;
	}


	/*observe(kinect.skeletons(1).head) {
		x => textField.text() = x + ""; true;
	} */

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
				case _ => println("FUCKYOURMOM");
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
