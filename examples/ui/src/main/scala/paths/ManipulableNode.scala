package ui.paths

import org.mt4j.Application
import org.mt4j.util.math.Vector3D
import org.mt4j.types.{Vec3d}

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.Color

import processing.core.PGraphics

import scala.actors._

import ui.paths.types._
import ui.properties._
import ui.properties.types._
import ui.events._
import ui.audio._
import ui._
import ui.util._


object ManipulableNode {
  
  def apply(app: Application, center: (Float, Float)) = {
      new ManipulableNode(app, Vec3d(center._1, center._2))
  }
  
  def apply(app: Application, center: Vector3D) = {
      new ManipulableNode(app, center)
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
class ManipulableNode(app: Application, center: Vector3D) extends Node(app, ManipulableNodeType, None, center) with Actor with AudioChannels {
  private var exists = true
  private var isPlaying = false
  private var playbackPos = 0.0f

  private var timeConnections = List[TimeConnection]()
  
  this.setupDeletionNode()
  this.start() //note: do not put this in an override setup method, it won't work...
  
  private var properties = Map(
    (VolumePropertyType -> SimpleVolumeProperty(this)),
    (PitchPropertyType -> SimplePitchProperty(this))
  )  

  
  def act = {          
    println("node: starting to act!")
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()
    var timeDiff = 0.0f //passed time in milliseconds
    
    var ignoreNextTogglePlayback = false //whether the next play/pause playback event is to be ignored

    while (this.exists) {
      receive {
        
        case event: ManipulationEvent => {
          this.synchronized {
            this.updateProperty(event.propertyType, event.value) 
          }
        }
        
        case event: ToggleChannelEvent => {
          this.synchronized {
            this.toggleChannel(event.channel)
          }
        }    
  
        case event: TimeConnectionAddEvent => {
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
          this.synchronized {
            this.timeConnections = this.timeConnections.filter(_ != event.connection)
          }
        }
        
        case event: NodeMoveEvent => {
          this.synchronized {
            this.timeConnections.foreach(timeConnection => {
              timeConnection.updateConnectionNode()
            })
          }
        }
                     
        case event: UiEvent => { //a 'simple' ui event
          this.synchronized {        
            
            if (event.name == "IGNORE_NEXT_TOGGLE_PLAYBACK") {
              ignoreNextTogglePlayback = true
            }
                      
            else if (event.name == "IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK") {
              ignoreNextTogglePlayback = false
            }                       
            
            else if (event.name == "START_PLAYBACK") {
              if (ignoreNextTogglePlayback) ignoreNextTogglePlayback = false
              else {   
                if (!this.isPlaying) {
                  lastTime = System.nanoTime() //init time
                  Synthesizer ! AudioEvent(this.collectOpenChannels, math.round(this.position._1), math.round(this.position._2), this.properties(PitchPropertyType)(), this.properties(VolumePropertyType)())   
                  this.isPlaying = true
                  this ! UiEvent("PLAY")
                }
              }
            }
            
            else if (event.name == "PLAY") {
              if (this.isPlaying) {
                currentTime = System.nanoTime()
                val passedTime = (currentTime - lastTime)/1000000.0f
                if (passedTime <= 1000) {
                  this.playbackPos = passedTime/1000
                  this ! event
                }
                else {
                  this.isPlaying = false
                  this.playbackPos = 0.0f
                }
              }              
            }

            else println("OH NO!")
          }
        }
      }
      Thread.sleep(5)
    }
  } 
  
  def playbackPosition: Float = {
    this.playbackPos
  }
  
  /**
  * Updates the specified property.
  */
  def updateProperty(propertyIdentifier: PropertyType, value: Float) = {
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
  * Destroys this node, which also implies stopping any associated threads.
  */
  override def destroy() = {
    this.exists = false 
    super.destroy()
  }  
  
  /**
  * Draws this node.
  */
  override def drawComponent(g: PGraphics) = {
    this.synchronized {
      super.drawComponent(g)
      this.properties.values.foreach(_.draw(g))
    }
  }  
  
  
  private def removeTimeConnections() = {    
    this.timeConnections.foreach(timeConnection => {
      this.timeConnections = this.timeConnections.filter(_ != timeConnection) //and remove them from this path
      timeConnection.timeNode.associatedPath.foreach(_ ! NodeDeletionEvent(timeConnection.connectionNode)) //propagate to other path if exists
      Ui -= timeConnection.connectionNode //remove the connection node as well
      Ui -= timeConnection
    })      
  }  
  
  
  
  def setupDeletionNode() = {
    val deleteCenter = Vec3d(this.getCenterPointLocal.getX + 2.5f*NodeType.Radius, this.getCenterPointLocal.getY)    
    val deleteNode = new DeleteNode(app, deleteCenter)     
    this.addChild(deleteNode)    
  }
  


  private class DeleteNode(app: Application, center: Vector3D) extends Node(app, DeleteManipulationNodeType, None, center) {   
    private val manipulableNode = ManipulableNode.this
    
    private val DeleteNodeRadius = NodeType.Radius * ControlNodeType.Size.toFloat
    private val LineColor = Color(0, 130, 130, 150)  
    private val DotNumber = 15
      
    override def drawComponent(g: PGraphics) = {
      super.drawComponent(g)
      
      //draw dotted line from button to node
      g.stroke(0, 0, 0, LineColor.getAlpha)
      val deletableNodeVector = this.parentToLocal(manipulableNode.getCenterPointLocal)
      val deletableNodePosition = (deletableNodeVector.getX, deletableNodeVector.getY)
      val deleteNodePosition = (this.getCenterPointLocal.getX, this.getCenterPointLocal.getY)
      val distance = Vector.euclideanDistance(deletableNodePosition, deleteNodePosition)
      val line = Functions.line(deletableNodePosition, deleteNodePosition)_
      (0 until this.DotNumber).foreach(dot => {
        val (x,y) = line(dot/this.DotNumber.toFloat)
        g.point(x, y)
      })
    }   
  }
  
  private object DeleteManipulationNodeType extends NodeType {  
    private val manipulableNode = ManipulableNode.this
    private val DeleteBackgroundColor = Color(0, 0, 0, 50)
    private val DeleteStrokeColor = Color(0, 0, 0, 0)
    val Size = 0.5f
    
    protected override def setupInteractionImpl(app: Application, deletionNode: Node) = {
      deletionNode.setScale(this.size)  
      
      val tapProcessor = new TapProcessor(app)
      tapProcessor.setEnableDoubleTap(true)
      deletionNode.registerInputProcessor(tapProcessor)      
        
      deletionNode.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
        override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
          gestureEvent match {
            case tapEvent: TapEvent => {
                if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
                  val replacementNode = IsolatedNode(Ui, manipulableNode.position)
                  manipulableNode.removeTimeConnections()
                  Ui -= manipulableNode
                  Ui += replacementNode
                }
                true
            }
            case someEvent => {
                println("I can't process this particular event: " + someEvent.toString)
                false
            }
          }
        }
      })   
    }

      
    override def size = {
      this.Size
    }   
    
    override def backgroundColor = {
      this.DeleteBackgroundColor
    }
    
    override def strokeColor = {
      this.DeleteStrokeColor
    }
    
      
  }  

  
  
}
