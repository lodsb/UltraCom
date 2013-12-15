package ui.paths

import org.mt4j.Application
import org.mt4j.util.math.Vector3D
import org.mt4j.types.{Vec3d}

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.{SessionLogger, Color}

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import scala.actors._

import ui.paths.types._
import ui.properties._
import ui.properties.types._
import ui.events._
import ui.audio._
import ui._
import ui.util._
import ui.tools._
import ui.persistence._


object SingleNode {
  
  def apply(app: Application, center: (Float, Float)) = {
      new SingleNode(app, Vec3d(center._1, center._2))
  }
  
  def apply(app: Application, center: Vector3D) = {
      new SingleNode(app, center)
  }

}

/**
* This class represents a single node.
*/
class SingleNode(app: Application, center: Vector3D) extends ManipulableNode(app, SingleNodeType, center) {

  SessionLogger.log("Created: Node",SessionLogger.SessionEvent.Event, this, null, (center, typeOfNode));

  override def toString: String = {
    super.toString +" @"+this.hashCode()
  }
}
