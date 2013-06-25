package ui.paths.types

import org.mt4j.Application
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.util.Color
import org.mt4j.types.{Vec3d}

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.util._
import ui.paths._
import ui.audio._
import ui.events._
import ui.properties.types._

/**
* This is the play-timbre node type.
* A node associated with this type provides a means to play back a single timbre by tapping.
*/
object PlayTimbreNodeType extends StartNodeType {

  private val DeleteButtonRadius = NodeType.Radius * ControlNodeType.Size.toFloat
  protected[types] val LineColor = Color(0, 130, 130, 150)  
  private val MaxDotNumber = 500
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    super.setupInteractionImpl(app, node)
    node.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
              if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                println("play down")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("play up")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                node match {
                  case manipulableNode: ManipulableNode => {
                    manipulableNode ! UiEvent("START_PLAYBACK") 
                  }
                  case otherNode => {
                    Synthesizer ! AudioEvent(0, math.round(node.position._1), math.round(node.position._2), PitchPropertyType.mean, VolumePropertyType.mean)   
                  }
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
    })    
    println("setting up interaction on timbre node type")
    
    val deleteCenter = Vec3d(node.position._1 + 3*NodeType.Radius, node.position._2)
    val deleteButton = new MTEllipse(app, deleteCenter, DeleteButtonRadius, DeleteButtonRadius) {
      this.setNoFill(true)
      this.setStrokeWeight(ControlNodeType.StrokeWeight)
      this.setStrokeColor(ControlNodeType.StrokeColor)
      this.unregisterAllInputProcessors()
      this.removeAllGestureEventListeners()
      
      val tapProcessor = new TapProcessor(app)
      tapProcessor.setEnableDoubleTap(true)
      this.registerInputProcessor(tapProcessor)      
      this.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
          override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
                gestureEvent match {
                    case tapEvent: TapEvent => {
                        if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
                          val replacementNode = Node(Ui, IsolatedNodeType, None, Vec3d(node.position._1, node.position._2))
                          Ui -= node
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
      
      override def drawComponent(g: PGraphics) = {
        super.drawComponent(g)
        //draw dotted line from button to node
        g.stroke(0, 0, 0, LineColor.getAlpha)
        val nodeVec = this.globalToLocal(Vec3d(node.position._1, node.position._2))
        val nodePoint = (nodeVec.getX, nodeVec.getY)
        val buttonPoint = (deleteCenter.getX, deleteCenter.getY)
        val distance = Vector.euclideanDistance(buttonPoint, nodePoint)
        val dots = math.round(distance/Ui.width * MaxDotNumber)
        val line = Functions.line(buttonPoint, nodePoint)_
        (0 to dots - 1).foreach(dot => {
          val (x,y) = line(dot/dots.toFloat)
          g.point(x, y)
        })
      }
      
    }
    node.addChild(deleteButton)
    
  }  
  
  override def drawSymbol(g: PGraphics, node: Node) = {
    PlayNodeType.drawSymbol(g, node)
  }
  
}
