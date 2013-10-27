package PitchIt

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MTColor._
import org.mt4j.util.MTColor
import org.mt4j.types.Vec3d

/**
 * A wrapper class for controllers. For better handling of the touch/draw events.
 */
class ControllerContainer(val widthValue: Float, var heightValue: Float) extends MTRectangle(app, widthValue, heightValue) {

  removeAllGestureEventListeners()
  val controller = new Controller(widthValue, heightValue/4f)
  addChild(controller)
  controller.setPositionRelativeToParent(Vec3d(widthValue/2f, 5*heightValue/8f))
  setStrokeColor(RED)
  setFillColor(app.TRANSPARENT)

}
