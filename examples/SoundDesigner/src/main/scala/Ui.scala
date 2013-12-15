package ui

import audio._
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
import org.mt4j.output.audio.AudioServer

import scala.actors.Actor._
import scala.actors.Scheduler
import scala.actors.scheduler.ResizableThreadPoolScheduler

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import java.util.ArrayList

import de.sciss.synth.{Node => SynthNode, _}
import ugen._
import org.mt4j.output.audio.AudioServer
import org.mt4j.output.audio.AudioServer._

import org.lodsb.reakt.Implicits._

import ui.util._
import ui.input._
import ui.tools._
import ui.paths._
import ui.menus.main._
import ui.paths.types._
import ui.properties._
import ui.properties.types._
import ui.persistence._
import ui.audio._
import ui.usability._


/**
* This is the sound application.
* All nodes and paths created by its users are organized by this object.
*/
object Ui extends Application with Persistability { 
	// see Settings.txt for basic settings, e.g. application name, resolution, framerate...

	private val aInterface = AudioInterface(new VPDTimbreSpace())

	def audioInterface = {
	  this.aInterface
	}
	
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
  
  def clearAll() = {
    this.nodes.foreach(node =>
      this.getCurrentScene.registerPreDrawAction(new DeleteNodeActionThreadSafe(node))
    )
    this.paths.foreach(path =>
      this.getCurrentScene.registerPreDrawAction(new DeleteNodeActionThreadSafe(path))
    )    
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
      if (paths.size > 1) {
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
      }
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
  
	
	override def toXML = {
    val paths = "<paths>" + this.paths.map(_.toXML).foldLeft("")((c1, c2) => c1 + " " + c2) + "</paths>"
    val nodes = "<nodes>" + this.nodes.map(_.toXML).foldLeft("")((n1, n2) => n1 + " " + n2) + "</nodes>"
    "<project>" + paths + nodes + "</project>"
	}
	
	
	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
    AudioServer.start(true)
		this.addScene(new UIScene(this, "Collaborative SoundDesigner"))
		//getInputManager().registerInputSource(new MacTrackpadSource(this))
		/* Ctrl+N to set second point, shift to toggle it */
	}
	
  /**
   * Handles key events.
   *
   * @param e The key event
   */
  override protected def handleKeyEvent(e: processing.event.KeyEvent) {
    if (keyPressed && keyCode == VK_ESCAPE) {
      AudioServer.quit // quit supercollider server scsynth
      Runtime.getRuntime.halt(0) // quit java runtime environment
    }
    super.handleKeyEvent(e)
  }	
	
	
}


/**
* This class represents the main UI developed in the context of this master's thesis.
*/
class UIScene(app: Application, name: String) extends Scene(app,name){
  
  //this.showTracer(true) //show touches
  val touchTracer = new TouchTracer(app, this)
  this.setup()     
	
	private def setup() = {
	  /* changing scheduler to avoid delay of new actors when max number of threads has already been reached */
	  System.setProperty("actors.enableForkJoin", "false") //disable default scheduler to avoid starvation when many actors are working
    Scheduler.impl = {
      val scheduler = new ResizableThreadPoolScheduler(false)
      scheduler.start()
      scheduler
    }
    
	  this.setClearColor(Color(255,255,255))
	 
	  val nodeSpace = NodeSpace(app)
    this.canvas += nodeSpace
    //this.canvas += DebugOutput //be sure to add this after the space itself
    
    
    //registering global input processors for path creation, feedforward and menu interaction
    this.registerGlobalInputProcessor(new CursorProcessor(app))
    this.registerGlobalInputProcessor(new FeedforwardProcessor(app))    
    this.registerGlobalInputProcessor(new MenuProcessor(app))
	  
    //setting up interaction on canvas, that is, global processors for tap and tap&hold
    this.registerGlobalInputProcessor(new NodeCreationListener(nodeSpace))

    //val tapAndHoldProcessor = new TapAndHoldProcessor(app, 300)
    this.registerGlobalInputProcessor(new ToolMenuListener(app))
    	  
    
    //setting up a component input processor on the canvas for unistroke gestures and adding a corresponding listener
    //val unistrokeProcessor = new UnistrokeProcessor(app)
    //unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.CIRCLE, UnistrokeUtils.Direction.CLOCKWISE)
    //unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.CIRCLE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    /*unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.DELETE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.DELETE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.TRIANGLE, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.TRIANGLE, UnistrokeUtils.Direction.CLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.PIGTAIL, UnistrokeUtils.Direction.COUNTERCLOCKWISE)
    unistrokeProcessor.addTemplate(UnistrokeUtils.UnistrokeGesture.PIGTAIL, UnistrokeUtils.Direction.CLOCKWISE) */
    //this.canvas.registerInputProcessor(unistrokeProcessor)
    //this.canvas.addGestureListener(classOf[UnistrokeProcessor], new UnistrokeListener())    
    
	}
	
}
