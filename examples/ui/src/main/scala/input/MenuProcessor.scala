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
      
      val angle = 
        if (position.getX < Menu.Space) 90f //left
        else if (position.getX > width - Menu.Space) 270f //right
        else if (position.getY < Menu.Space) 180f //top
        else /*if (position.getY > height - Menu.Space)*/ 0f //bottom
        
      Menu.calculateMenuPosition(position, angle).foreach(menuPosition => {
        this.addMenu(Menu(app, menuPosition, angle), inputCursor)
      })
    }
  }
  
  def addMenu(menu: Menu, inputCursor: InputCursor) = {
    this.cursorMap += (inputCursor -> menu)
    Menu += menu
    Ui += menu    
  }
  
}
