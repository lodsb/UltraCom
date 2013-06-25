package ui

import org.mt4j.{Scene, Application}
import org.mt4j.components.MTComponent
import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.visibleComponents.widgets._
import org.mt4j.components.visibleComponents.shapes._
import org.mt4j.types.Vec3d

import org.mt4j.sceneManagement.AddNodeActionThreadSafe

import org.mt4j.input.IMTInputEventListener
import org.mt4j.input.inputSources.MacTrackpadSource

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor
import org.mt4j.input.inputProcessors.globalProcessors.RawFingerProcessor
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeProcessor
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.Direction

import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.inputData.MTFingerInputEvt
import org.mt4j.input.inputData.AbstractCursorInputEvt
import org.mt4j.input.inputData.InputCursor

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.types.Vec3d

import scala.actors.Actor._

import java.util.ArrayList

import org.lodsb.reakt.Implicits._

import ui.util._
import ui.input._
import ui.tools._
import ui.paths._
import ui.menus.main._
import ui.paths.types._
import ui.properties._
import ui.properties.types._


/**
* This is the sound application.
* All nodes and paths created by its users are organized by this object.
*/
object Ui extends Application { 
	// see Settings.txt for basic settings, e.g. application name, resolution, framerate...

  /**
  * Registers the specified node.
  */
  def +=(node: Node) = {
    this.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(node, this.getCurrentScene.getCanvas))
    println("nodeSet: " + this.nodes)
  }
  
  /**
  * Registers the specified path.
  */  
  def +=(path: Path) = {
    this.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(path, this.getCurrentScene.getCanvas))
    println("pathSet: " + this.paths)
  }
  
  def +=(component: MTComponent) = {
    this.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(component, this.getCurrentScene.getCanvas))
  }
 
   /**
  * Unregisters and destroys the specified node.
  */ 
  def -=(node: Node) = {
    this.getCurrentScene.registerPreDrawAction(new DeleteNodeActionThreadSafe(node))
  }
 
  /**
  * Unregisters and destroys the specified path.
  */  
  def -=(path: Path) = {
    this.getCurrentScene.registerPreDrawAction(new DeleteNodeActionThreadSafe(path))
  }
  
  def -=(component: MTComponent) = {
    this.getCurrentScene.registerPreDrawAction(new DeleteNodeActionThreadSafe(component))
  }
  
  def nodes = {
    this.getCurrentScene.getCanvas.getChildren.collect({case node: Node => node})
  }
  
  def paths = {
    this.getCurrentScene.getCanvas.getChildren.collect({case path: Path => path})
  }
	
	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		this.addScene(new UIScene(this, "Collaborative Sounddesign"))
		//getInputManager().registerInputSource(new MacTrackpadSource(this))
		/* Ctrl+N to set second point, shift to toggle it */
	}
	
}


/**
* This class represents the main UI developed in the context of this master's thesis.
*/
class UIScene(app: Application, name: String) extends Scene(app,name){
  
  var cPoint = (100.0f, 100.0f)
  var tPoint = (100.0f, 100.0f)
  
  def setClosestPoint(p: (Float, Float)) = {
    this.cPoint = p
  }
  
  def closestPoint = {
    this.cPoint
  }
  
  def setTipPoint(p: (Float, Float)) = {
    this.tPoint = p
  }
  
  def tipPoint = {
    this.tPoint
  }
  
  
  //this.showTracer(true) //show touches
  this.setup()
  
	
	private def setup() = {
	  this.setClearColor(Color(255,255,255))
	 
	  val timbreSpace = TimbreSpace(app)
    //val debug = DebugOutput
    
    this.canvas += timbreSpace
    //this.canvas += debug //be sure to add this after the space itself
    
    this.setupInteraction()
	}
	
	private def setupInteraction() = {	      
    //registering a global input processor for creating connections between nodes
    this.registerGlobalInputProcessor(new NodeConnectionProcessor(app))
    
    //registering a global input processor for showing the main menu at appropriate times
    this.registerGlobalInputProcessor(new MenuProcessor(app))
	  
    //setting up a component input processor on the canvas for taps and adding a corresponding listener
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    this.canvas.registerInputProcessor(tapProcessor)
    this.canvas.addGestureListener(classOf[TapProcessor], new NodeCreationListener())	  
    
    //setting up a component input processor on the canvas for unistroke gestures and adding a corresponding listener
    val unistrokeProcessor = new UnistrokeProcessor(app)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.CIRCLE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.CIRCLE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.DELETE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.DELETE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.TRIANGLE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.TRIANGLE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.PIGTAIL, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.PIGTAIL, UnistrokeUtils.Direction.CLOCKWISE)
    this.canvas.registerInputProcessor(unistrokeProcessor)
    this.canvas.addGestureListener(classOf[UnistrokeProcessor], new UnistrokeListener())
	}
	
}
