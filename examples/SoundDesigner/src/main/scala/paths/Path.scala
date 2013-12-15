package ui.paths

import org.mt4j.Application

import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.visibleComponents.AbstractVisibleComponent

import org.mt4j.sceneManagement.AddNodeActionThreadSafe

import org.mt4j.util.{SessionLogger, Color}
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.Vec3d

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import scala.actors._

import ui._
import ui.paths.types._
import ui.util._
import ui.events._
import ui.audio._
import ui.properties.types._
import ui.menus.main._
import ui.menus.context._
import ui.usability._
import ui.tools._
import ui.persistence._



abstract class PlaybackState{}
object Playing extends PlaybackState{override def toString = "Playing"}
object Paused extends PlaybackState{override def toString = "Paused"}
object Stopped extends PlaybackState{override def toString = "Stopped"}


object Path {
  
  /**
  * Constructs a path with a variable number of connections between nodes.
  */
  def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => ManipulableBezierConnection), connections: List[ManipulableBezierConnection]) = {
    var channels = new Array[Boolean](Ui.audioInterface.Channels)
    for (index <- 0 until channels.size) channels(index) = true //set channels to true 
    new Path(app, defaultConnectionFactory, connections, Stopped, StopNodeType, false, 0, 0.0f, 0.0f, 0, 0.0f, Map[TimeNode, Boolean](), List[TimeConnection](), channels)
  }
  
  /**
  * Constructs a path with a single connection between the specified nodes.
  */
  def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => ManipulableBezierConnection), firstNode: Node, secondNode: Node) = {
    val startNode = Node(app, PlayNodeType, firstNode.associatedPath, firstNode.getCenterPointGlobal)
    val endNode = Node(app, StopNodeType, secondNode.associatedPath, secondNode.getCenterPointGlobal)
    Ui -= firstNode
    Ui -= secondNode
    Ui += startNode
    Ui += endNode
    var channels = new Array[Boolean](Ui.audioInterface.Channels)
    for (index <- 0 until channels.size) channels(index) = true //set channels to true 
    new Path(app, defaultConnectionFactory, List(defaultConnectionFactory(app, startNode, endNode)), Stopped, StopNodeType, false, 0, 0.0f, 0.0f, 0, 0.0f, Map[TimeNode, Boolean](), List[TimeConnection](), channels)
  }
  
  /**
  * Constructs a path with a variable number of connections between nodes and a given playback position.
  */
  protected def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => ManipulableBezierConnection), connections: List[ManipulableBezierConnection], playback: PlaybackState, playbackType: NodeType, reversed: Boolean, currentCon: Int, connectionAcc: Float, currentConParam: Float, currentBuck: Int, bucketAcc: Float, timeNodesMap: Map[TimeNode, Boolean], timeConnectionsList: List[TimeConnection], channels: Array[Boolean]) = {
    new Path(app, defaultConnectionFactory, connections, playback, playbackType, reversed, currentCon, connectionAcc, currentConParam, currentBuck, bucketAcc, timeNodesMap, timeConnectionsList, channels)
  }
  
  
  /**
  * Constructs a path with a variable number of connections between nodes.
  */
  protected def apply(app: Application, defaultConnectionFactory: ((Application, Node, Node) => ManipulableBezierConnection), connections: List[ManipulableBezierConnection], playback: PlaybackState, playbackType: NodeType, timeNodes: Map[TimeNode, Boolean], timeConnections: List[TimeConnection], channels: Array[Boolean]) = {
    new Path(app, defaultConnectionFactory, connections, playback, playbackType, false, 0, 0.0f, 0.0f, 0, 0.0f, timeNodes, timeConnections, channels)
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
* This class represents a path through the timbre space, that is, a sequence of manipulable bezier connections between nodes.
*
* Note that while there is no theoretical - if practical - limit to the number of connections which can make up a path,
* every path consists of at least one connection.
*
*/ 
class Path(app: Application, defaultConnectionFactory: ((Application, Node, Node) => ManipulableBezierConnection), var connections: List[ManipulableBezierConnection], 
           playback: PlaybackState, playbackType: NodeType, reversed: Boolean, currentCon: Int, connectionAcc: Float, currentConParam: Float, currentBuck: Int, bucketAcc: Float,
           timeNodesMap: Map[TimeNode, Boolean], timeConnectionsList: List[TimeConnection], channels: Array[Boolean]) 
           extends AbstractVisibleComponent(app) with Actor with AudioOutputChannels with MIDIInputChannels with ToolRegistry with Persistability with Identifier {
 
  var exists = true
  
  private var playbackState = playback //whether this path is currently played back, paused or stopped  
  private var isReversedPlayback = reversed //whether the path is played back reversed
  
  private var currentConnection = currentCon //index of the connection currently played 
  private var currentBucket = currentBuck //index of the current _speed_ bucket; use this only in association with the speed property type! 
  private var bucketAccumulator = bucketAcc //accumulate passed time in milliseconds for current bucket
  private var connectionAccumulator = connectionAcc //accumulated passed time in milliseconds for the current connection
  private var currentConnectionParameter = currentConParam //(curve) connection parameter indicating the playback progress on the currently played back connection
  
  private var timeNodes = timeNodesMap
  private var timeConnections = timeConnectionsList
      
  this.setup()

  SessionLogger.log("Created: Path",SessionLogger.SessionEvent.Event, this, null, null);
 
  private def setup() = {
    connections.foreach(_.nodes.foreach(node => {
      node.associatedPath = Some(this)
      //node.updateRotation()
    })) //set associated path to this for all connected nodes
    
    this.timeNodes.keys.foreach(_.associatedPath = Some(this)) //set associated path to this for all time nodes
    
    connections.head.nodes.head.nodeType = if (this.playbackState == Playing) PauseNodeType else PlayNodeType //set start...
    connections.last.nodes.last.nodeType = playbackType //...and end node of this path
    
    (0 until channels.size).foreach(index => this.setOutputChannel(index, channels(index)))
    
    connections.foreach(connection => Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(connection, this))) //add connections as children
    this.start() //start acting
    
    if (this.playbackState == Playing) {
      this ! UiEvent("PLAY") //continue playing if state dictates so
      Playback ! PathPlaybackEvent(this, true)
    }
  }  
  

  def act = {         
    println("path: starting to act!")
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()
    var timeDiff = 0.0f //passed time in milliseconds
    
    var currentBucketValue = 0.0f
    var currentConnectionValue = 0.0f

    var (currentX, currentY) = (0f, 0f)
    
    var ignoreNextTogglePlayback = false //whether the next play/pause playback event is to be ignored
    var ignoreNextStopPlayback = false //whether the next stop playback event is to be ignored
    //these two are needed to distinguish between a tap and a tapAndHold

    while (this.exists) {
      /* Note: Obviously, synchronization goes kind of against the whole actor concept. 
         However, we have to ensure that the mt4j drawing thread (which is not an actor) does not draw while we are updating a path.
         Since we cannot send messages directly to the drawing thread, there is - as far as I can see - no straightforward way to
         ensure consistent state without synchronization on some datastructure.
      */
      
      receive {       
        
        case event: TimeNodeAddEvent => {
          SessionLogger.log("TimeNodeAdd: Path",SessionLogger.SessionEvent.Event, this, event.node, null);
          this.synchronized {
            this.timeNodes = this.timeNodes + (event.node -> false)
            Ui += event.node
          }
        }      
 
        case event: TimeConnectionAddEvent => {
          SessionLogger.log("TimeConnectionAdd: Path",SessionLogger.SessionEvent.Event, this, event.connection.timeNode, event.connection.startNode);
          this.synchronized {
            if (this.timeConnections.filter(connection => connection.timeNode == event.connection.timeNode && connection.startNode == event.connection.startNode).size <= 0) {
              //if such a connection is not already in place
              this.timeConnections = this.timeConnections :+ event.connection
              Ui += event.connection
              Ui += event.connection.connectionNode
            }
          }
        }  
                
        
        case event: PathPlaybackTypeEvent => {
          SessionLogger.log("PathPlaybackType: Path",SessionLogger.SessionEvent.Event, this,null,event.playbackType );
          this.synchronized {
            this.connections.last.nodes.last.nodeType = event.playbackType
          }
        }
        
        case event: ToggleOutputChannelEvent => {
          SessionLogger.log("ToggleOutputChannel: Path",SessionLogger.SessionEvent.Event, this, null, (event.channel, event.channel));
          this.synchronized {
            this.toggleOutputChannel(event.channel)
          }
        }
                                            
        case event: ToggleInputChannelEvent => {
          SessionLogger.log("ToggleInputChannel: Path",SessionLogger.SessionEvent.Event, this, null, (event.channel, event.channel));
          this.synchronized {
            this.activateInputChannel(event.channel)
          }
        }        
        
        case event: NodeDeletionEvent => {
          SessionLogger.log("NodeDeletion: Path",SessionLogger.SessionEvent.Event, this, null, event.node);
          this.synchronized {
            event.node match {
              case timeNode: TimeNode => {
                this.removeTimeNode(timeNode)
              }
              case someNode => {
                if (someNode.nodeType == TimeConnectionNodeType) {
                  val affectedTimeConnections = this.timeConnections.filter(_.connectionNode == someNode)
                  affectedTimeConnections.foreach(timeConnection => {
                    println("affected: " + timeConnection.toString)
                    this.timeConnections = this.timeConnections.filter(_ != timeConnection)
                    timeConnection.startNode.associatedPath.foreach(_ ! event) //propagate to other path if exists
                    timeConnection.startNode match {case node: ManipulableNode => node ! event case otherNode => {}} //or to manipulable node
                    Ui -= timeConnection
                  })      
                  Ui -= someNode 
                }
                else {
                  val result = (this -= someNode)
                  /*if (result.size == 1) { //if we have just cut a node at the start or end of this path
                    this.connections.head.nodes.head.updateRotation() //we update the start node
                    this.connections.last.nodes.last.updateRotation() //and end node of this path
                  }*/
                }
              }
            }
          }
        }
        
        case event: NodeAppendEvent => {
          SessionLogger.log("NodeAppend: Path",SessionLogger.SessionEvent.Event, this, null, event.node);
          this.synchronized {
            this += event.node
            //event.node.updateRotation()
          }
        }
        
        case event: NodePrependEvent => {
          SessionLogger.log("NodePrepend: Path",SessionLogger.SessionEvent.Event, this, null, event.node);
          this.synchronized {
            event.node +=: this
            //event.node.updateRotation()
          }
        }
        
        case event: PathAppendEvent => {
          SessionLogger.log("PathAppend: Path",SessionLogger.SessionEvent.Event, this, null, event.path);
          this.synchronized {
            this ++= event.path
          }
        }
        
        case event: NodeMoveEvent => { //if a node was moved, update the affected connections and rotate node correspondingly
          this.synchronized { 
            val isControlNode = event.node.nodeType == ControlNodeType
            val affectedConnections = this.connections.filter(_.nodes.exists(_ == event.node))
            affectedConnections.foreach(connection => {
              connection.updateCurveParameters() //first update the curve parameters
              //if (isControlNode) connection.nodes.foreach(_.updateRotation())
            })
            //if (!isControlNode) event.node.updateRotation()
              
            this.timeNodes.keys.foreach(timeNode => { //only then update the time nodes
              val (x,y) = timeNode.connection(timeNode.parameter)

              SessionLogger.log("TimeNodeMoveEvent: Path",SessionLogger.SessionEvent.Event, this, event.node, Vec3d(x,y));

              Ui.getCurrentScene.registerPreDrawAction(new RepositionNodeActionThreadSafe(timeNode, Vec3d(x,y)))
              //timeNode.globalPosition := Vec3d(x,y)
            })
            
            this.timeConnections.foreach(timeConnection => { //and the time connections
              timeConnection.updateConnectionNode()
            })
            
          }
        }

       case event: TimeNodeMoveEvent => { //different from node move event since it specifies coordinates x and y
          this.synchronized {
            val (connection, parameter) = this.closestSegment(event.x, event.y)
            val (closestX, closestY) = connection(parameter)

            SessionLogger.log("TimeNodeMoveEvent: Path",SessionLogger.SessionEvent.Event, this, event.node, Vec3d(closestX,closestY));

            event.node.connection = connection
            event.node.parameter = parameter
            //event.node.globalPosition := Vec3d(closestX, closestY) 
            Ui.getCurrentScene.registerPreDrawAction(new RepositionNodeActionThreadSafe(event.node, Vec3d(closestX,closestY)))
            
            val nodeConnectionIndex = this.indexOf(event.node.connection)
            if  (nodeConnectionIndex > this.currentConnection || (nodeConnectionIndex == this.currentConnection && event.node.parameter > this.currentConnectionParameter)) {
                //if through the movement the time node is now yet to come (again)
                this.timeNodes = this.timeNodes.updated(event.node, false) //we reset the trigger mechanism
            }
            this.timeConnections.filter(_.timeNode == event.node).foreach(_.updateConnectionNode()) //more than needed but not costly
          }
        }    

        case event: RegisterToolWithLocationEvent => {
          this.synchronized {
            this.registerTool(event.tool, event.connection, event.connectionParameter, event.manipulationRadius)
          }
        }
        
        case event: UnregisterToolEvent => {
          this.synchronized {
            this.unregisterTool(event.tool)
          }
        }   
        
        case event: PathManipulationEvent => {

          SessionLogger.log("PathManipulationEvent: Path",SessionLogger.SessionEvent.Event, this, null, (event.value, event.value));

          this.synchronized {
            event.connection.updateProperty(event.tool.propertyType, event.connectionParameter, event.manipulationRadius, event.value)
            if (event.tool.propertyType == SpeedPropertyType) { //if the speed property has been changed
              this.connectionAccumulator = event.connection.partialPropertySum(SpeedPropertyType, this.currentBucket-1) + this.bucketAccumulator 
              /* then we need to recalculate the connectionAccumulator since we don't want for manipulations to affect the playback position on the arc */
            }
            this.registerTool(event.tool, event.connection, event.connectionParameter, event.manipulationRadius)
          }
        }     
       
        
        case event: PathFastForwardEvent => {
          this.synchronized {
            if (this.playbackState != Stopped) {
              val hasReachedEnd = this.step(event.time)
              if (hasReachedEnd) {
                this.setPlaybackToEnd() //we set the playback to the end
                this.triggerConnectedEntities() //then we possibly trigger one last time
                this.evaluateEndNode() //and then evaluate what should happen next   
              }
            }
          }
        }
        
        case event: PathRewindEvent => {
          this.synchronized {
            if (this.playbackState != Stopped) {       
              val hasReachedStart = this.stepReverse(event.time) 
              if (hasReachedStart) {
                this.setPlaybackToEnd() //we set the playback to the end
                this.triggerConnectedEntities() //then we possibly trigger one last time
                this.evaluateEndNode() //and then evaluate what should happen next                
              }
            }
          }
        }
        
        case event: UiEvent => { //a 'simple' ui event
          this.synchronized {
            
            if (event.name == "IGNORE_NEXT_TOGGLE_PLAYBACK") {
              ignoreNextTogglePlayback = true
            }
            
            else if (event.name == "IGNORE_NEXT_STOP_PLAYBACK") {
              ignoreNextStopPlayback = true
            }        
            
            else if (event.name == "IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK") {
              ignoreNextTogglePlayback = false
            }   
            
            else if (event.name == "IGNORE_IGNORE_NEXT_STOP_PLAYBACK") {
              ignoreNextStopPlayback = false
            }                          
            
            else if (event.name == "START_GLOBAL_PLAYBACK") {
              val startNode = this.connections.head.nodes.head
              this.timeConnections.find(_.startNode == startNode) match { //start playback of this path only if it is not triggered by another path
                case Some(connection) => {}
                case None => this ! UiEvent("START_PLAYBACK")
              }
            }
            
            else if (event.name == "START_PLAYBACK") {

              SessionLogger.log("StartPlayback: Path",SessionLogger.SessionEvent.Event, this, null, null);

              if (ignoreNextTogglePlayback) {ignoreNextTogglePlayback = false}
              else {
                if (this.playbackState != Playing) {
                  lastTime = System.nanoTime() //init time
                  this.connections.head.nodes.head.nodeType = PauseNodeType
                  this.playbackState = Playing
                  Playback ! PathPlaybackEvent(this, true)
                }          
                this ! UiEvent("PLAY")
              }
            }
            
            else if (event.name == "START_REVERSE_PLAYBACK") {         
              this ! UiEvent("PLAY")          
            }

            else if (event.name == "PAUSE_PLAYBACK") {
              if (ignoreNextTogglePlayback) {ignoreNextTogglePlayback = false}
              else {              
                if (this.playbackState == Playing) {
                  this.connections.head.nodes.head.nodeType = PlayNodeType             
                  this.playbackState = Paused
                  Ui.audioInterface ! PauseAudioEvent(this.id)
                  Playback ! PathPlaybackEvent(this, false)
                }
              }
            }

            else if (event.name == "STOP_GLOBAL_PLAYBACK") {

              SessionLogger.log("StartPlayback: Path",SessionLogger.SessionEvent.Event, this, null, null);

              this ! UiEvent("STOP_PLAYBACK")
            }
            
            else if (event.name == "STOP_PLAYBACK") {
              if (ignoreNextStopPlayback) {ignoreNextStopPlayback = false}
              else {
                this.resetPlayback()
                Ui.audioInterface ! PauseAudioEvent(this.id)
                Playback ! PathPlaybackEvent(this, false)
              }
            }
            
            else if (event.name == "PLAY") {  
              if (this.playbackState == Playing) {            
                var hasReachedEnd = false
                currentTime = System.nanoTime()
                timeDiff = (currentTime - lastTime)/1000000.0f //passed time in milliseconds
                lastTime = currentTime
                val con = this.connections(this.currentConnection)
                val buckets = con.propertyBuckets(SpeedPropertyType) //get number of buckets    
                currentBucketValue = con.propertyValue(SpeedPropertyType, this.currentBucket)
                currentConnectionValue = con.propertySum(SpeedPropertyType)

                /* ################################ */
                //first send new audio event if necessary
                val (uiXFloat, uiYFloat) = con(this.currentConnectionParameter)
                val (uiX, uiY) = (uiXFloat.toInt, uiYFloat.toInt)
                val (newX, newY) = (uiX/Ui.width.toFloat, uiY/Ui.height.toFloat)
                  
                if (newX != currentX || newY != currentY){
                  currentX = newX
                  currentY = newY
                  val channels = this.collectOpenOutputChannels
                  val arcParameter = this.currentBucket/buckets.toFloat + (this.bucketAccumulator/currentBucketValue)/buckets
                  val pitchBucket = (arcParameter * (con.propertyBuckets(PitchPropertyType) - 1)).toInt
                  val volumeBucket = (arcParameter * (con.propertyBuckets(VolumePropertyType) - 1)).toInt
                  Ui.audioInterface ! PlayAudioEvent(this.id, currentX, currentY, con.propertyValue(PitchPropertyType, pitchBucket), con.propertyValue(VolumePropertyType, volumeBucket), activeInputChannel, channels)
                }
                /* ################################ */
                
                
                
                
                /* ################################ */
                // then possibly trigger connected entities
                this.triggerConnectedEntities()
                /* ################################ */ 
                
                
                
                /* ################################ */
                // then progress in time
                hasReachedEnd = 
                  if (!this.isReversedPlayback) this.step(timeDiff) 
                  else this.stepReverse(timeDiff)                  
                /* ################################ */
                

                if (hasReachedEnd) { //if we have reached the end of the path, 
                  this.setPlaybackToEnd() //we set the playback to the end
                  this.triggerConnectedEntities() //then we possibly trigger one last time
                  this.evaluateEndNode() //and then evaluate what should happen next   
                }
                else this ! event //else we keep playing until the playback is either stopped/paused or the path has been played back
                
              }              
            }       
            
            else if (event.name == "STOP_ACTING") {
              exit()
            }
            
            else {
              println("OH NO! Event is " + event.name)
            }
          }
        }
      }
      Thread.sleep(5)
    }
  }    

  
  /**
  * Steps forward in time and then returns whether the path has been played back.
  */
  private def step(timeDiff: Float): Boolean = {
    var hasReachedEnd = false 
    var con = this.connections(this.currentConnection)
    var currentBucketValue = con.propertyValue(SpeedPropertyType, this.currentBucket) 
    var buckets = con.propertyBuckets(SpeedPropertyType) //get number of buckets 
    this.bucketAccumulator = this.bucketAccumulator + timeDiff //accumulate passed time for current bucket
    this.connectionAccumulator = this.connectionAccumulator + timeDiff //and current connection
    
    while (this.bucketAccumulator >= currentBucketValue && !hasReachedEnd) { //while the time specified by the bucket still surpasses the value of the current bucket and the path has not reached its end
      currentBucketValue = con.propertyValue(SpeedPropertyType, this.currentBucket) 
      this.currentBucket = this.currentBucket + 1 //we process the next current bucket of the current connection //(connectionAccumulator/currentConnectionValue * buckets).toInt //
      this.bucketAccumulator = this.bucketAccumulator - currentBucketValue //and set back the bucket accumulator with carry-over 
      if (this.currentBucket >= buckets) { //if we processed all buckets of the current connection
        this.currentConnection = this.currentConnection + 1 //we process the next connection on the path
        this.currentBucket = 0 //and set back the current bucket variable
        //println("setting current bucket to " + this.currentBucket)
        this.connectionAccumulator = this.bucketAccumulator //as well as the connection accumulator, again accounting for carry-over
        if (this.currentConnection >= this.connections.size) { //if we have processed all connections on the path
          hasReachedEnd = true
        }
        else { //if there are still connections left, get the next and update the bucket number
          con = this.connections(this.currentConnection)
          buckets = con.propertyBuckets(SpeedPropertyType) //get number of buckets 
        }
      }
    } 
    this.currentConnectionParameter = con.toCurveParameter(this.currentBucket/buckets.toFloat + (this.bucketAccumulator/currentBucketValue)/buckets) 
    //println("param: " + this.currentConnectionParameter)
    hasReachedEnd
  }  
  
  
  /**
  * Steps backwards in time and then returns whether the start of the path has been reached.
  */
  private def stepReverse(timeDelta: Float): Boolean = {
    val timeDiff = -timeDelta
    var hasReachedStart = false
    var con = this.connections(this.currentConnection)
    var currentBucketValue = con.propertyValue(SpeedPropertyType, this.currentBucket) 
    var buckets = con.propertyBuckets(SpeedPropertyType) //get number of buckets 
    
    this.bucketAccumulator = this.bucketAccumulator + timeDiff //accumulate passed time for current bucket
    this.connectionAccumulator = this.connectionAccumulator + timeDiff //and current connection
    
    while (this.bucketAccumulator <= 0 && !hasReachedStart) { //while the time specified by the bucket still undershoots 0 and the path has not reached its start
      currentBucketValue = con.propertyValue(SpeedPropertyType, this.currentBucket) 
      this.currentBucket = this.currentBucket - 1 //we process the previous current bucket of the current connection //(connectionAccumulator/currentConnectionValue * buckets).toInt //
      //println("reverse 1: setting current bucket to " + this.currentBucket)
      if (this.currentBucket >= 0) {
        this.bucketAccumulator = con.propertyValue(SpeedPropertyType, this.currentBucket) + this.bucketAccumulator //and set back the bucket accumulator with carry-over 
      }
      else { //if we processed all buckets of the current connection in reverse
        this.currentConnection = this.currentConnection - 1 //we process the previous connection on the path
        if (this.currentConnection >= 0) {
          con = this.connections(this.currentConnection)
          this.currentBucket = con.propertyBuckets(SpeedPropertyType) - 1//and set back the current bucket variable
          this.bucketAccumulator = con.propertyValue(SpeedPropertyType, this.currentBucket) + this.bucketAccumulator //as well as the bucket accumulator with carry-over 
          //println("reverse 2: setting current bucket to " + this.currentBucket)
          this.connectionAccumulator = con.propertySum(SpeedPropertyType) + this.bucketAccumulator //as well as the connection accumulator, again accounting for carry-over
          buckets = con.propertyBuckets(SpeedPropertyType) //get number of buckets 
        }
        else if (this.currentConnection < 0) { //if we have processed all connections on the path in reverse
          hasReachedStart = true
        }
      }
    }    
    this.currentConnectionParameter = con.toCurveParameter(this.currentBucket/buckets.toFloat + (this.bucketAccumulator/currentBucketValue)/buckets) 
    //println("param: " + this.currentConnectionParameter)    
    hasReachedStart
  }
  
  
  
  /**
  * Triggers connected entities - that is, either paths or manipulable nodes - if their associated time node has been reached.
  */
  private def triggerConnectedEntities() = {
    this.timeNodes.foreach(entry => { //for each time node
      val timeNode = entry._1
      val hasBeenTriggered = entry._2
      val trigger = 
        if (!this.isReversedPlayback) (!hasBeenTriggered && timeNode.connection == this.connections(math.min(this.currentConnection, this.connections.size - 1)) && timeNode.parameter <= this.currentConnectionParameter)
        else (!hasBeenTriggered && timeNode.connection == this.connections(math.min(this.currentConnection, this.connections.size - 1)) && timeNode.parameter >= this.currentConnectionParameter)
      if (trigger) {
        this.timeNodes = this.timeNodes.updated(timeNode, true)           
        this.timeConnections.filter(_.timeNode == timeNode).foreach(timeConnection => {
          timeConnection.startNode.associatedPath match {
            case Some(path) => if (path.state != Playing) {           
              path ! UiEvent("START_PLAYBACK")
            }
            case None => { 
              timeConnection.startNode match {
                case manipulableNode: ManipulableNode => {manipulableNode ! UiEvent("TOGGLE_PLAYBACK")}
                case otherNode => {}
              }
            }
          }
        })
      }
    })    
  }
  
  
  /**
  * Returns the manipulable bezier connections of this path.
  */
  /*private def manipulableConnections: List[ManipulableBezierConnection] = {
    this.connections collect {case c: ManipulableBezierConnection => c}
  } */
  
  
  private def evaluateEndNode() {
    val endNodeType = this.connections.last.nodes.last.nodeType
    if (endNodeType == RepeatNodeType) {
      this.resetPlayback()
      this ! UiEvent("START_PLAYBACK")
    }
    else if (endNodeType == ReverseNodeType) {
      if (!this.isReversedPlayback) {
        this.timeNodes = this.timeNodes.mapValues(value => false) //set all time nodes back to being untriggered
        this.isReversedPlayback = true
        this ! UiEvent("START_REVERSE_PLAYBACK")
      }
      else {
        this.isReversedPlayback = false
        this.resetPlayback()
        Ui.audioInterface ! StopAudioEvent(this.id)
        Playback ! PathPlaybackEvent(this, false)
      }
    }
    else { //StopNodeType
      this.resetPlayback()
      Ui.audioInterface ! StopAudioEvent(this.id)
      Playback ! PathPlaybackEvent(this, false)
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
    this.isReversedPlayback = false
    this.timeNodes = this.timeNodes.mapValues(value => false) //set all time nodes back to being untriggered
  }
  
  
  private def setPlaybackToEnd() = {
    this.currentConnection = this.connections.size - 1
    this.currentBucket = this.connections(currentConnection).propertyBuckets(SpeedPropertyType) - 1
    this.connectionAccumulator = this.connections(currentConnection).propertySum(SpeedPropertyType)
    this.bucketAccumulator = this.connections(currentConnection).propertyValue(SpeedPropertyType, this.currentBucket)
    this.currentConnectionParameter = 1.0f
  }
  
  
  /**
  * Whether this path is currently playing.
  */
  def isPlaying = {
    this.playbackState == Playing
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
    this.connections.foldLeft(0.0f)((sum, con) => sum + con.propertySum(SpeedPropertyType))
  }
  
  /**
  * Returns the time in milliseconds it takes to complete the playback of this path.
  */ 
  def timeLeft: Float = {
    val timePassed = this.connections.take(this.currentConnection + 1).foldLeft(0.0f)((sum, con) => sum + con.propertySum(SpeedPropertyType)) + this.connectionAccumulator
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
  override def drawComponent(g: PGraphicsOpenGL) = {
    //connections are drawn automatically due to parent-child-relationship
    //thus this method is only needed for decorating the path; in this case, with audio channel visualisation
    this.synchronized {
      val startNode = this.connections.head.nodes.head
      val (x,y) = startNode.position
      g.noFill()
      g.strokeWeight(2)
      val stepSize = 2*PI.toFloat/this.outputChannelNumber
      (0 until this.outputChannelNumber).foreach(index => {
        val color = AudioOutputChannels.colorFromIndex(index)
        if (this.isOutputChannelOpen(index)) g.stroke(color.getR, color.getG, color.getB, 150) else g.stroke(0, 0, 0, 50)
        g.arc(x, y, 2*startNode.radius*startNode.getScaleFactor - 2, 2*startNode.radius*startNode.getScaleFactor - 2, HALF_PI + index*stepSize, HALF_PI + (index+1)*stepSize)
      })      
      
      g.fill(0,20,80,150)
      g.noStroke()
      (0 to this.activeInputChannel).foreach(item => {
        val (px,py) = Functions.positionOnCircle(x, y, startNode.radius*startNode.getScaleFactor, 2*math.Pi.toFloat, item, this.activeInputChannel + 1)
        g.ellipse(px, py, InputChannelItem.DotWidth, InputChannelItem.DotWidth)
      })
    }
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
  * Returns the tangent at the specified node if it is part of or associated with this path, or (0,0) if it is not.
  */
  def tangent(node: Node): (Float, Float) = {
    this.connectionForNode(node) match {
      case Some(connection) => {
        connection.tangent(connection.parameterizedClosestPoint(node.positionAsVertex))
      }      
      case None => {
        if (node.nodeType == TimeConnectionNodeType) {        
          this.timeConnectionForNode(node) match {
            case Some(timeConnection) => {
              Functions.gradient(timeConnection.timeNode.position, timeConnection.startNode.position)
            }
            case None => (0.0f, 0.0f)
          }
        }
        else (0.0f, 0.0f)
      }
    }
  }
  
  
  /**
  *Returns - as an Option - the connection corresponding to the given node, nor None if the node is not part of this path.
  */
  private def connectionForNode(node: Node): Option[Connection] = {
    this.connections.find(_.nodes.exists(_ == node))   
  }
  
  /**
  *Returns - as an Option - the time connection corresponding to the given connection node, nor None if the connection node is not associated with this path.
  */
  private def timeConnectionForNode(connectionNode: Node): Option[TimeConnection] = {
    this.timeConnections.find(_.connectionNode == connectionNode)
  }  
  
  
  /**
  * Appends the specified path to this path. This includes updating the node types as well as the associated paths of the nodes
  * and also implies destroying the specified path afterwards.
  */
  private def ++=(path: Path) = {
    path.exists = false //stop receiving events immediately
    val previousConnectionNumber = this.connections.size //get number of connections before changing anything
    
    /* 1. create new connection between the two paths */
    val lastBefore = this.connections.last.endNode
    val firstAfter = path.connections.head.startNode
    val newConnection = this.defaultConnectionFactory(app, lastBefore, firstAfter) //note that a manipulable connection will use the first node to determine the associated path (!)
    lastBefore.nodeType = AnchorNodeType //last node of this path is now anchor
    firstAfter.nodeType = AnchorNodeType //first node of other path is now anchor, too
    this.connections = this.connections :+ newConnection //adding connection between the two paths
    Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(newConnection, this))

    /* 2. move connections of specified path to this, update time nodes & connections*/
    path.connections.foreach(connection => this.connections = this.connections :+ connection) //adding connections of second path to this
    path.connections.foreach(_.nodes.foreach(_.associatedPath = Some(this))) //associate all nodes of specified path with this
    
    /* 3. move time nodes of specified path to this and update time connections */
    val thisLegalTimeConnections = this.timeConnections.diff(path.timeConnections) //connections of this which do not connect to the specified path
    val thisIllegalTimeConnections = this.timeConnections.diff(thisLegalTimeConnections) //connections of this which DO connect to the specified path
    val pathLegalTimeConnections = path.timeConnections.diff(this.timeConnections) //connections from the specified path to other paths than this
    this.timeConnections = (thisLegalTimeConnections ++ pathLegalTimeConnections)
    thisIllegalTimeConnections.foreach(Ui -= _) //and remove them from the ui
    this.timeNodes = this.timeNodes ++ path.timeNodes 
    this.timeNodes.keys.foreach(_.associatedPath = Some(this))
    
    /* 4. delete obsolete objects */
    path.connections.foreach(connection => Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(connection, this)))
    Ui.getCurrentScene.registerPreDrawAction(new RemoveChildrenActionThreadSafe(path))
    Ui -= path
    Playback ! PathPlaybackEvent(path, false)
    
    /* 5. if the appended path is currently played back or is paused while this path is not played back, set new playback position */
    if (path.playbackState == Playing || (path.playbackState == Paused && this.playbackState != Playing)) {
      this.currentConnection = previousConnectionNumber + 1 + path.currentConnection //+1 to account for additional new connection between paths
      this.currentBucket = path.currentBucket
      this.bucketAccumulator = path.bucketAccumulator
      this.connectionAccumulator = path.connectionAccumulator
      this.currentConnectionParameter = path.currentConnectionParameter
      this.isReversedPlayback = path.isReversedPlayback
    }
    if (this.playbackState == Playing || path.playbackState == Playing) this ! UiEvent("START_PLAYBACK")
  }
  
  /**
  * Appends the specified node to this path and sets the node types accordingly.
  */
  private def +=(node: Node) = {
    val lastNode = this.connections.last.endNode //get last node of last connection 
    val newNode = Node(node.app, lastNode.nodeType, Some(this), node.getCenterPointGlobal)
    val newConnection = this.defaultConnectionFactory(app, lastNode, newNode) //create a new connection between last node and specified node
    lastNode.nodeType = AnchorNodeType //set node types
    this.connections = this.connections :+ newConnection
    this.removeTimeConnectionsToStartNode(node) 
    Ui -= node
    Ui += newNode
    Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(newConnection, this))
  }
  
  /**
  * Prepends the specified node to this path and sets the node types accordingly.
  */
  private def +=:(node: Node) = {
    this.removeTimeConnectionsToStartNode(this.connections.head.startNode)
    val firstNode = this.connections.head.startNode //get first node of first connection
    val newNode = Node(node.app, if (this.playbackState == Playing) PauseNodeType else PlayNodeType, Some(this), node.getCenterPointGlobal)
    val newConnection = this.defaultConnectionFactory(app, newNode, firstNode) //create a new connection between first node and specified node
    firstNode.nodeType = AnchorNodeType //set node types
    this.connections = newConnection +: this.connections
    this.removeTimeConnectionsToStartNode(node)     
    this.currentConnection = if (this.playbackState != Stopped) this.currentConnection + 1 else 0
    Ui -= node
    Ui += newNode
    Ui.getCurrentScene.registerPreDrawAction(new AddNodeActionThreadSafe(newConnection, this))
  }

  
  /**
  * Removes the specified node from this path and returns the resulting path(s).
  *
  * Note that if anything but a list with exactly one path is returned, this path has actually ceased to exist.
  */
  private def -=(node: Node): List[Path] = {
    var result = List(this)  
    if (node.nodeType == TimeNodeType) {
      Ui -= node
    }
    else if (this.connections.exists(_.nodes.exists(_ == node))) {
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
    }
    else {} //node is not associated at all with this path
    result      
  }  

    
  /**
  * Removes the specified control node from this path and returns the resulting path(s).
  */
  private def removeControlNode(node: Node): List[Path] = { 
    //if a control node is removed, split the affected path into two or create one or two isolated nodes
    Ui -= node
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
          Ui += SingleNode(app, head.position)
          Ui += SingleNode(app, last.position)
          this.removeTimeNodesOnConnection(connection)
          this.removeTimeConnectionsToStartNode(connection.startNode)
          Ui -= head
          Ui -= last
          Ui -= this
          Playback ! PathPlaybackEvent(this, false)
          List() //path, connection and control point have ceased to exist
        }
        else if (index == 0) { //else if there are other connections but the control node is part of the first connection
          println("follows start node")
          val head = connection.nodes.head
          val previousStartNodeType = head.nodeType
          this.connections = this.connections.tail //dropping first connection
          this.connections.head.nodes.head.nodeType = previousStartNodeType //new start node (previously anchor) gets corresponding node type
          this.removeTimeNodesOnConnection(connection)
          this.removeTimeConnectionsToStartNode(connection.startNode)
          Ui -= head
          Ui += SingleNode(app, head.position)
          Ui -= connection //register connection to be deleted; do NOT destroy the connection directly or you will run into concurrency issues
          if (this.currentConnection == index) {
            Ui.audioInterface ! StopAudioEvent(this.id)
            this.resetPlayback()
          } 
            else this.currentConnection = this.currentConnection - 1
          List(this)           
        }
        else if (index == this.connections.size - 1) { //else if there are other connections but the control node is part of the last connection
          println("followed by end node")
          val last = connection.nodes.last
          val previousEndNodeType = last.nodeType
          this.connections = this.connections.init //dropping last connection
          this.connections.last.nodes.last.nodeType = previousEndNodeType //new end node (previously anchor) gets corresponding node type
          this.removeTimeNodesOnConnection(connection)
          Ui -= last
          Ui += SingleNode(app, last.position)
          Ui -= connection //register connection to be deleted; do NOT destroy the connection directly or you will run into concurrency issues
          if (this.currentConnection == index) {
            Ui.audioInterface ! StopAudioEvent(this.id)
            this.resetPlayback()
          } 
          List(this)             
        } 
        else { //else there is a split into two paths
          this.exists = false
          println("split")
          
          val firstTimeNodes = this.timeNodes.filterKeys(timeNode => this.indexOf(timeNode.connection) < index) 
          val secondTimeNodes = this.timeNodes.filterKeys(timeNode => this.indexOf(timeNode.connection) > index) 
          val deletedTimeNodes = this.timeNodes.filterKeys(timeNode => this.indexOf(timeNode.connection) == index) 
          val firstTimeConnections = this.timeConnections.filter(connection => firstTimeNodes.filterKeys(timeNode => timeNode == connection.timeNode).size > 0)
          val secondTimeConnections = this.timeConnections.filter(connection => secondTimeNodes.filterKeys(timeNode => timeNode == connection.timeNode).size > 0)

          deletedTimeNodes.foreach(entry => this.removeTimeNode(entry._1))
          
          val (firstPath, secondPath) = 
          if (this.currentConnection < index) {
            (Path(app, this.defaultConnectionFactory, this.connections.slice(0, index), this.playbackState, StopNodeType, this.isReversedPlayback, this.currentConnection, this.connectionAccumulator, this.currentConnectionParameter, this.currentBucket, this.bucketAccumulator, firstTimeNodes, firstTimeConnections, this.audioChannel),    
             Path(app, this.defaultConnectionFactory, this.connections.slice(index+1, this.connections.size), Stopped, this.connections.last.nodes.last.nodeType, secondTimeNodes, secondTimeConnections, this.audioChannel))
          }
          else if (this.currentConnection > index) {
            (Path(app, this.defaultConnectionFactory, this.connections.slice(0, index), Stopped, StopNodeType, firstTimeNodes, firstTimeConnections, this.audioChannel),
             Path(app, this.defaultConnectionFactory, this.connections.slice(index+1, this.connections.size), this.playbackState, this.connections.last.nodes.last.nodeType, this.isReversedPlayback, this.currentConnection - (index + 1), this.connectionAccumulator, this.currentConnectionParameter, this.currentBucket, this.bucketAccumulator, secondTimeNodes, secondTimeConnections, this.audioChannel))              
          }
          else {//connection to be deleted is currently played back, stop all playback in this case
            Ui.audioInterface ! StopAudioEvent(this.id)
            this.resetPlayback()
            (Path(app, this.defaultConnectionFactory, this.connections.slice(0, index), Stopped, StopNodeType, firstTimeNodes, firstTimeConnections, this.audioChannel),
             Path(app, this.defaultConnectionFactory, this.connections.slice(index+1, this.connections.size), Stopped, this.connections.last.nodes.last.nodeType, secondTimeNodes, secondTimeConnections, this.audioChannel))   
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

  
  private def removeTimeNode(timeNode: TimeNode) = {
    val affectedTimeConnections = this.timeConnections.filter(_.timeNode == timeNode) //get all connections from the time node
    affectedTimeConnections.foreach(timeConnection => {
      this.timeConnections = this.timeConnections.filter(_ != timeConnection) //and remove them from this path
      timeConnection.startNode.associatedPath.foreach(_ ! NodeDeletionEvent(timeConnection.connectionNode)) //propagate to other path if exists
      timeConnection.startNode match {case node: ManipulableNode => node ! TimeConnectionDeletionEvent(timeConnection) case otherNode => {}} //or to manipulable node
      Ui -= timeConnection.connectionNode //remove the connection node as well
      Ui -= timeConnection
    })   
    this.timeNodes = this.timeNodes - timeNode //then also remove the time node itself
    Ui -= timeNode    
  }
  
  /**
  * Removes all time connections of this path which are connected to the given start node.
  */
  private def removeTimeConnectionsToStartNode(startNode: Node) = {    
    val affectedTimeConnections = this.timeConnections.filter(_.startNode == startNode) //get all connections to the start node
    affectedTimeConnections.foreach(timeConnection => {
      this.timeConnections = this.timeConnections.filter(_ != timeConnection) //and remove them from this path
      timeConnection.timeNode.associatedPath.foreach(_ ! NodeDeletionEvent(timeConnection.connectionNode)) //propagate to other path if exists
      Ui -= timeConnection.connectionNode //remove the connection node as well
      Ui -= timeConnection
    })      
  }
  
  
  private def removeTimeNodesOnConnection(connection: Connection) = {
    this.timeNodes.foreach(timeNodeTrigger => {
      val timeNode = timeNodeTrigger._1
      if (timeNode.connection == connection) this.removeTimeNode(timeNode)
    })
  }
  
  
  override def destroy() = {
    this ! "STOP_ACTING"
    this.exists = false    
    Ui.audioInterface ! StopAudioEvent(this.id)
    super.destroy()
  }
  
  override def toString = {
    "Path(" + this.connections.map(_ + "").foldLeft("")((c1, c2) => c1 + " + " + c2) + ") @"+this.hashCode
  }
  
  
  override def toXML = {
    val start = "<path factory = 'ManipulableBezierConnection'>"
    val connections = "<connections>" + this.connections.map(_.toXML).foldLeft("")((c1, c2) => c1 + " " + c2) + "</connections>"
    val timeNodes = "<timeNodes>" + this.timeNodes.keys.map(_.toXML).foldLeft("")((n1, n2) => n1 + " " + n2) + "</timeNodes>"
    val timeConnections = "<timeConnections>" + this.timeConnections.map(_.toXML).foldLeft("")((c1, c2) => c1 + " " + c2) + "</timeConnections>"
    val end = "</path>"
    start + connections + timeNodes + timeConnections + end
  }
  
  
}
