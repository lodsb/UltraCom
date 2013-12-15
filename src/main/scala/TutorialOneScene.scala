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

package TutorialOne

import org.mt4j.{Scene, Application}
import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.visibleComponents.widgets._
import org.mt4j.types.{Vec3d, Rotation}
import org.lodsb.reakt.Implicits._
import scala.actors.Actor._
import org.mt4j.components.visibleComponents.shapes.{Line, MTLine}
import java.util.Random
import org.mt4j.util.stateMachine.StateMachine


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

	// Show touches
	//showTracer(true)

	/*
		Basics

		- component creation
		- changing properties
	 */

	// Create two UI components ...
	var textField = TextArea();
	var slider = Slider(0,100);

	// ^^ this can also be done java style using plain mt4j
	var textField2 = new MTTextArea(app)
	var slider2 = new MTSlider(app,100,100,100,20,0,100);

	//Adding the textarea reakt style to canvas
	canvas += textField;

	//Adding slider to canvas, original java style
	this.getCanvas.addChild(slider);

	//Adding arrays as children to an object (could be canvas), could be single object, too
	textField += textField2++slider2

	//Removing a child
	//textField -= textField2

	// Setting position ... java style from original MT4J
	textField.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));
  textField2.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));

	// same, but reakt style (makes use of properties, see below)
	textField.globalPosition := Vec3d(app.width / 2f, app.height / 2f);
  textField2.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));

	
	// setting and reading Properties
	textField.text:="foo"
	textField2.text() ="foo"
	val foo = textField.text();
	slider.globalPosition() = Vec3d(100,200);

	textField2.rotateX(Vec3d(100,0,0), 123.0f)

	/*
		other component properties are
		- global and relative position
		- rotation
		- padding ...

	 */


	/*
		Linking/Connecting event streams

		Some examples about making use of event streams
		- linking
		- modifying
		- general observing

		NOTE: All event streams are thread-save! so no worries about cross-connecting UI/Network/Input streams :)

	 */

	// Connect a slider to a textarea (show slider values)
	// - every time the slider outputs a value, it is updating the text field
	// - the type conversion of Float to String is done automatically (mostly works in simple cases)
	textField.text <~ slider.value + ""

	// convert the Float event stream to a Rotation and a Vector Event stream
	textField.localRotation <~ slider.value.map({ x => Rotation(degreeX = x*0.4f) })
	textField.globalPosition <~ slider.value.map({ x => Vec3d(x*10f,x*10f,0) })

	// convert the Float event stream to a Color event stream
	textField.strokeColor <~ slider.value.map({ x => Color(0f,x,255f-x) })
	// 						              ^^ this and the examples before create other anonymous signals,
	// 										 we can't simply disconnect them explictly
	// so the only solution is to disconnect all sources (from strokeColor in this case)
	textField.strokeColor.disconnectAll

	// but we can also use a variable for the intermediate signal, so connecting/disconnecting can be done this way
	val intermediateSignal = slider.value.map({ x => Color(0f,x,255f-x) })
	textField.strokeColor <~ intermediateSignal
	textField.strokeColor.disconnect(intermediateSignal)
	// or with an overloaded operator
	textField.strokeColor |~ (intermediateSignal)

	// reconnect...
	textField.strokeColor <~ intermediateSignal

	/* <b>Other signal operators are:</b>
	   ~> 				: connect; works to successively concatenate signals as well!
	   signal() = value : push value into signal
	   :> merge         : merge two signals to one - creates tuple signal

	   arithmetic
	   +, - , /, * 		- creates a signal that contains the result of two signals' arithmetic operation

	   min, max 		- creates signal containing the min/max of two signals

	   comparisons 		- creates a boolean signal
	   < , > , <= , >=, eq

	   DIY:
	   binop(fun: (T,T) => T) - roll your binary op
	*/


	/*
		(little) Finite State Machine and Buttons

		An utterly useless example
	 */
	val button = Button("Some text...");
	val slider3= Slider(0,20)

	button.globalPosition() = Vec3d(100,300)
	slider3.globalPosition() = Vec3d(100,500)

	canvas += button

	// just for fun: create a sequence of lines with a random start point
	val r = new Random()
	def rval(): Float = { (r.nextFloat()*600)%300}

	val lines = (1 to 25).map({x =>
		val l = Line()
		l.setStartPoint(Vec3d(rval()*2,rval()*2,rval()))
		l.setStrokeColor( Color(rval(),rval(),rval()) )
    l.setPickable(false)
		// connect the end position of each line to the (position of the) textfield
		l.endPosition <~ textField.globalPosition
    //textField2.globalPosition.observe({x=> println(x); l.setEndPoint(x);true})
      canvas += l
//    l.endPosition |~ textField.globalPosition
		l
	})

}
