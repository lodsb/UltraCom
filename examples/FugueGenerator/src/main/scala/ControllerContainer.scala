package de.ghagerer.FugueGenerator

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MTColor._
import org.mt4j.types.Vec3d
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.util.math.{Vertex, Vector3D}
import scala.math._
import org.mt4j.components.TransformSpace._

/**
 * A wrapper class for controllers. For better handling of the touch/draw events.
 */
class ControllerContainer(val widthValue: Float, var heightValue: Float)
  extends MTRectangle(app, widthValue, heightValue) with IGestureEventListener {

  // make it unmovable
  removeAllGestureEventListeners()

  // add a controller
  val controller = new Controller(widthValue, 0f)
  addChild(controller)
  controller.setPositionRelativeToParent(Vec3d(widthValue/2f, heightValue/2f))

  // color stuff
  setStrokeColor(RED)
  setFillColor(app.TRANSPARENT)

  /**
   * Takes up DragEvents and passes it either up to the ControllerCanvas
   * or down to its Controller.
   * @param ge The GestureEvent, handled like a DragEvent
   **/
  override def processGestureEvent(ge: MTGestureEvent): Boolean = {
    val drag = ge.asInstanceOf[DragEvent]

    parent.logDragEvent(drag)

    if(this.containsPointGlobal(drag.getTo) || controller.containsPointGlobal(drag.getTo)) {
      controller.processGestureEvent(ge)
    } else {
      parent.processGestureEvent(ge)
    }
    true
  }

  /**
   * This is complicated stuff necessary for calculating
   * the new height for its Controller during a DragEvent.
   * Here the nearest corner of this container gets returned
   * for a passed point.
   * @param point The passed point
   * @return The corner of this controller-rectangle next to the point
   */
  def getNearestCorner(point: Vector3D): Vector3D = {

    // initialization
    var distance = Float.MaxValue
    var tmp = 0f
    var nearestCorner = null.asInstanceOf[Vector3D]
    val vertices: Array[Vertex] = getVerticesGlobal.clone()

    // throw out the same corners as the passed point
    for(i <- 0 to vertices.length-1) {
      if(vertices(i).equalsVector(point)) {
        vertices(i) = null.asInstanceOf[Vertex]
      }
    }

    // find nearestCorner
    vertices.foreach {
      case null =>
      case vertex: Vertex =>
        tmp = point.distance2D(vertex)
        if (tmp < distance) {
          distance = tmp
          nearestCorner = vertex
        }
    }
    nearestCorner
  }

  /**
   * This is complicated stuff necessary for calculating
   * the new height for its Controller during a DragEvent.
   * A passed point (where the drag happens) gets passed
   * and a new height for the controller gets calculated.
   * @param point The point, where a drag happens
   * @return Float The new height for the controller
   */
  def getNewHeight(point: Vector3D): Float = {

    // Get the two nearest corners = the nearest line of the controller rectangle
    val nearestCorner = getNearestCorner(point)
    val secondNearestCorner = getNearestCorner(nearestCorner)

    // calculate the new height out of the known points

    // the connection lines to the nearest corners
    val a = point.getSubtracted(nearestCorner)
    val b = secondNearestCorner.getSubtracted(nearestCorner)

    // the angle between the connection lines
    val angle = a.angleBetween(b).toDouble

    // distance from the point to the connection line
    val distance = a.length() * sin(angle)

    // the new height for the controller rectangle
    // height of the controller/2 minus the distance
    var newHeight = heightValue/2f - distance.toFloat

    // take the correct signum
    if (parent.degrees%180 == 0) {
      // the canvas lies horizontally
      newHeight *= parent.signum * parent.getPosition(GLOBAL).getSubtracted(point).getY.signum
    } else {
      // the canvas lies vertically
      if (parent.getPosition(GLOBAL).getX < point.getX) {
        newHeight *= -1
      }
      if (parent.degrees%270 == 0) {
        newHeight *= -1
      }
    }

    newHeight
  }
  def center = getCenterPointGlobal
  def parent = getParent.asInstanceOf[ControllerCanvas]

}
