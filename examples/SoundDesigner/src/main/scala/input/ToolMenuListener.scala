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

import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent

import org.mt4j.components.ComponentImplicits._
import org.mt4j.types.Vec3d
import org.mt4j.util.SessionLogger

import scala.actors._

import ui._
import ui.paths._
import ui.events._
import ui.util._
import ui.menus.context._

class ToolMenuListener(app: Application) extends AbstractGlobalInputProcessor[MTFingerInputEvt] with Actor {

  private var tapList = List[CustomTapEvent]() //list for keeping track of touch events, that is, their position and the time at which they occurred
  private val TapAndHoldMaxDist = 5 //maximum distance between the start and end point of a tap and hold touch event
  private val TapAndHoldTime = 600 //time in milliseconds before a tap and hold is triggered
  this.start()
  this ! "CHECK"
  
	def processInputEvtImpl(inputEvent: MTFingerInputEvt) {
	  if (inputEvent.getTarget == Ui.getCurrentScene.getCanvas){ //only process event if the user is not interacting with another component, i.e. only the canvas is touched
      inputEvent match {
        case cursorEvent: AbstractCursorInputEvt => {
          val id = cursorEvent.getCursor.getId
          val x = cursorEvent.getPosition.getX
          val y = cursorEvent.getPosition.getY
          val time = System.nanoTime() 
          if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED) { //DETECTED is officially deprecated and should read STARTED but local MT4J code does not correspond with online API, that is, there is no INPUT_STARTED         
            this ! CustomTapEvent(id, x, y, time, CustomTapEvent.Started)
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED) {
            this ! CustomTapEvent(id, x, y, time, CustomTapEvent.Updated)
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
            this ! CustomTapEvent(id, x, y, time, CustomTapEvent.Ended)
          }
        }
        case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
        }
      }
    }
  }  

  def act() = {
    while (true) {
      receive {
        case "CHECK" => {
          var completedTaps = List[CustomTapEvent]()
          this.tapList.foreach(event => {
            if ((System.nanoTime() - event.time)/1000000.0f >= TapAndHoldTime) { //tap and hold completed
              println("tap and hold completed")
              if (!ToolContextMenu.isMenuInProximity(Vec3d(event.x, event.y)) && ToolContextMenu.isMenuInBounds(app, Vec3d(event.x, event.y)) && !CursorProcessor.isCursorInUse(event.id)) {
                completedTaps = event :: completedTaps
                val menu = ToolContextMenu(app, Vec3d(event.x, event.y))
                SessionLogger.log("Created: Tool menu",SessionLogger.SessionEvent.Event, this, null, null)
                ToolContextMenu += menu
                Ui += menu      
              }
            }
          })
          Thread.sleep(30)
          this.tapList = this.tapList.diff(completedTaps)    
          if (!this.tapList.isEmpty) this ! "CHECK"
        }
        case event: CustomTapEvent => {
          if (event.eventType == CustomTapEvent.Started) {
            this.tapList = event :: this.tapList
            this ! "CHECK"
          }
          else if (event.eventType == CustomTapEvent.Updated) {
            val entryOption = this.tapList.find(_.id == event.id) //look if an event with this cursor id exists
            entryOption.foreach(entry => { 
              if (Vector.euclideanDistance((event.x, event.y), (entry.x, entry.y)) > TapAndHoldMaxDist) { //if the cursor has moved too much
                this.tapList = this.tapList.filter(_.id != entry.id) //we remove the event since a tap and hold cannot occur any longer
              }
            })
          }          
          else if (event.eventType == CustomTapEvent.Ended) {
            this.tapList = this.tapList.filter(_.id != event.id) //remove entries belonging to the given cursor id
          }
        }
      }
    }
  }  
  
}
