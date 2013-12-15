package ui.paths

import org.mt4j.Application
import org.mt4j.util.math.Vector3D
import org.mt4j.types.{Vec3d}

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.{SessionLogger, Color}

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import scala.actors._

import ui.paths.types._
import ui.properties._
import ui.properties.types._
import ui.events._
import ui.audio._
import ui._
import ui.util._
import ui.tools._
import ui.persistence._
import ui.menus.context._


object ManipulableNode {
  
  def apply(app: Application, center: (Float, Float)) = {
      new ManipulableNode(app, ManipulableNodeType, Vec3d(center._1, center._2))
  }
  
  def apply(app: Application, center: Vector3D) = {
      new ManipulableNode(app, ManipulableNodeType, center)
  }
  
  protected def apply(app: Application, center: Vector3D, nodeType: NodeType) = {
      new ManipulableNode(app, nodeType, center)
  }

}

/**
* This class represents a manipulable node.
* The following manipulations are supported:
* <ul>
*   <li> Change of pitch </li>
*   <li> Change of volume  </li>
* </ul>
*/
class ManipulableNode(app: Application, nodeType: NodeType, center: Vector3D) extends Node(app, nodeType, None, center) with Actor with AudioOutputChannels with MIDIInputChannels with ToolRegistry with Persistability {
  var exists = true
  protected var isNodePlaying = false
  private var playbackPos = 0.0f

  private var timeConnections = List[TimeConnection]()
  
  //this.setupDeletionNode()
  this.start() //note: do not put this in an override setup method, it won't work...
  
  private var properties = Map(
    (VolumePropertyType -> SimpleVolumeProperty(this)),
    (PitchPropertyType -> SimplePitchProperty(this))
  )  
  
   def setProperties(propertyMap: Map[PropertyType,SimpleProperty]) = {
     this.properties = propertyMap
   }  
   
   /**
   * Returns for the given property type the current value.
   */
   def getPropertyValue(propertyType: PropertyType) = {
     this.properties(propertyType)()
   }

  
  def act = {          
    println("node: starting to act!")
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()
    var timeDiff = 0.0f //passed time in milliseconds
    
    var ignoreNextTogglePlayback = false //whether the next play/pause playback event is to be ignored

    while (this.exists) {
      receive {

        case event: RegisterToolEvent => {
          this.synchronized {
            this.registerTool(event.tool)
          }
        }
        
        case event: UnregisterToolEvent => {
          this.synchronized {
            this.unregisterTool(event.tool)
          }
        }         
        
        case event: ManipulationEvent => {
          SessionLogger.log("Manipulation: Node",SessionLogger.SessionEvent.Event, this, event.tool, (event.value, event.value));
          this.synchronized {
            this.updateProperty(event.tool.propertyType, event.value) 
            this.sendPlayEvent()
            this.registerTool(event.tool)
          }
        }
        
        case event: ToggleOutputChannelEvent => {
          SessionLogger.log("ToggleOutputChannel: Node",SessionLogger.SessionEvent.Event, this, null, (event.channel, event.channel));

          this.synchronized {
            this.toggleOutputChannel(event.channel)
            this.sendPlayEvent()
          }
        } 
        
        case event: ToggleInputChannelEvent => {
          SessionLogger.log("ToggleInputChannel: Node",SessionLogger.SessionEvent.Event, this, null, (event.channel, event.channel));
          this.synchronized {
            this.activateInputChannel(event.channel)
            this.sendPlayEvent()
          }
        }        
  
        case event: TimeConnectionAddEvent => {
          SessionLogger.log("TimeConnectionAddEvent: Node",SessionLogger.SessionEvent.Event, this, event.connection.startNode, event.connection.timeNode);
          this.synchronized {
            if (this.timeConnections.filter(connection => connection.timeNode == event.connection.timeNode && connection.startNode == event.connection.startNode).size <= 0) {
              //if such a connection is not already in place
              this.timeConnections = this.timeConnections :+ event.connection
              Ui += event.connection
              Ui += event.connection.connectionNode
            }
          }
        }   
        
        case event: TimeConnectionDeletionEvent => {
          SessionLogger.log("TimeConnectionDelEvent: Node",SessionLogger.SessionEvent.Event, this, event.connection.startNode, event.connection.timeNode);
          this.synchronized {
            this.timeConnections = this.timeConnections.filter(_ != event.connection)
          }
        }
        
        case event: NodeMoveEvent => {
          SessionLogger.log("NodeMove: Node",SessionLogger.SessionEvent.Event, this, null, event.node.position);
          this.synchronized {
            this.timeConnections.foreach(timeConnection => {
              timeConnection.updateConnectionNode()
            })
            this.sendPlayEvent()
          }
        }
                     
        case event: UiEvent => { //a 'simple' ui event
          this.synchronized {        
            
            if (event.name == "REMOVE_TIME_CONNECTIONS") {
              this.synchronized {
                this.removeTimeConnections()
              }
            }
            
            else if (event.name == "IGNORE_NEXT_TOGGLE_PLAYBACK") {
              ignoreNextTogglePlayback = true
            }
                      
            else if (event.name == "IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK") {
              ignoreNextTogglePlayback = false
            }                       

            else if (event.name == "START_GLOBAL_PLAYBACK") {
              this.timeConnections.find(_.startNode == this) match { //start playback of this node only if it is not triggered by another path
                case Some(connection) => {}
                case None => {
                  Ui.audioInterface ! PauseAudioEvent(this.id)
                  this.isNodePlaying = false //reset so that toggling will start playback
                  this.playbackPos = 0.0f
                  this ! UiEvent("TOGGLE_PLAYBACK")
                }
              }
            }
            
            else if (event.name == "STOP_GLOBAL_PLAYBACK") {
              this.isNodePlaying = true
              this ! UiEvent("STOP_PLAYBACK")
            }            
            
            else if (event.name == "TOGGLE_PLAYBACK") {
              if (ignoreNextTogglePlayback) ignoreNextTogglePlayback = false
              else {   
                if (!this.isNodePlaying) {
                  this ! UiEvent("START_PLAYBACK")
                }
                else {
                  this ! UiEvent("STOP_PLAYBACK")               
                }
              }
            }
            
            else if (event.name == "START_PLAYBACK") {
              SessionLogger.log("StartPlayback: Node",SessionLogger.SessionEvent.Event, this, null, null);

              lastTime = System.nanoTime() //init time
              this.isNodePlaying = true
              this.sendPlayEvent()
              this ! UiEvent("PLAY") 
            }
              
            else if (event.name == "STOP_PLAYBACK") {
                SessionLogger.log("StopPlayback: Node",SessionLogger.SessionEvent.Event, this, null, null);

                Ui.audioInterface ! PauseAudioEvent(this.id)
                this.isNodePlaying = false
                this.playbackPos = 0.0f               
            }
            
            else if (event.name == "PLAY") {
              if (this.isNodePlaying) {
                currentTime = System.nanoTime()
                val passedTime = (currentTime - lastTime)/1000000.0f
                this.playbackPos = if (passedTime%2000 < 1000) (passedTime%1000)/1000 else 1f - (passedTime%1000)/1000
                //val (uiX, uiY) = (this.position._1, this.position._2)
                //val (x, y) = (uiX/Ui.width, uiY/Ui.height)
                //Ui.audioInterface ! PlayAudioEvent(this.id, x, y, this.properties(PitchPropertyType)(), this.properties(VolumePropertyType)(),this.activeInputChannel, this.collectOpenOutputChannels)
                this ! event
              }              
            }
            
            else if (event.name == "STOP_ACTING") {
              exit()
            }
            
            else println("OH NO!")
          }
        }
      }
      Thread.sleep(5)
    }
  } 
  
  
  def sendPlayEvent() = {
    if (this.isPlaying) {
      val (uiX, uiY) = (this.position._1, this.position._2)
      val (x, y) = (uiX/Ui.width, uiY/Ui.height)
      Ui.audioInterface ! PlayAudioEvent(this.id, x, y, this.properties(PitchPropertyType)(), this.properties(VolumePropertyType)(), this.activeInputChannel, this.collectOpenOutputChannels)          
    }
  }
  
  
  def playbackPosition: Float = {
    this.playbackPos
  }
  
  /**
  * Whether this node is currently playing.
  */
  def isPlaying = {
    this.isNodePlaying
  }
    
  
  /**
  * Updates the specified property.
  */
  private def updateProperty(propertyIdentifier: PropertyType, value: Float) = {
    if (this.properties.contains(propertyIdentifier)) {
      val property = this.properties(propertyIdentifier)
      property.update(this.value(property, value))
    }
  }
  
  /**
  * Returns a value which can be used to update the bucket of the specified property.
  */
  private def value(property: SimpleProperty, value: Float): Float = {
    val (min, max) = property.range
    (value/property.maxWidth) * (max - min) + min
  }
 
  /**
  * Destroys this node, which also implies stopping any associated threads as well as its audio output.
  */
  override def destroy() = {
    this ! "STOP_ACTING"
    this.getGestureListeners.foreach(listener => {
      listener match {
        case actor: Actor => {actor ! "STOP_ACTING"}
        case noActor => {}
      }
    })
    this.exists = false 
    this.inEx = false
    this.removeTimeConnections()
    Ui.audioInterface ! StopAudioEvent(this.id)
    super.destroy()
  }  
  
  /**
  * Draws this node.
  */
  override def drawComponent(g: PGraphicsOpenGL) = {
    this.synchronized {
      super.drawComponent(g)
      this.properties.values.foreach(_.draw(g))
      g.noFill()
      g.strokeWeight(2)      
      val (x,y) = (this.getCenterPointLocal.getX, this.getCenterPointLocal.getY)
      
      val stepSize = 2*PI.toFloat/this.outputChannelNumber
      (0 until this.outputChannelNumber).foreach(index => {
        val color = AudioOutputChannels.colorFromIndex(index)
        if (this.isOutputChannelOpen(index)) g.stroke(color.getR, color.getG, color.getB, 150) else g.stroke(0, 0, 0, 50)
        g.arc(x, y, 2*this.radius - 2, 2*this.radius - 2, HALF_PI + index*stepSize, HALF_PI + (index+1)*stepSize)
      })
      
      g.fill(0,20,80,150)
      g.noStroke()
      (0 to this.activeInputChannel).foreach(item => {
        val (px,py) = Functions.positionOnCircle(x, y, this.radius, 2*math.Pi.toFloat, item, this.activeInputChannel + 1)
        g.ellipse(px, py, InputChannelItem.DotWidth, InputChannelItem.DotWidth)
      })
      
      
    }
  }  
  
  
  private def removeTimeConnections() = {    
    this.timeConnections.foreach(timeConnection => {
      this.timeConnections = this.timeConnections.filter(_ != timeConnection) //and remove them from this node
      timeConnection.timeNode.associatedPath.foreach(_ ! NodeDeletionEvent(timeConnection.connectionNode)) //propagate to path if exists
      Ui -= timeConnection.connectionNode //remove the connection node as well
      Ui -= timeConnection
    })      
  }  
   
  private def setupDeletionNode() = {
    val deleteCenter = Vec3d(this.getCenterPointLocal.getX + 2.5f*NodeType.Radius, this.getCenterPointLocal.getY)    
    val deleteNode = new DeleteNode(app, this, deleteCenter)     
    this.addChild(deleteNode)    
  }
  
  
  override def toXML = {
    val (x,y) = this.position
    val properties = "<properties>" + this.properties.values.map(_.toXML).foldLeft("")((p1, p2) => p1 + " " + p2) + "</properties>"
    "<node type = '" + this.nodeType.toString + "' x = '" + x + "' y = '" + y + "'>"+ properties + "</node>"
  }    

  
  
}
