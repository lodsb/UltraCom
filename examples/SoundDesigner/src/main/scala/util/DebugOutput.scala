package ui.util

import org.mt4j.Application

import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.visibleComponents.AbstractVisibleComponent

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.Vec3d

import processing.opengl.PGraphicsOpenGL

import ui._

/**
* This singleton is for basic debugging. It provides a means to render an arbitrary number of points to the screen,
* allowing for the correctness of e.g. distance or position calculations to be verified visually.
*/
object DebugOutput extends AbstractVisibleComponent(Ui) {    

  var points = Map[String, Vector3D]()
  
  /**
  * Draws debugging output.
  */
  override def drawComponent(g: PGraphicsOpenGL) = {
    g.stroke(255, 0, 0)
    g.fill(255, 0, 0)
    points.values.foreach(position => 
      {
        g.ellipse(position.getX, position.getY, 5, 5)
      }
    )
  }  
  
  /**
  * Sets a point with the specified identifier at the specified position,
  * or updates it if it already exists.
  */
  def setPoint(pointIdentifier: String, position: Vector3D): Unit = {
    if (points.contains(pointIdentifier)) {
      points = points.updated(pointIdentifier, position)
    }
    else {
      points = points + (pointIdentifier -> position)
    }
  }
 
  def setPoint(pointIdentifier: String, position: (Float, Float)): Unit = {
    this.setPoint(pointIdentifier, Vec3d(position._1, position._2))
  }
  
  def removePoint(pointIdentifier: String) = {
    points = points - pointIdentifier
  }
  
}
