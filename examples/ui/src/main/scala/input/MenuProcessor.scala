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

import ui._
import ui.menus.main._
import ui.paths._


/**
* This class represents a global input processor for the menu of this application.
*/
class MenuProcessor(app: Application) extends AbstractGlobalInputProcessor[MTFingerInputEvt] {

  var cursorMap = Map[InputCursor, Menu]()
  
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
              //Ui -= menu
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
  
  def evaluateInteraction(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
    if (!this.cursorMap.contains(inputCursor)) { //if there is no corresponding menu, we create it         
      val position = Vec3d(cursorEvent.getScreenX, cursorEvent.getScreenY)
      val width = app.width
      val height = app.height
      
      if (position.getX < Menu.Space) { //left
        val vec = Vec3d(Menu.Space/2, position.getY)
        if (!Menu.isMenuInProximity(vec, 90.0f)) {
          this.addMenu(Menu(app, Vec3d(Menu.Space/2, position.getY), 90.0f), inputCursor)    
        }
      }
      else if (position.getX > width - Menu.Space) { //right
        val vec = Vec3d(width - Menu.Space/2, position.getY)
        if (!Menu.isMenuInProximity(vec, 270.0f)) {
          this.addMenu(Menu(app, Vec3d(width - Menu.Space/2, position.getY), 270.0f), inputCursor)
        }
      }
      else if (position.getY < Menu.Space) { //top
        val vec = Vec3d(position.getX, Menu.Space/2)
        if (!Menu.isMenuInProximity(vec, 180.0f)) {
          this.addMenu(Menu(app, Vec3d(position.getX, Menu.Space/2), 180.0f), inputCursor)
        }
      }
      else if (position.getY > height - Menu.Space) { //bottom
        val vec = Vec3d(position.getX, height - Menu.Space/2)
        if (!Menu.isMenuInProximity(vec, 0.0f)) {
          this.addMenu(Menu(app, Vec3d(position.getX, height - Menu.Space/2), 0.0f), inputCursor) 
        }
      }
    }
  }
  
  def addMenu(menu: Menu, inputCursor: InputCursor) = {
    this.cursorMap += (inputCursor -> menu)
    Menu += menu
    Ui += menu    
  }
  
}
