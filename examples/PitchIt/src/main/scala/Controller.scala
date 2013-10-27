package PitchIt

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MTColor._
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragEvent, DragProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}

/**
 * The Controller, which controls and visualizes the pitch of tones.
 */
class Controller(var widthValue: Float, var heightValue: Float) extends MTRectangle(app, widthValue, heightValue) with IGestureEventListener {

  setStrokeColor(GREEN)
  setFillColor(app.TRANSPARENT)

  removeAllGestureEventListeners()

  addGestureListener(classOf[DragProcessor], this)

  /**
   * Process gesture event.
   *
   * @param ge the ge
   * @return true, if successful
   */
  override def processGestureEvent(ge: MTGestureEvent): Boolean = {
    val drag = ge.asInstanceOf[DragEvent]
    if(getParent.containsPointGlobal(drag.getTo) || this.containsPointGlobal(drag.getTo)) {
      height() = getHeightXYGlobal+drag.getTranslationVect().y
    }
    true
  }

}
