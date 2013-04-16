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

package cyntersizer

import org.mt4j.Application

import org.mt4j.util.math.Vector3D

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  17.03.13 :: 17:11
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 * ^     ^
 *  ^   ^
 *  (o o)
 * {  |  }                  (Wong)
 *    "
 *
 * Don't eat the pills!
 */
object app extends Application {

  // some global values
  def apply = this
  def center = new Vector3D(app.width / 2f, app.height / 2f)
  var scene:NodeScene = null

  //all nodes are stored here
  var globalNodeSet = new NodeSet[DragableNode] {}

  // start scene
  def main(args: Array[String]) {
    execute(false)
  }

  override def startUp() {
    scene = new CyntersizerScene()
    addScene(scene)
  }

}


class CyntersizerScene() extends NodeScene() {

  Metronome().start()
  //SourceNode()
  SourceNode() += (NewRandomNode() += NewRandomNode(),NewRandomNode(),NewRandomNode(),NewRandomNode())
  println("SourceNode().size = "+SourceNode().size)
  /*

  // Show touches
  // showTracer(true)


  // Create two UI components ...
  var textField = TextArea()
  var slider = Slider(0,100)

  // ^^ this can also be done java style using plain mt4j
  var textField2 = new MTTextArea(app)
  var slider2 = new MTSlider(app,100,100,100,20,0,100);

  //Adding the textarea reakt style to canvas
  canvas += textField;

  //Adding slider to canvas, original java style
  this.getCanvas.addChild(slider);

	//Adding arrays as children to an object (could be canvas), could be single object, too
	//textField += textField2++slider2

	//Removing a child
	textField -= textField2

	// Setting position ... java style from original MT4J
	textField.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));

	// same, but reakt style (makes use of properties, see below)
	textField.globalPosition := Vec3d(app.width / 2f, app.height / 2f);

	// setting and reading Properties
	textField.text:="foo"
	textField.text() ="foo"
	val foo = textField.text();
	slider.globalPosition() = Vec3d(100,200);

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
	textField.text <~ slider.value.map({ x => x+"" })

	// convert the Float event stream to a Rotation and a Vector Event stream
	textField.localRotation <~ slider.value.map({ x => Rotation(degreeY = x*0.4f) })
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


	// fix button

	// FSM

	// AUDIO


  */
}
