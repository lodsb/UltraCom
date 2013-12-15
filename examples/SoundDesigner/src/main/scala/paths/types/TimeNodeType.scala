package ui.paths.types

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.visibleComponents.StyleInfo

import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.IMTInputEventListener
import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.inputData.MTFingerInputEvt

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.InertiaDragAction

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import org.mt4j.util.Color

import ui._
import ui.input._
import ui.paths._
import ui.properties.types._

/**
* This is the time node type.
* A node of this type is bound to a path and associated with a specific playback position on that path.
* It can be used to create a link to another path. The latter will then start its playback
* every time the aforementioned specific playback position is reached.
*/
object TimeNodeType extends NodeType{

  private val color = SpeedPropertyType.color
  protected val TimeBackgroundColor = Color(color.getR, color.getG, color.getB, 100)
  protected val TimeStrokeColor = Color(color.getR, color.getG, color.getB, 150)
  val Size = 0.4f
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)
    
    node.registerInputProcessor(new DragProcessor(app))
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)
    
    node.associatedPath.foreach(_ => {
      node match {
        case timeNode: TimeNode => timeNode.addGestureListener(classOf[DragProcessor], new PathBoundedDragAction(timeNode))
        case otherNode => {}
      }
    })
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)  
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
  }    

  override def vicinity = {
    this.radius * 3f
  } 
  
   override def toString = {
     "TimeNode"
   }
   
  override def size = {
    this.Size
  } 

  override def backgroundColor = {
   this.TimeBackgroundColor
  }
  
  override def strokeColor = {
   this.TimeStrokeColor
  }  

}
