package PitchIt

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MTColor._
import org.mt4j.types.Vec3d
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.util.math.{Vertex, Vector3D}
import scala.math._

/**
 * A wrapper class for controllers. For better handling of the touch/draw events.
 */
class ControllerContainer(val widthValue: Float, var heightValue: Float)
  extends MTRectangle(app, widthValue, heightValue) with IGestureEventListener {

  // make it unmovable
  removeAllGestureEventListeners()

  // add a controller
  val controller = new Controller(widthValue, heightValue/4f)
  addChild(controller)
  controller.setPositionRelativeToParent(Vec3d(widthValue/2f, 5*heightValue/8f))

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
    var distance = Float.MaxValue
    var tmp = 0f
    var nearestCorner = null.asInstanceOf[Vector3D]
    val vertices: Array[Vertex] = getVerticesGlobal.clone()

    // throw out the same corner
    for(i <- 0 to vertices.length-1) {
      if(vertices(i).equalsVector(point)) {
        vertices(i) = null.asInstanceOf[Vertex]
      }
    }

    vertices.foreach( vertex => { if(vertex != null) {
      tmp = point.distance2D(vertex)
      if(tmp < distance) {
        distance = tmp
        nearestCorner = vertex
      }
    }})
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

    // Get the two nearest corners
    val nearestCorner = getNearestCorner(point)
    val secondNearestCorner = getNearestCorner(nearestCorner)

    // calculate the new height out of the known points
    val a = point.getSubtracted(nearestCorner)
    val b = secondNearestCorner.getSubtracted(nearestCorner)
    val angle = a.angleBetween(b).toDouble
    val distance = a.length() * sin(angle)
    var newHeight = heightValue/2f - distance.toFloat
    newHeight *= -1 * center.getSubtracted(point).getY.signum

    newHeight
  }

  def center = getCenterPointGlobal
  def parent = getParent.asInstanceOf[ControllerCanvas]

}
