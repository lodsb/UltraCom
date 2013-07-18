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
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
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
  * Registers the specified component threadsafe.
  */  
  def +=(component: MTComponent) = {
    this.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(component, this.getCurrentScene.getCanvas))
  }
  
  /**
  * Unregisters and destroys the specified component threadsafe.
  */
  def -=(component: MTComponent) = {
    this.getCurrentScene.registerPreDrawAction(new DeleteNodeActionThreadSafe(component))
  }
  
  def nodes = {
    this.getCurrentScene.getCanvas.getChildren.collect({case node: Node => node})
  }
  
  def paths = {
    this.getCurrentScene.getCanvas.getChildren.collect({case path: Path => path})
  }
  
  
	/**
	* Returns - as an Option - the closest path, the closest connection on that path and the curve parameter yielding the closest point on that connection to the specified point,
	* or None if there are no paths.
	*
	*/
	def closestPath(point: (Float, Float)): Option[(Path, Connection, Float)] = {
	  val paths = this.paths
	  if (paths.size > 0) {
      var closestPath = paths.head /* set first path as initial closest path */
      var (closestConnection, argminParameter) = closestPath.closestSegment(point._1, point._2)
      var minDist = Vector.euclideanDistance(closestConnection(argminParameter), point)
      paths.tail.foreach(path => { //for all the other paths in the list of paths
        val (connection, parameter) = path.closestSegment(point._1, point._2) //get the point on the path that is closest to the specified coordinate as well as the segment of the path
        val dist = Vector.euclideanDistance(connection(parameter), point)
        if (minDist > dist){ //compare its distance to the current min distance
          closestPath = path
          closestConnection = connection
          argminParameter = parameter
          minDist = dist //and update if necessary
        }
      })	  
      Some((closestPath, closestConnection, argminParameter))
    }
    else None
	}
         
	/**
	* Returns - as an Option - out of the given collection of nodes the one closest to the specified point, or None if the array is empty.
	*
	* @throws NoSuchElementException if the collection is empty
	*/	
	def closestNode(point: (Float, Float), nodeCandidates: Iterable[Node]): Option[Node] = {
	  if (nodeCandidates.size > 0) {	    
      var closestNode = nodeCandidates.head /* set first node as initial closest node */
      var minDist = Vector.euclideanDistance(closestNode.position, point)
      nodeCandidates.tail.foreach(node => { //for all the other nodes in the list of manipulable nodes
        val dist = Vector.euclideanDistance(node.position, point)
        if (minDist > dist){ //compare its distance to the current min distance
          closestNode = node
          minDist = dist //and update if necessary
        }
      })	  
      Some(closestNode)
    }
    else None
	}  
	
	
	/**
	* Returns - as an Option - the node closest to the specified point, or None if there is no node.
	*
	* @throws NoSuchElementException if there are no nodes
	*/	
	def closestNode(point: (Float, Float)): Option[Node] = {
	  this.closestNode(point, this. nodes)
	}  	
	
	
	/**
	* Returns - as an Option - out of the manipulable nodes the one closest to the specified point, or None if there is no manipulable node.
	*
	* @throws NoSuchElementException if there are no manipulable nodes
	*/		
	def closestManipulableNode(point: (Float, Float)): Option[Node] = {
    val manipulableNodes = this.nodes.collect({case node: ManipulableNode => node})
    this.closestNode(point, manipulableNodes)
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
  
  //this.showTracer(true) //show touches
  this.setup()
  
	
	private def setup() = {
	  System.setProperty("actors.enableForkJoin", "false") //disable default scheduler to avoid starvation when many actors are working
	  this.setClearColor(Color(255,255,255))
	 
	  val timbreSpace = TimbreSpace(app)
    this.canvas += timbreSpace
    //this.canvas += DebugOutput //be sure to add this after the space itself
    
    
    //registering a global input processor for creating connections between nodes
    this.registerGlobalInputProcessor(new CursorProcessor(app))
    
    //registering a global input processor for feedforward
    this.registerGlobalInputProcessor(new FeedforwardProcessor(app))    
    
    //registering a global input processor for showing the main menu at appropriate times
    this.registerGlobalInputProcessor(new MenuProcessor(app))
	  
    //setting up a component input processor on the canvas for taps and adding a corresponding listener
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    this.canvas.registerInputProcessor(tapProcessor)
    this.canvas.addGestureListener(classOf[TapProcessor], new NodeCreationListener(timbreSpace))

    val tapAndHoldProcessor = new TapAndHoldProcessor(app, 200)
    tapAndHoldProcessor.setMaxFingerUpDist(5) 
    this.canvas.registerInputProcessor(tapAndHoldProcessor)
    this.canvas.addGestureListener(classOf[TapAndHoldProcessor], new ToolMenuListener(app))
    	  
    
    //setting up a component input processor on the canvas for unistroke gestures and adding a corresponding listener
    val unistrokeProcessor = new UnistrokeProcessor(app)
    //unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.CIRCLE, UnistrokeUtils.Direction.CLOCKWISE)
    //unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.CIRCLE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    /*unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.DELETE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.DELETE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.TRIANGLE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.TRIANGLE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.PIGTAIL, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.PIGTAIL, UnistrokeUtils.Direction.CLOCKWISE) */
    this.canvas.registerInputProcessor(unistrokeProcessor)
    this.canvas.addGestureListener(classOf[UnistrokeProcessor], new UnistrokeListener())    
    
	}
	
}
