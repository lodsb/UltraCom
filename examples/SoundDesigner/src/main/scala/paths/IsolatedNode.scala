package ui.paths

import org.mt4j.Application

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import scala.actors._

import ui._
import ui.util._
import ui.paths.types._
import ui.events._
import ui.usability._
import ui.audio._


object IsolatedNode {
  
  def apply(app: Application, center: (Float, Float)) = {
      new IsolatedNode(app, Vec3d(center._1, center._2))
  }
  
  def apply(app: Application, center: Vector3D) = {
      new IsolatedNode(app, center)
  }
}

/**
* This class represents an isolated node.
* An isolated node is not part of any path but can be used to explore the timbre space auditorily by dragging it around.
* Furthermore, an isolated node features audio channels, allowing a user to interactively specify who can listen to generated audio events.
*
*/
class IsolatedNode(app: Application, center: Vector3D) extends Node(app, IsolatedNodeType, None, center) with AudioOutputChannels {
  
  /**
  * Destroys this node, which also implies stopping any playback.
  */
  override def destroy() = {
    Ui.audioInterface ! StopAudioEvent(this.id)
    super.destroy()
  }    
  
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
    g.noFill()
    g.strokeWeight(1)
    val (x,y) = (this.getCenterPointLocal.getX, this.getCenterPointLocal.getY)
    val stepSize = 2*PI.toFloat/this.outputChannelNumber
    (0 until this.outputChannelNumber).foreach(index => {
      val color = AudioOutputChannels.colorFromIndex(index)
      if (this.isOutputChannelOpen(index)) g.stroke(color.getR, color.getG, color.getB, 150) else g.stroke(0, 0, 0, 50)
      g.arc(x, y, 2*this.radius - 2, 2*this.radius - 2, HALF_PI + index*stepSize, HALF_PI + (index+1)*stepSize)
    })    
  }
  
  
}
