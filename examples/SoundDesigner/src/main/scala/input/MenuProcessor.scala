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

import org.mt4j.components.interfaces.IMTComponent3D
import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.MTCanvas
import org.mt4j.components.TransformSpace

import org.mt4j.types.Vec3d

import org.lodsb.reakt.Implicits._

import scala.actors._

import ui._
import ui.menus.main._
import ui.events._
import ui.paths._
import ui.util._


/**
* This class represents a global input processor for the menu of this application.
*/
class MenuProcessor(app: Application) extends AbstractGlobalInputProcessor[MTFingerInputEvt] with Actor {

  var cursorMap = Map[InputCursor, Menu]()
  private var tapList = List[CustomCursorEvent]() //list for keeping track of cursor events, that is, their position and the time at which they occurred
  private val TapAndHoldMaxDist = 5 //maximum distance between the start and end point of a tap and hold touch event
  private val TapAndHoldTime = 400 //time in milliseconds before a tap and hold is triggered
  this.start()
  this ! "CHECK"  

	def processInputEvtImpl(inputEvent: MTFingerInputEvt) {
	  if (inputEvent.getTarget == Ui.getCurrentScene.getCanvas){ //only process event if the user is not interacting with another component, i.e. only the canvas is touched
      inputEvent match {
        case cursorEvent: AbstractCursorInputEvt => {
          //val id = cursorEvent.getCursor.getId
          //val x = cursorEvent.getPosition.getX
          //val y = cursorEvent.getPosition.getY
          val time = System.nanoTime() 
          if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED) { //DETECTED is officially deprecated and should read STARTED but local MT4J code does not correspond with online API, that is, there is no INPUT_STARTED         
            this ! CustomCursorEvent(cursorEvent, time, CustomCursorEvent.Started)
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED) {
            this ! CustomCursorEvent(cursorEvent, time, CustomCursorEvent.Updated)
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
            this ! CustomCursorEvent(cursorEvent, time, CustomCursorEvent.Ended)
          }
        }
        case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
        }
      }
    }
  } 

  /*
	def processInputEvtImpl(inputEvent: MTFingerInputEvt) {
	  if (inputEvent.getTarget == Ui.getCurrentScene.getCanvas){ //only process event if the user is not interacting with another component, i.e. only the canvas is touched
      inputEvent match {
        case cursorEvent: AbstractCursorInputEvt => {
          if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED) { //DETECTED is officially deprecated and should read STARTED but local MT4J code does not correspond with online API, that is, there is no INPUT_STARTED         
            this.evaluateInteraction(cursorEvent)
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED) {
            this.evaluateInteraction(cursorEvent)
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
            val inputCursor = cursorEvent.getCursor()
            if (this.cursorMap.contains(inputCursor)) { 
              val menu = this.cursorMap(inputCursor)
              this.cursorMap -= inputCursor
            }
          }
          true
        }
        case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
          false
        }
      }
    }
  }
  */
  
  def evaluateInteraction(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor
    //if (!this.cursorMap.contains(inputCursor)) { //if there is no corresponding menu, we create it         
      val position = Vec3d(cursorEvent.getScreenX, cursorEvent.getScreenY)
      val width = app.width
      val height = app.height
      
      val angle = 
        if (position.getX < Menu.Space) 90f //left
        else if (position.getX > width - Menu.Space) 270f //right
        else if (position.getY < Menu.Space) 180f //top
        else if (position.getY > height - Menu.Space) 0f //bottom
        else -1f //do not instantiate a menu if the input cursor is not located in the menu space
        
      if (angle != -1f) {  
        Menu.calculateMenuPosition(position, angle).foreach(menuPosition => {
          this.addMenu(Menu(app, menuPosition, angle), inputCursor)
        })
      }
    //}
  }
  
  def addMenu(menu: Menu, inputCursor: InputCursor) = {
    this.cursorMap += (inputCursor -> menu)
    Menu += menu
    Ui += menu    
  }
  
  
  def act() = {
    while (true) {
      receive {
        case "CHECK" => {
          var completedTaps = List[CustomCursorEvent]()
          this.tapList.foreach(event => {
            if ((System.nanoTime() - event.time)/1000000.0f >= TapAndHoldTime) { //tap and hold completed
              //println("tap and hold completed")
              completedTaps = event :: completedTaps
              this.evaluateInteraction(event.cursorEvent)
            }
          })
          Thread.sleep(30)
          this.tapList = this.tapList.diff(completedTaps)    
          //println("tapList: " + this.tapList)
          if (!this.tapList.isEmpty) this ! "CHECK"
        }
        case event: CustomCursorEvent => {
          //println("custom cursor event incoming...")
          if (event.eventType == CustomCursorEvent.Started) {
            this.tapList = event :: this.tapList
            this ! "CHECK"
          }
          else if (event.eventType == CustomCursorEvent.Updated) { 
            val entryOption = this.tapList.find(_.cursorEvent.getCursor.getId == event.cursorEvent.getCursor.getId) //look if an event with this cursor id exists
            entryOption.foreach(entry => { 
              val eventPos = event.cursorEvent.getCursor.getPosition
              val entryPos = entry.cursorEvent.getCursor.getPosition
              if (Vector.euclideanDistance((eventPos.getX, eventPos.getY), (entryPos.getX, entryPos.getY)) > TapAndHoldMaxDist) { //if the cursor has moved too much
                this.tapList = this.tapList.filter(_.cursorEvent.getCursor.getId != entry.cursorEvent.getCursor.getId) //we remove the event since a tap and hold cannot occur any longer
              }
            })
          }          
          else if (event.eventType == CustomCursorEvent.Ended) {
            this.tapList = this.tapList.filter(_.cursorEvent.getCursor.getId != event.cursorEvent.getCursor.getId) //remove entries belonging to the given cursor id
          }
        }
      }
    }
  }    
  
}
