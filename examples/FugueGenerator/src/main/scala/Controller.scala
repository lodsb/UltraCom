package de.ghagerer.FugueGenerator

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MTColor._
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragEvent, DragProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import scala.collection.mutable.ArrayBuffer
import org.mt4j.util.math.Vertex

/**
 * The Controller, which controls and visualizes the pitch of tones.
 */
class Controller(var widthValue: Float, var heightValue: Float)
  extends MTRectangle(app, widthValue, heightValue) with IGestureEventListener {

  var active = false

  setStrokeColor(color)
  setFillColor(app.TRANSPARENT)
  removeAllGestureEventListeners()
  addGestureListener(classOf[DragProcessor], this)


  /**
   * for triggering the active-status (on/off)
   */
  def triggerActive {
    active = !active
    setStrokeColor(color)
  }

  /**
   * returns the color for the current status
   * @return an MTColor object
   */
  def color = if (active) WHITE else GREY


  /**
   * Process gesture event.
   *
   * @param ge the ge
   * @return true, if successful
   */
  override def processGestureEvent(ge: MTGestureEvent): Boolean = {
    val drag = ge.asInstanceOf[DragEvent]

    parent.parent.logDragEvent(drag)

    if(parent.containsPointGlobal(drag.getTo) || this.containsPointGlobal(drag.getTo)) {
      height() = parent.getNewHeight(drag.getTo)
    } else {
      parent.parent.processGestureEvent(ge)
    }
    true
  }

  def parent = getParent.asInstanceOf[ControllerContainer]

  def getHeight: Float = {
    val vertices: Array[Vertex] = getVerticesLocal.clone()//getVerticesGlobal
    var upperCorners = new ArrayBuffer[Vertex]()
    upperCorners += vertices(0)
    var lowerCorners = new ArrayBuffer[Vertex]()
    for (i <- 1 to 3) {
      if (vertices(0).getY == vertices(i).getY) {
        upperCorners += vertices(i)
      } else {
        lowerCorners += vertices(i)
      }
    }
    try {
      lowerCorners(0).getY - upperCorners(0).getY
    } catch {
      case e: IndexOutOfBoundsException => 0f
    }
  }

}
