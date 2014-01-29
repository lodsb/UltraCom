package de.ghagerer.FugueGenerator

import org.mt4j.components.visibleComponents.shapes.{MTLine, MTRectangle}
import org.mt4j.types.Vec3d
import org.mt4j.util.MTColor._
import org.mt4j.util.math.Vertex
import org.mt4j.input.inputProcessors.{IInputProcessor, MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import scala.collection.mutable.ArrayBuffer
import org.lodsb.reakt.sync.VarS
import org.mt4j.util.SessionLogger
import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.inputProcessors.MTGestureEvent._
import org.mt4j.util.SessionLogger.SessionEvent

/**
 * This is the class, which contains the pitch-controllers
 */
class ControllerCanvas(val widthValue: Float, val heightValue: Float, howMany: Int)
  extends MTRectangle(app, widthValue, heightValue) with IGestureEventListener {

  // -- attributes

  // this value tells how much the canvas was rotated
  var degrees = 0

  // rotation/orientation factor
  var signum = -1

  // initialize sound/synthesizer for this canvas
  val synthi = new Synthi()


  // containers are all ControllerContainers. Needed for their sequential ordering
  var containers = null.asInstanceOf[ArrayBuffer[ControllerContainer]]
  var activeController = null.asInstanceOf[Controller]


  // -- initialization

  setStrokeColor(BLUE)
  setFillColor(app.TRANSPARENT)
  initializeBaseline
  initializeControllers(howMany)


  // -- methods

  /**
   * Adds some controllers to this canvas. Every with equal width.
   * @param howMany Int How many controllers should be initialized
   */
  def initializeControllers(howMany: Int) {

    howMany match {
      case 2 =>
      case 4 =>
      case 8 =>
      case 16 =>
      case _ => return
    }

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

    synthi.timeSignature() = containers.size.toFloat

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

  def logDragEvent(event: DragEvent) {
    event.getId match {
      case GESTURE_DETECTED =>
        SessionLogger.log("Started drag gesture at "+event.getFrom, SessionEvent.BeginGesture, this, null, null)
      case GESTURE_ENDED =>
        SessionLogger.log("Stopped drag gesture at "+event.getTo, SessionEvent.EndGesture, this, null, null)
      case GESTURE_UPDATED =>
        SessionLogger.log("Updated drag gesture at "+event.getFrom, SessionEvent.Event, this, null, null)
      case _ =>
    }
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

    logDragEvent(drag)

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
   * @param step The step number to be activated (step/16th)
   * @return The height of the controller to be activated
   */
  def playNext(step: Int, really: Boolean = true): Boolean = {

    // find the number of the new active controller
    var stepLength = 0
    containers.synchronized {
      stepLength = Metronome.totalSteps/containers.length

        // Logging (disgusting hack) , taking place every bar
        if(step == stepLength -1) {

          var min = Float.MaxValue
          var max = Float.MinValue
          var avgHeight = 0.0
          var avgHeightDistance = 0.0
          var oldHeight = containers.last.controller.getHeight
          var newHeight = containers(0).controller.getHeight

          containers.foreach({ container =>

            oldHeight = newHeight
            newHeight = container.controller.getHeight

            if(min  > newHeight) min = newHeight
            if(max  < newHeight) max = newHeight

            avgHeight += newHeight
            avgHeightDistance += (newHeight - oldHeight)

          })

          avgHeight = avgHeight / containers.size

          SessionLogger.log("Steps ", SessionLogger.SessionEvent.Event, this, this, (min, max, avgHeight, avgHeightDistance, stepLength))
      }
    }
    var i = 1
    while (i*stepLength < step) {
      i += 1
    }

    if (!decide(step, containers.length)) return false
    //if (!really) return true

    // if the same controller is still active -> exit
    val tmp = containers(i-1).controller
    if (tmp eq activeController) return false


    // make the current activeController visibly inactive
    if (activeController != null) {
      activeController.triggerActive
    }

    // replace the current activeController with the following one
    activeController = tmp

    // make the new activeController visibly active
    activeController.triggerActive

    // get the height of the new activeController as percent
    val height = (-1f) * activeController.getHeight / (this.height().toFloat/2f)

    val toneDuration = Metronome.duration() //* stepsToNextController(step)


    synthi.play(height, toneDuration)

    true
  }

  /**
   * this method contains the logic for rhythm complexity (~arousal/activity)
   * if arousal/activity for this ControllerCanvas instance is high the regarding rhythm complexity is also high
   * high rhythm complexity means high meanWnbd value
   * this method is a decision maker, whether this ControllerCanvas should play a sound on the regarding 32th beat or not
   * for beat logic see Metronome.scala respectively Metronome- and counter-object
   * @param step actual 32th beat
   * @param timeSignature essentially how many controllers are within the actual ControllerCanvas
   * @return Boolean: yes or no
   */
  private def decide(step: Int, timeSignature: Int): Boolean = {
    val act: Double =
    if (synthi.activity() == 1d) {
      0.2499999999
    } else {
      synthi.activity() % 0.25
    }
    val valence = synthi.valence()
    timeSignature match {
      case  4 => {
        val complexity = math.round((act * 8).toInt * valence)
        if (
          complexity == 0 ||
          complexity == 1 && (step == 1 || step == 17 || step == 13 || step == 29)
        ) {return true}
      }
      case  8 => {
        val complexity = math.round((act * 12).toInt * valence)
        if (
          ((step-1)/4)%2 == 0 ||
          //step == 1 2 3 4  9 10 11 12  17 18 19 20  25 26 27 28
          complexity == 0 ||
          complexity == 1 && (step == 5 || step == 15 || step == 21 || step == 31) ||
          complexity == 2 && (step == 7 || step == 15 || step == 23 || step == 31)
        ) {return true}
      }
      case 16 => {
        val complexity = math.round((act * 16).toInt * valence)
        println("- complexity="+complexity+", activity="+act)
        if (
          ((step-1)/2)%2 == 0 ||
          //step == 1 2  5 6  9 10  13 14  17 18  21 22  25 26  29 30
          (complexity == 0 && valence != 1) ||
          complexity == 1 && (step == 3 || step == 7 || step == 11 || step == 16 || step == 19 || step == 23 || step == 27 || step == 32) ||
          complexity == 2 && (step == 3 || step == 8 || step == 11 || step == 16 || step == 19 || step == 24 || step == 27 || step == 32) ||
          (complexity == 3 || synthi.activity() == 1) && (step == 4 || step == 8 || step == 12 || step == 16 || step == 20 || step == 24 || step == 28 || step == 32)
        ) {return true}
      }
      case  _ => return true
    }
    false
  }

  private def stepsToNextController(actualStep: Int): Int = {
    var i = 0
    try {
      while (!playNext(step = actualStep+i, really = false)) {
        i += 1
      }
    } catch { case e: IndexOutOfBoundsException => {
      var j = 0
      while (!playNext(step = j, really = false)) {
        i += 1
        j += 1
      }
    }}
    i
  }


  def rotate180 {
    degrees += 180
    rotateZ(app.center, 180f)
    signum *= -1
  }
  def rotate90 {
    degrees += 90
    rotateZ(app.center, 90f)
    //signum *= -1
  }

  /**
   * set the activity respective the arousal
   * @param value a float-value between 0 and 1
   */
  def activity(value: Float) {
    // reset the number of controllers corresponding to arousal value
    val numberOfControllers = math.pow(2,1+(value / 0.25).toInt).toInt
    initializeControllers(numberOfControllers)

    // pass the arousal value to the SC synthesizer
    synthi.activity() = value
}

  /**
   * sets valence for synthesis
   * happy = 0, sad = 1
   */

  def valence(value: Float) = {
    if(synthi != null) {
      synthi.valence() = value
    }
  }

}
