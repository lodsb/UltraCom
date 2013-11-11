package PitchIt


import org.mt4j.components.visibleComponents.shapes.{MTLine, MTRectangle}
import org.mt4j.types.Vec3d
import org.mt4j.util.MTColor._
import org.mt4j.util.math.Vertex
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import scala.collection.mutable.ArrayBuffer

/**
 * This is the class, which contains the pitch-controllers
 */
class ControllerCanvas(val widthValue: Float, val heightValue: Float, howMany: Int)
  extends MTRectangle(app, widthValue, heightValue) with IGestureEventListener {

  setStrokeColor(BLUE)
  setFillColor(app.TRANSPARENT)

  // containers are all ControllerContainers. Needed for their sequential ordering
  var containers = null.asInstanceOf[ArrayBuffer[ControllerContainer]]
  var activeController = null.asInstanceOf[Controller]

  initializeBaseline
  initializeControllers(howMany)


  /**
   * Adds some controllers to this canvas. Every with equal width.
   * @param howMany Int How many controllers should be initialized
   */
  def initializeControllers(howMany: Int) {

    // quit if there are no controllers or the number of controllers didn't change
    if (containers != null && containers.length == howMany) {
      return
    }

    // save the heights from the first and middle controller for re-use later
    // instantiation
    val oldHeights = new ArrayBuffer[Float]()

    if (containers != null) {
      containers.foreach( container => {
        // saving old heights for later use
        oldHeights += container.controller.getHeight
        // remove all children / old controllers
        container.removeFromParent()
      })
    }

    // remove all left old controllers
    removeAllChildren()

    // create new controllers and add them
    val tmpContainers = new ArrayBuffer[ControllerContainer]
    for(i <- 1 to howMany) {

      // create a container for a controller
      val container = new ControllerContainer(widthValue/howMany, heightValue)

      // set position of the controller correctly
      val xPosition = (widthValue/howMany)*(i-1) + widthValue/howMany/2
      val yPosition = heightValue/2

      container.setPositionRelativeToParent(Vec3d(xPosition,yPosition))

      // add the controller to the ControllerCanvas
      addChild(container)
      tmpContainers += container
    }
    containers = tmpContainers

    if (0 < oldHeights.length && 0 < containers.length) {
      for (i <- 0 to containers.length-1) {
        val j = ((oldHeights.length.toFloat / containers.length) * i).toInt
        containers(i).controller.height() = oldHeights(j)
      }
    }
  }

  /**
   * Only draws the line in the middle of the ControllerCanvas
   */
  def initializeBaseline {
    // initialize baseline
    val lineStart = new Vertex(Vec3d(0f, heightValue/2f))
    val lineEnd = new Vertex(Vec3d(widthValue, heightValue/2f))
    val baseline = new MTLine(app, lineStart, lineEnd)
    baseline.setStrokeColor(YELLOW)
    baseline.removeAllGestureEventListeners()
    addChild(baseline)
  }

  /**
   * This methods gets passed GestureEvents from controllers,
   * which don't want to process their DragEvents, because
   * the drag happens outside of them. In this case the
   * ControllerCanvas looks if there is another suitable
   * controller within itself. If not -> throw it away.
   * @param ge The GestureEvent. Handled like a DragEvent
   **/
  override def processGestureEvent(ge: MTGestureEvent): Boolean = {
    val drag = ge.asInstanceOf[DragEvent]
    if(containsPointGlobal(drag.getTo)) {
      getChildren.foreach( child => {
        if(child.containsPointGlobal(drag.getTo)) {
          child match {
            case container: ControllerContainer => {
              container.processGestureEvent(drag)
            }
            case controller: Controller => {
              controller.processGestureEvent(drag)
            }
            case _ =>
          }
        }
      })
    }
    true
  }

  /**
   * Activates the right controller (i.e. switches its color)
   * for a passed step.
   * @param step The step number to be activated
   * @return The height of the controller to be activated
   */
  def setStep(step: Int): Float = {
    var stepLength = null.asInstanceOf[Int]
    containers.synchronized {
      stepLength = Metronome.totalSteps/containers.length
    }
    var i = 1
    while (i*stepLength < step) {
      i += 1
    }
    if(activeController != null) {
      activeController.triggerActive
    }
    activeController = containers(i-1).controller
    activeController.triggerActive
    (-2f) * activeController.getHeight / height().asInstanceOf[Float]
  }

}
