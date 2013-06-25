package ui.paths

import org.mt4j.Application

import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.visibleComponents.AbstractVisibleComponent

import org.mt4j.sceneManagement.AddNodeActionThreadSafe

import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.InertiaDragAction

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.Vec3d

import processing.core.PGraphics

import scala.actors._

import ui._
import ui.paths.types._
import ui.util._
import ui.events._
import ui.audio._
import ui.properties.types._
import ui.menus.main._



abstract class PlaybackState{}
object Playing extends PlaybackState{}
object Paused extends PlaybackState{}
object Stopped extends PlaybackState{}


object Path {
  
  /**
  * Constructs a path with a variable number of connections between nodes.
  */
  def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => Connection), connections: List[Connection]) = {
    new Path(app, defaultConnectionFactory, connections, Stopped, 0, 0.0f, 0.0f, 0, 0.0f)
  }
  
  /**
  * Constructs a path with a single connection between the specified nodes.
  */
  def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => Connection), firstNode: Node, secondNode: Node) = {
    new Path(app, defaultConnectionFactory, List(defaultConnectionFactory(app, firstNode, secondNode)), Stopped, 0, 0.0f, 0.0f, 0, 0.0f)
  }
  
  /**
  * Constructs a path with a variable number of connections between nodes and a given playback position.
  */
  def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => Connection), connections: List[Connection], playback: PlaybackState, currentCon: Int, connectionAcc: Float, currentConParam: Float, currentBuck: Int, bucketAcc: Float) = {
    new Path(app, defaultConnectionFactory, connections, playback, currentCon, connectionAcc, currentConParam, currentBuck, bucketAcc)
  }
  
  /**
  * Returns the center between two nodes.
  */
  def center(firstNode: Node, secondNode: Node) = {
    val center1 = firstNode.getCenterPointGlobal
    val center2 = secondNode.getCenterPointGlobal
    new Vector3D((center1.getX+center2.getX)/2, (center1.getY+center2.getY)/2)
  }

}


/**
* This class represents a path through the timbre space, that is, a sequence of connections between nodes.
*
* Note that while there is no theoretical - if practical - limit to the number of connections which can make up a path,
* every path consists of at least one connection.
*
*/ 
class Path(app: Application, defaultConnectionFactory: ((Application, Node, Node) => Connection), var connections: List[Connection], 
           playback: PlaybackState, currentCon: Int, connectionAcc: Float, currentConParam: Float, currentBuck: Int, bucketAcc: Float) extends AbstractVisibleComponent(app) with Actor {
 
  private var exists = true
  private var playbackState = playback //whether this path is currently played back, paused or stopped
  private var currentConnection = currentCon //index of the connection currently played 
  private var currentBucket = currentBuck //index of the current speed bucket
  private var bucketAccumulator = bucketAcc //accumulate passed time in milliseconds for current bucket
  private var connectionAccumulator = connectionAcc //accumulated passed time in milliseconds for the current connection
  private var currentConnectionParameter = currentConParam //connection parameter indicating the playback progress on the currently played back connection
      
  this.setup()
  
  /*
  Note: Do not declare this a case class since those should be immutable.
  
  Moreover, the methods equals and hashCode which are implicitly defined by case classes 
  recursively call equals/hashCode on their respective fields.
  If those fields have types which themselves are case classes and which reference this class
  (which is the case here given that paths reference nodes and nodes reference paths)
  this will lead to StackoverflowErrors, which is bad ;)
  */ 
  
 
  private def setup() = {
    connections.foreach(_.nodes.foreach(_.associatedPath = Some(this))) //set associated path to this for all connected nodes
    connections.head.nodes.head.nodeType = if (this.playbackState == Playing) PauseNodeType else PlayNodeType //set start...
    connections.last.nodes.last.nodeType = StopNodeType //...and end node of this path
    connections.foreach(connection => Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(connection, this))) //add connections as children
    this.start() //start acting
    if (this.playbackState == Playing) {
      this ! UiEvent("PLAY") //continue playing if state dictates so
      Playback ! PathPlaybackEvent(this, true)
    }
    println("exiting setup")
  }  
    

  def act = {

    /**
    * Returns the manipulable bezier connections.
    */
    def manipulableConnections: List[ManipulableBezierConnection] = {
      this.connections collect {case c: ManipulableBezierConnection => c}
    }            
    
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()
    var timeDiff = 0.0f //passed time in milliseconds
    
    var currentBucketValue = 0.0f
    var currentConnectionValue = 0.0f

    var (currentX, currentY) = (0, 0)

    while (this.exists) {
      receive {
        
        case event: NodeDeletionEvent => {
          this -= event.node
        }
        
        case event: NodeAppendEvent => {
          this += event.node
        }
        
        case event: NodePrependEvent => {
          event.node +=: this
        }
        
        case event: PathAppendEvent => {
          this ++= event.path
        }
        
        case event: NodeMoveEvent => { //if a node was moved, update the affected connections
          val affectedConnections = this.connections.filter(_.nodes.exists(_ == event.node)) collect {case c: ManipulableBezierConnection => c}
          affectedConnections.foreach(_.updateCurveParameters())
        }
        
        case event: PathManipulationEvent => {
          event.connection.updateProperty(event.propertyType, event.connectionParameter, event.manipulationRadius, event.value) 
          if (event.propertyType == SpeedPropertyType) { //if the speed property has been changed
            this.connectionAccumulator = event.connection.partialPropertySum(SpeedPropertyType, this.currentBucket-1) + this.bucketAccumulator 
            /* then we need to recalculate the connectionAccumulator since we don't want for manipulations to affect the playback position on the arc */
          }
        }     
        
        case event: PathFastForwardEvent => {
          val time = event.time
        }
        
        case event: PathRewindEvent => {
          val time = event.time
        }
        
        case event: UiEvent => { //a 'simple' ui event
          if (event.name == "START_PLAYBACK") {
            if (this.playbackState != Playing) {
              lastTime = System.nanoTime() //init time
              this.connections.head.nodes.head.nodeType = PauseNodeType
              this.playbackState = Playing
              Playback ! PathPlaybackEvent(this, true)
              this ! UiEvent("PLAY")
            }
          }
          
          else if (event.name == "PLAY") {
            if (this.playbackState == Playing) {
              currentTime = System.nanoTime()
              timeDiff = (currentTime - lastTime)/1000000.0f //passed time in milliseconds
              lastTime = currentTime
              
              val connections = manipulableConnections //get connections 
              val con = connections(this.currentConnection)
              val buckets = con.propertyBuckets(SpeedPropertyType) //get number of buckets              
              this.bucketAccumulator = this.bucketAccumulator + timeDiff //accumulate passed time for current bucket
              this.connectionAccumulator = this.connectionAccumulator + timeDiff //and current connection
              currentBucketValue = con.propertyValue(SpeedPropertyType, this.currentBucket)
              currentConnectionValue = con.propertySum(SpeedPropertyType)
              
              if (this.bucketAccumulator >= currentBucketValue) { //if the time specified by the bucket has been surpassed
                this.currentBucket = this.currentBucket + 1 //we process the next current bucket of the current connection //(connectionAccumulator/currentConnectionValue * buckets).toInt //
                this.bucketAccumulator = this.bucketAccumulator - currentBucketValue //and set back the bucket accumulator with carry-over 
                if (this.currentBucket >= buckets) { //if we processed all buckets of the current connection
                  this.currentConnection = this.currentConnection + 1 //we process the next connection on the path
                  this.currentBucket = 0 //and set back the current bucket variable
                  this.connectionAccumulator = this.bucketAccumulator //as well as the connection accumulator, again accounting for carry-over
                  if (this.currentConnection >= connections.size) { //if we have processed all connections on the path
                    this.resetPlayback()
                    Playback ! PathPlaybackEvent(this, false)
                  }
                }
              } 
              
              currentConnectionParameter = con.toCurveParameter(this.currentBucket/buckets.toFloat + (this.bucketAccumulator/currentBucketValue)/buckets)    
              val (newXFloat, newYFloat) = con(currentConnectionParameter)
              val (newX, newY) = (math.round(newXFloat), math.round(newYFloat))
                
              if (newX != currentX || newY != currentY){
                currentX = newX
                currentY = newY
                Synthesizer ! AudioEvent(0, currentX, currentY, con.propertyValue(PitchPropertyType, this.currentBucket), con.propertyValue(VolumePropertyType, this.currentBucket))
              }   
              
              this ! event //keep playing until the playback is either stopped/paused or the path has been played back
            }              
          }
          
          else if (event.name == "PAUSE_PLAYBACK") {
            if (this.playbackState != Paused) {
              this.connections.head.nodes.head.nodeType = PlayNodeType             
              this.playbackState = Paused
              Playback ! PathPlaybackEvent(this, false)
            }
          }
          
          else if (event.name == "STOP_PLAYBACK") {
            this.resetPlayback()
            Playback ! PathPlaybackEvent(this, false)
          }
          
          else println("OH NO!")
        }
      }
    }
  }    
  
  private def resetPlayback() = {
    this.currentConnection = 0 //we set back the connection...
    this.currentBucket = 0 //the bucket...
    this.connectionAccumulator = 0.0f
    this.bucketAccumulator = 0.0f //...and the accumulators
    this.currentConnectionParameter = 0.0f
    this.connections.head.nodes.head.nodeType = PlayNodeType //then we set back the node type
    this.playbackState = Stopped
  }
  
  /**
  * Returns the current playback position of this path, that is, 
  * the index of the currently played back connection and the curve parameter yielding the current playback point on that connection.
  */
  def playbackPosition: (Int, Float) = {
    (this.currentConnection, this.currentConnectionParameter)
  }
   
  
  /**
  * Returns the time in milliseconds it takes to play back this path once.
  */
  def time: Float = {
    this.connections.collect({case c: ManipulableBezierConnection => c}).foldLeft(0.0f)((sum, con) => sum + con.propertySum(SpeedPropertyType))
  }
  
  /**
  * Returns the time in milliseconds it takes to complete the playback of this path.
  */ 
  def timeLeft: Float = {
    val timePassed = this.connections.take(this.currentConnection + 1).collect({case c: ManipulableBezierConnection => c}).foldLeft(0.0f)((sum, con) => sum + con.propertySum(SpeedPropertyType)) + this.connectionAccumulator
    this.time - timePassed
  }
  
  /**
  * Returns the current state of this path, which is either playing, paused or stopped.
  */
  def state = {
    this.playbackState
  }
  
  /**
  * Returns the index of the specified connection, or -1 if the connection is not part of this path.
  */
  def indexOf(connection: Connection) = {
    this.connections.indexOf(connection)
  }


  /**
  * Draws this path.
  */
  override def drawComponent(g: PGraphics) = {
    //connections are drawn automatically due to parent-child-relationship
    //thus this method is only needed if the path is to be decorated in some way - tbd
  }   
  
  /**
  * Returns the distance of the specified coordinate from this path.
  * Not implemented.
  */
  def distance(x: Float, y: Float): Float = {
    0 //TODO implement when actually needed; also called lazy implementation ;)
  }

  /**
  * Returns the distance of the specified coordinate from this path.
  * Not implemented.
  */    
  def distance(position: Vector3D): Float = {
    this.distance(position.getX, position.getY)
  }
  
  /**
  * Returns the segment on the path closest to the specified point as well as a parameter which yields the closest point on that path segment.
  */ 
  def closestSegment(position: Vector3D): (Connection, Float) = {
    var closestConnection = this.connections(0)
    var argminParameter = closestConnection.parameterizedClosestPoint(position)
    var minDist = Vector.euclideanDistance(closestConnection(argminParameter), (position.getX, position.getY))   
    this.connections.tail.foreach(connection => {
      val parameter = connection.parameterizedClosestPoint(position)
      val dist = Vector.euclideanDistance(connection(parameter), (position.getX, position.getY))
      if (minDist > dist){
        closestConnection = connection
        argminParameter = parameter
        minDist = dist
      }
    })
    (closestConnection, argminParameter)
  }
  
  
  /**
  * Returns the segment on the path closest to the specified point as well as a parameter which yields the closest point on that path segment.
  */  
  def closestSegment(x: Float, y: Float): (Connection, Float) = {
    this.closestSegment(Vec3d(x, y))
  } 
  
  /**
  * Appends the specified path to this path. This includes updating the node types as well as the associated paths of the nodes
  * and also implies destroying the specified path afterwards.
  */
  private def ++=(path: Path) = {
    path.exists = false //stop receiving events immediately
    val previousConnectionNumber = this.connections.size //get number of connections before changing anything
    
    /* 1. create new connection between the two paths */
    val lastBefore = this.connections.last.nodes.last
    val firstAfter = path.connections.head.nodes.head
    val newConnection = this.defaultConnectionFactory(app, lastBefore, firstAfter) //note that a manipulable connection will use the first node to determine the associated path (!)
    lastBefore.nodeType = AnchorNodeType //last node of this path is now anchor
    firstAfter.nodeType = AnchorNodeType //first node of other path is now anchor, too
    this.connections = this.connections :+ newConnection //adding connection between the two paths
    Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(newConnection, this))

    /* 2. move connections of specified path to this and then delete obsolete path */
    path.connections.foreach(connection => this.connections = this.connections :+ connection) //adding connections of second path to this
    path.connections.foreach(_.nodes.foreach(_.associatedPath = Some(this))) //associate all nodes of specified path with this
    path.connections.foreach(connection => Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(connection, this)))
    Ui.getCurrentScene.registerPreDrawAction(new RemoveChildrenActionThreadSafe(path))
    Ui -= path
    Playback ! PathPlaybackEvent(path, false)
    
    /* 3. if the appended path is currently played back or is paused while this path is not played back, set new playback position */
    if (path.playbackState == Playing || (path.playbackState == Paused && this.playbackState != Playing)) {
      this.currentConnection = previousConnectionNumber + 1 + path.currentConnection //+1 to account for additional new connection between paths
      this.currentBucket = path.currentBucket
      this.bucketAccumulator = path.bucketAccumulator
      this.connectionAccumulator = path.connectionAccumulator
      this.currentConnectionParameter = path.currentConnectionParameter
    }
    if (this.playbackState == Playing || path.playbackState == Playing) this ! UiEvent("START_PLAYBACK")
  }
  
  /**
  * Appends the specified node to this path and sets the node types accordingly.
  */
  private def +=(node: Node) = {
    val lastNode = this.connections.last.nodes.last //get last node of last connection   
    val newConnection = this.defaultConnectionFactory(app, lastNode, node) //create a new connection between last node and specified node
    lastNode.nodeType = AnchorNodeType //set node types
    node.nodeType = StopNodeType //"
    node.associatedPath = Some(this) //set associated path for appended node
    this.connections = this.connections :+ newConnection
    Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(newConnection, this))
    //note that the connection takes care of adding the node as a child of itself
  }
  
  /**
  * Prepends the specified node to this path and sets the node types accordingly.
  */
  private def +=:(node: Node) = {
    val firstNode = this.connections.head.nodes.head //get first node of first connection
    val newConnection = this.defaultConnectionFactory(app, node, firstNode) //create a new connection between first node and specified node
    firstNode.nodeType = AnchorNodeType //set node types
    node.nodeType = if (this.playbackState == Playing) PauseNodeType else PlayNodeType //"
    node.associatedPath = Some(this) //set associated path for appended node
    this.connections = newConnection +: this.connections
    this.currentConnection = if (this.playbackState != Stopped) this.currentConnection + 1 else 0
    Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(newConnection, this))
  }

  
  /**
  * Removes the specified node from this path and returns the resulting path(s).
  *
  * Note that if anything but a list with exactly one path is returned, this path has actually ceased to exist.
  */
  private def -=(node: Node): List[Path] = {
    var result = List(this)  
    node.nodeType match {
      case _: StartNodeType => {
        println("start nodes cannot be removed")
      }
      case _: EndNodeType => {
        println("end nodes cannot be removed")
      }
      case AnchorNodeType => {
        println("anchor nodes cannot be removed")
      }
      case ControlNodeType => {
        println("control node is being removed") 
        result = this.removeControlNode(node)
      }
    }
    result      
  }  

    
  /**
  * Removes the specified control node from this path and returns the resulting path(s).
  */
  private def removeControlNode(node: Node): List[Path] = { 
    //if a control node is removed, split the affected path into two or create one or two isolated nodes
    val optConnection = this.connections.find(_.nodes.exists(_ == node))
    optConnection match {
      case None => List(this) //if the specified node is not part of this path, return immediately, leaving the path unchanged
      case Some(connection) => {
        val index = this.connections.indexOf(connection)
        if (this.connections.size == 1) { //if there is just this one connection
          this.exists = false
          println("single connection")
          val head = connection.nodes.head
          val last = connection.nodes.last
          head.nodeType = IsolatedNodeType
          head.associatedPath = None
          last.nodeType = IsolatedNodeType
          last.associatedPath = None
          Ui -= this
          Playback ! PathPlaybackEvent(this, false)
          List() //path, connection and control point have ceased to exist
        }
        else if (index == 0) { //else if there are other connections but the control node is part of the first connection
          println("follows start node")
          val head = connection.nodes.head
          head.nodeType = IsolatedNodeType //start node becomes isolated
          head.associatedPath = None
          this.connections = this.connections.tail //dropping first connection
          this.connections.head.nodes.head.nodeType = PlayNodeType //new start node (previously anchor) gets corresponding node type
          Ui -= connection //register connection to be deleted; do NOT destroy the connection directly or you will run into concurrency issues
          if (this.currentConnection == index) this.resetPlayback() else this.currentConnection = this.currentConnection - 1
          List(this)           
        }
        else if (index == this.connections.size - 1) { //else if there are other connections but the control node is part of the last connection
          println("followed by end node")
          val last = connection.nodes.last
          last.nodeType = IsolatedNodeType //end node becomes isolated
          last.associatedPath = None
          this.connections = this.connections.init //dropping last connection
          this.connections.last.nodes.last.nodeType = StopNodeType //new end node (previously anchor) gets corresponding node type
          Ui -= connection //register connection to be deleted; do NOT destroy the connection directly or you will run into concurrency issues
          if (this.currentConnection == index) this.resetPlayback()
          List(this)             
        } 
        else { //else there is a split into two paths
          this.exists = false
          println("split")
          val (firstPath, secondPath) = 
          if (this.currentConnection < index) {
            (Path(app, this.defaultConnectionFactory, this.connections.slice(0, index), this.playbackState, this.currentConnection, this.connectionAccumulator, this.currentConnectionParameter, this.currentBucket, this.bucketAccumulator),    
             Path(app, this.defaultConnectionFactory, this.connections.slice(index+1, this.connections.size)))
          }
          else if (this.currentConnection > index) {
            (Path(app, this.defaultConnectionFactory, this.connections.slice(0, index)),
             Path(app, this.defaultConnectionFactory, this.connections.slice(index+1, this.connections.size), this.playbackState, this.currentConnection - (index + 1), this.connectionAccumulator, this.currentConnectionParameter, this.currentBucket, this.bucketAccumulator))              
          }
          else {//connection to be deleted is currently played back, stop all playback in this case
            this.resetPlayback()
            (Path(app, this.defaultConnectionFactory, this.connections.slice(0, index)),
             Path(app, this.defaultConnectionFactory, this.connections.slice(index+1, this.connections.size)))   
          }
          Ui.getCurrentScene.registerPreDrawAction(new RemoveChildrenActionThreadSafe(this))         
          Ui -= this
          Ui -= connection
          //Ui -= connection
          //Ui -= this //register path to be destroyed as well   
          Playback ! PathPlaybackEvent(this, false)
          Ui += firstPath
          Ui += secondPath
          List(firstPath, secondPath)
        }  
      }
    }  
  }
  
  
  override def toString = {
    "Path(" + this.connections.map(_ + "").reduce((c1, c2) => c1 + " + " + c2) + ")"
  }
  
}
