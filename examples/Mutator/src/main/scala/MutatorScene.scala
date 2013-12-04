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

//package Mutator

import de.sciss.osc.Message
import java.net.InetSocketAddress
import org.mt4j.input.osc.OSCCommunication
import org.mt4j.input.osc.OSCCommunication.UDP
import org.mt4j.{Scene, Application}
import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.components.ComponentImplicits._
import org.mt4j.stateMachine.StateMachine
import org.mt4j.components.visibleComponents.widgets._
import org.mt4j.types.{Vec3d, Rotation}
import org.lodsb.reakt.Implicits._
import scala.actors.Actor._
import org.mt4j.components.visibleComponents.shapes.{Line, MTLine}
import java.util.Random


object Mutator extends Application {
	/*
			Settings, such as <b>the application name<b>, display properties, etc are set in Settings.txt
	 */

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		val scene = new MutatorScene(this, "Mutator");
		this.addScene(scene)
	}

}


class MutatorScene(app: Application, name: String) extends Scene(app,name) {

	// Show touches
	showTracer(true)

	/*
		Basics

		- component creation
		- changing properties
	 */

	// Create two UI components ...
	var textField = TextArea();
  var textField2 = TextArea();
	var slider = Slider(0,100);

  canvas += slider ++ textField ++ textField2

  textField.globalPosition := Vec3d(100,100)
  textField2.globalPosition := Vec3d(100,300)


  val oscTransmit = OSCCommunication.createOSCTransmitter(UDP, new InetSocketAddress("131.159.200.144", 1338))

  oscTransmit.send <~ slider.value.map( x => Message("/foo",x))

 textField.text <~ slider.value + ""

  val oscReceive = OSCCommunication.createOSCReceiver(UDP, new InetSocketAddress("127.0.0.1", 1340))

  textField2.text <~ oscReceive.receipt.map(x => x)+""

  textField.text := "foo"
  textField2.text := "bar"




}
