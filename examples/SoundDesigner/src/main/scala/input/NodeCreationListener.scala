package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor
import org.mt4j.input.inputProcessors.globalProcessors.RawFingerProcessor
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 

import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.inputData.MTFingerInputEvt
import org.mt4j.input.inputData.AbstractCursorInputEvt
import org.mt4j.input.inputData.InputCursor

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.types.Vec3d
import org.mt4j.components.ComponentImplicits._

import ui._
import ui.paths._
import ui.paths.types._
import ui.util._
import ui.events._

/**
* This class handles the creation of nodes in the specified node space.
*/
class NodeCreationListener(nodeSpace: NodeSpace) extends AbstractGlobalInputProcessor[MTFingerInputEvt] {

  private var tapList = List[CustomTapEvent]() //list for keeping track of touch events, that is, their position and the time at which they occurred
  private val DoubleTapMaxDist = 5 //maximum distance between the two taps of a double tap
  private val DoubleTapMaxTime = 300 //maximum time in milliseconds between the two taps of a double tap
  
	def processInputEvtImpl(inputEvent: MTFingerInputEvt) {
	  if (inputEvent.getTarget == Ui.getCurrentScene.getCanvas){ //only process event if the user is not interacting with another component, i.e. only the canvas is touched
      inputEvent match {
        case cursorEvent: AbstractCursorInputEvt => {
          if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED) { //DETECTED is officially deprecated and should read STARTED but local MT4J code does not correspond with online API, that is, there is no INPUT_STARTED         
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED) {
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
            val id = cursorEvent.getCursor.getId
            val x = cursorEvent.getPosition.getX
            val y = cursorEvent.getPosition.getY
            val time = System.nanoTime()
            val eventOption = this.tapList.find(event => Vector.euclideanDistance((event.x, event.y), (x,y)) < DoubleTapMaxDist && (System.nanoTime() - event.time)/1000000.0f < DoubleTapMaxTime)
            eventOption match {
              case Some(event) => this.evaluateDoubleTap(x,y)
              case None => this.tapList = CustomTapEvent(id,x,y,time,CustomTapEvent.Ended) :: this.tapList
            }
            this.tapList = this.tapList.filter(event => (System.nanoTime() - event.time)/1000000.0f <= DoubleTapMaxTime) //get rid of 'old' events
          }
        }
        case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
        }
      }
    }
  }
  
  
  private def evaluateDoubleTap(x: Float, y: Float) = {
    //println("double tap is being evaluated")
    val position = (x,y)
    val posVec = Vec3d(x,y)
    val closestPathPositionOption = Ui.closestPath(position)
    closestPathPositionOption match {
      case Some(closestPathPosition) => {
        val path = closestPathPosition._1
        val connection = closestPathPosition._2
        val parameter = closestPathPosition._3
        val pathPosition = connection(parameter)
        if (Vector.euclideanDistance(pathPosition, position) <= 10) {
          path ! TimeNodeAddEvent(TimeNode(Ui, closestPathPosition))
        }
        else {
          if (nodeSpace.contains(posVec)) Ui += SingleNode(Ui, posVec)    
        }
      } 
      case None => {
        if (nodeSpace.contains(posVec)) Ui += SingleNode(Ui, posVec)
      }       
    } 
  }
  
  
}
