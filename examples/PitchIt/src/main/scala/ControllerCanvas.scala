package PitchIt


import org.mt4j.components.visibleComponents.shapes.{MTLine, MTRectangle}
import org.mt4j.types.Vec3d
import org.mt4j.util.MTColor._
import org.mt4j.util.math.Vertex

/**
 * This is the class, which contains the pitch-controllers
 */
class ControllerCanvas(val widthValue: Float, val heightValue: Float, val howMany: Int) extends MTRectangle(app, widthValue, heightValue) {


  setStrokeColor(BLUE)
  setFillColor(app.TRANSPARENT)
  initializeControllers(howMany)
  initializeBaseline


  /**
   * Adds some controllers to this canvas. Everyone with equal width.
   * @param howMany Int How many controllers should be initialized
   */
  def initializeControllers(howMany: Int) {
    removeAllChildren()
    for(i <- 1 to howMany) {

      // create a container for a controller
      val container = new ControllerContainer(widthValue/howMany, heightValue)

      // set position of the controller correctly
      val xPosition = (widthValue/howMany)*(i-1) + widthValue/howMany/2
      val yPosition = heightValue/2

      container.setPositionRelativeToParent(Vec3d(xPosition,yPosition))

      // add the controller to the ControllerCanvas
      addChild(container)
    }
  }

  def initializeBaseline {
    // initialize baseline
    val lineStart = new Vertex(Vec3d(0f, heightValue/2f))
    val lineEnd = new Vertex(Vec3d(widthValue, heightValue/2f))
    val baseline = new MTLine(app, lineStart, lineEnd)
    baseline.setStrokeColor(YELLOW)
    baseline.removeAllGestureEventListeners()
    addChild(baseline)
  }

}
