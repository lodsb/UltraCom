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

import org.mt4j.components.visibleComponents.font.{Font, FontManager}
import org.mt4j.components.visibleComponents.style.Style
import org.mt4j.input.inputData.{AbstractCursorInputEvt, MTFingerInputEvt}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragProcessor, DragEvent}
import org.mt4j.input.inputProcessors.globalProcessors.{CameraProcessor, AbstractGlobalInputProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.util.Alpha
import org.mt4j.util.animation._
import org.mt4j.util.animation.LoopRepetitions
import org.mt4j.util.animation.Vector3DTransition
import org.mt4j.util.ColorInterpolation
import org.mt4j.util.ColorInterpolation
import org.mt4j.util.ColorLightness
import org.mt4j.util.ColorLightness
import org.mt4j.util.ColorOpacity
import org.mt4j.util.ColorOpacity
import org.mt4j.util.ColorRotation
import org.mt4j.util.ColorRotation
import org.mt4j.util.ColorSaturation
import org.mt4j.util.ColorSaturation
import org.mt4j.{Scene, Application}
import org.mt4j.util._
import org.mt4j.util.math.{Tools3D, Vector3D}
import org.mt4j.components.ComponentImplicits._
import org.mt4j.util.stateMachine.StateMachine
import org.mt4j.components.visibleComponents.widgets._
import org.mt4j.types.{Vec3d, Rotation}
import org.lodsb.reakt.Implicits._
import scala.actors.Actor._
import org.mt4j.components.visibleComponents.shapes._
import java.util.Random


object TutorialOne extends Application {
	/*
			Settings, such as <b>the application name<b>, display properties, etc are set in Settings.txt
	 */

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		val scene = new TutorialOneScene(this, "Scene of Tutorial One");
		this.addScene(scene)
	}

}


class TutorialOneScene(app: Application, name: String) extends Scene(app,name) {
  self =>
	// Show touches
	showTracer(true)


  import Style._

	/*
		Basics

		- component creation
		- changing properties
	 */


  val r = new CameraProcessor(this)


  this.registerGlobalInputProcessor(r)

	// Create two UI components ...
  var textField = TextField();
	var slider = Slider(0,1);

	// ^^ this can also be done java style using plain mt4j
//	var textField2 = TextField()
//  var textField3 = TextField()
//  var textField4 = TextField()
	var slider2 = Slider(0,1)
  slider.globalPosition() = Vec3d(200,200)

	//Adding the textarea reakt style to canvas
	canvas += textField ++ slider2 ++ slider// ++ textField2++textField3++textField4

  slider2 use Style(
    "fillColor" := Color.RED
  )

  val testColor = Color.MAROON

  val rect = Slider(0,1)
  rect.globalPosition() = Vec3d(100,100,-50)

  val slider3 = Slider(0,1)
  slider3 use Style(
    "globalPosition" := Vec3d(200,300),
    "fillColor" := Color.FUCHSIA
  )

  val slider4 = Slider(0,1)
  slider4 use Style(
    "globalPosition" := Vec3d(200,400),
    "fillColor" := Color.FUCHSIA
  )

  val slider5 = Slider(0,1)
  slider5 use Style(
    "globalPosition" := Vec3d(200,500),
    "fillColor" := Color.BLACK
  )

  val group = Group(vertical = false)
  canvas += group

  val tt = TextArea("some sliders")

  group += tt++slider3 ++ slider4 ++ slider5

  //canvas += slider3 ++ slider4 ++ slider5

  slider2.value.observe{x=> rect.colorTransformation(ColorLightness(x));true }
  slider.value.observe{x=> rect.colorTransformation(ColorSaturation(x));true }
  slider3.value.observe{x=> rect.colorTransformation(ColorRotation(360f*x));true }
  slider4.value.observe{x=> rect.colorTransformation(ColorOpacity(x));true }
  slider5.value.observe{x=> rect.colorTransformation(ColorInterpolation(x, Color.TEAL, destination=Color.LIME));true }

  val rand = new Random()
  def rval(): Int = { (rand.nextInt(255))}
  val button = Button("reset")
  canvas += button
  button.globalPosition() = Vec3d(100,200,0)
  button.pressed.observe{x=> if(x){rect.fillColor() = Color(rval(), rval(), rval())};true}

  val button2 = Button("start animation")
  button2 use Style {
    "globalPosition" := Vec3d(300,300)
    "fillColor" := Color.RED
  }

  button2.globalPosition() = Vec3d(400,400)

  canvas += button2

  val tween = Tween(2.0f, LoopRepetitions(1))
 // tween.step.observe({ x => button.colorTransformation(ColorOpacity(x._2)); true})
  tween.step.observe({ x => slider.colorTransformation(ColorOpacity(x)); true})
  textField.text <~ tween.step.map(x => Color((x*255).toInt, 0,0))+""

  val otherEasing = QuartInOut()
  val tween2 = Tween(3f, LoopRepetitions(1))
  slider.globalPosition <~ tween2.step.map(x=>otherEasing.map(y=>Interpolation(y, rect.globalPosition(), Vec3d(100,100,100)))(x))



  //button.globalPosition <~ tween.step.map(x=> Vec3d(100,200,150f*x._2))
  //val a = rect.globalPosition()
  var a = Vec3d(10,10)
  val myEasing = BounceOut()

  // loop this
  tween before tween2 before tween


  val transition = Vector3DTransition(slider2.globalPosition, Vec3d(0,0), Vec3d(400,400), 4f)
  transition.start <~ button2.pressed
  button.pressed.observe{x=> tween.animation.start();true}

  textField.text <~ slider.value+"dd"

  //rect.globalPosition.observe({x => println(x + " | "+ rect.globalPosition.get()); a = x; true})

  canvas += rect
  rect.setPickable(true)

  val rr = Rectangle(100,100)
  canvas += rr

  group use Style(
    "fillColor" := Color.GRAY - Alpha(80)
  )







  textField.font() = Font("data/fonts/cardo/Cardo104s.ttf")
/*
  textField2 use Style(
    "font" := Font("data/fonts/arvo/Arvo-Regular_hinted.ttf")
  )

  textField3 use Style(
    "font" := Font("data/fonts/cardo/Cardo104s_hinted.ttf")
  )

  textField4 use Style(
    "font" := Font("data/fonts/vera/Vera_hinted.ttf")
  ) */

	//Adding slider to canvas, original java style
	//this.getCanvas.addChild(slider);

  textField.text := "OOFcnorm absc ASDb"
//  textField2.text() = "OOFArv absc ASDb"
//  textField3.text := "FOOchint absc ASDb"
//  textField4.text() = "VeraFOOOOO absc ASDb"


  this.setClearColor(Color.AQUA);

  slider use Style(
    "fillColor" := Color.TEAL
  )





  //Adding arrays as children to an object (could be canvas), could be single object, too
	//textField += textField2++slider2

  //var svg = new SVG("NewTux.svg")
  //canvas() += svg

//	//Removing a child
//	//textField -= textField2
//
//	// Setting position ... java style from original MT4J
//	textField.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));
//  textField2.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));
//
//	// same, but reakt style (makes use of properties, see below)
//	textField.globalPosition := Vec3d(app.width / 2f, app.height / 2f);
//  textField2.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));
//
//	textField2.globalPosition.observe{{x => println("ROTOTOTO "+x); true}}
//
//
//	// setting and reading Properties
//	textField.text:="foo"
//	textField2.text() ="foo"
//	val foo = textField.text();
//	slider.globalPosition() = Vec3d(100,200);
//
//	textField2.rotateX(Vec3d(100,0,0), 123.0f)
//
//	/*
//		other component properties are
//		- global and relative position
//		- rotation
//		- padding ...
//
//	 */
//
//
//	/*
//		Linking/Connecting event streams
//
//		Some examples about making use of event streams
//		- linking
//		- modifying
//		- general observing
//
//		NOTE: All event streams are thread-save! so no worries about cross-connecting UI/Network/Input streams :)
//
//	 */
//
//	// Connect a slider to a textarea (show slider values)
//	// - every time the slider outputs a value, it is updating the text field
//	// - the type conversion of Float to String is done automatically (mostly works in simple cases)
//	textField.text <~ slider.value + ""
//
//	// convert the Float event stream to a Rotation and a Vector Event stream
//	textField.localRotation <~ slider.value.map({ x => Rotation(degreeX = x*0.4f) })
//	textField.globalPosition <~ slider.value.map({ x => Vec3d(x*10f,x*10f,0) })
//
//	// convert the Float event stream to a Color event stream
//	textField.strokeColor <~ slider.value.map({ x => Color(0f,x,255f-x) })
//	// 						              ^^ this and the examples before create other anonymous signals,
//	// 										 we can't simply disconnect them explictly
//	// so the only solution is to disconnect all sources (from strokeColor in this case)
//	textField.strokeColor.disconnectAll
//
//	// but we can also use a variable for the intermediate signal, so connecting/disconnecting can be done this way
//	val intermediateSignal = slider.value.map({ x => Color(0f,x,255f-x) })
//	textField.strokeColor <~ intermediateSignal
//	textField.strokeColor.disconnect(intermediateSignal)
//	// or with an overloaded operator
//	textField.strokeColor |~ (intermediateSignal)
//
//	// reconnect...
//	textField.strokeColor <~ intermediateSignal
//
//	/* <b>Other signal operators are:</b>
//	   ~> 				: connect; works to successively concatenate signals as well!
//	   signal() = value : push value into signal
//	   :> merge         : merge two signals to one - creates tuple signal
//
//	   arithmetic
//	   +, - , /, * 		- creates a signal that contains the result of two signals' arithmetic operation
//
//	   min, max 		- creates signal containing the min/max of two signals
//
//	   comparisons 		- creates a boolean signal
//	   < , > , <= , >=, eq
//
//	   DIY:
//	   binop(fun: (T,T) => T) - roll your binary op
//	*/
//
//
//	/*
//		(little) Finite State Machine and Buttons
//
//		An utterly useless example
//	 */
//	val button = Button("Some text...");
//	val slider3= Slider(0,20)
//
//	button.globalPosition() = Vec3d(100,300)
//	slider3.globalPosition() = Vec3d(100,500)
//
//	canvas += button
//
//	// just for fun: create a sequence of lines with a random start point
//	val r = new Random()
//	def rval(): Float = { (r.nextFloat()*600)%300}
//
//	val lines = (1 to 25).map({x =>
//		val l = Line()
//		l.startPosition() = Vec3d(rval()*2,rval()*2,rval())
//		l.setStrokeColor( Color(rval(),rval(),rval()) )
//		// connect the end position of each line to the (position of the) textfield
////		l.endPosition <~ textField.globalPosition
//    l.endPosition <~ textField2.globalPosition
////    l.endPosition |~ textField.globalPosition
//		l
//	})
//
//	val myFSM = new StateMachine { // Derive & define a StateMachine
//
//		fsm {
//			// S - is used to define the start state MyStart - the "'" is used by scala to denote symbols
//			S('MyStart)
//
//			// define the state MyStart
//			'MyStart is {
//				println("I am in state MyStart")
//
//				button.text() = "Trigger me!"
//
//				// tell the state to react on input,
//				// this can use the usual pattern matching, e.g. tuples, types,...
//				react {
//
//					// do something if true is received:
//					// transition to state ShowMoreUI, before that
//					// show all lines, we created before
//					case true => {
//						// add sequence (array) to the canvas
//						canvas += lines
//						->('UseMoreUI) // with UTF symbol: →('ShowMoreUI)
//					}
//				}
//			}
//
//			'UseMoreUI is {
//				println("I am in state UseMoreUI")
//				button.text() = "Use sliders?"
//
//				canvas += slider3
//
//				react {
//					case true => {
//						canvas -= lines ++ slider3
//						->('MyStart)
//					}
//
//					case x:Float => {
//						slider.fillColor() = Color(x,0,0)
//						->('UseMoreUI)
//					}
//
//					case x:String => {
//						textField.text() = x+" use button to go back"
//						->('UseMoreUI)
//					}
//
//				}
//			}
//		}
//	}
//
//	// push the various UI outputs to the state machine and let it decide how to react
//	button.pressed.observe({ x => myFSM.consume(x); true })
//	slider.value.observe({ x => myFSM.consume(x); true })
//	slider3.value.observe({ x => myFSM.consume("Some val "+x); true })

}
