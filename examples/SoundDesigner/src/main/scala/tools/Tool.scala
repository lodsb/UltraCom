package ui.tools

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.bounds.BoundsArbitraryPlanarPolygon

import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.InertiaDragAction

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.Vec3d

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.input._
import ui.properties._
import ui.properties.types._
import ui.util._
import ui.events._
import ui.usability._
import processing.opengl.PGraphicsOpenGL

/**
* Companion object defining the basic look of tools.
*/
object Tool {
  val StrokeWeight = 1
  val StrokeAlpha = 70
  val ActiveStrokeAlpha = 170
  
  val Width = (Ui.width/32.0f).toInt //width of a tool relative to the ui width
  val Height = (Width*2.1f).toInt //height of a tool, already relative to ui (so do not change if you want to keep current proportions)
  val BottomHeight = (Width/3.0f).toInt //height of the bottom area of a tool, already relative to ui 
  val ArcDelta = 2*Height/7.0f //distance from arc center to tip of tool
  val ArcWidth = Height/4.0f
  
  def apply(app: Application, center: (Float, Float), propertyType: PropertyType) = {
    new Tool(app, center, propertyType)
  }
}


/**
* This class represents a tool.
* Tools are used to alter the properties of paths.
*/
class Tool(app: Application, center: (Float, Float), pType: PropertyType) extends MTPolygon(app, Array(new Vertex(0, 0))) with Feedback { 
    private var rotationAngle = 0.0f //the current rotation angle (in degrees) of this tool
    private var editing = false //whether this tool is currently in edit mode
    private var currentColor = this.propertyType.color
 
    this.setupRepresentation()          
    this.setupInteraction()
    
    private def setupRepresentation() = {
      this.globalPosition := Vec3d(center._1, center._2)
      val color = this.color //deep copy of color
      color.setA(Tool.StrokeAlpha)
      this.setVertices(this.shape)
      this.setStrokeWeight(Tool.StrokeWeight)
      this.setStrokeColor(color)
    }
    
    def propertyType = {
      this.pType
    }
    
    def isEditing_=(edit: Boolean) = {
      this.editing = edit
    }
  
    def isEditing = {
      this.editing
    }   
    
    /**
    * Returns a deep copy of this tool's current color.
    */
    def color() = {
      new MTColor(this.currentColor.getR, this.currentColor.getG, this.currentColor.getB, this.currentColor.getA)
    }
    
    protected def setColor(col: MTColor) = {
      this.currentColor = col
    }
    
    
    def manipulationRadius = {
      Tool.ArcWidth/2
    }
    
    /**
    * Returns the global position of this tool's tip.
    */
    def tipPoint = {
      import Tool._
      val center = this.getCenterPointLocal //get local center point
      val localTipVector = Vec3d(center.getX, center.getY - Height/2) //calculate local tip point
      val globalTipVector = this.localToGlobal(localTipVector) //convert to global point
      (globalTipVector.getX, globalTipVector.getY)
    }

    /**
    * Returns the local position of this tool's tip.
    */    
    def localTipPoint = {
      import Tool._
      val center = this.getCenterPointLocal //get local center point
      val localTipVector = Vec3d(center.getX, center.getY - Height/2) //calculate local tip point
      (localTipVector.getX, localTipVector.getY)      
    }
    
    /**
    * Returns the global position of this tool's center.
    */
    def centerPoint = {
      val c = this.getCenterPointGlobal
      (c.getX, c.getY)
    }
    
    
    /**
    * Returns the global position of this tool's center.
    */
    def position = {
      this.centerPoint
    }
   
    /**
    * Returns the local center position of this tool's symbol.
    */
    def localSymbolPoint = {
      import Tool._
      val (tx, ty) = this.tipPoint //global tip, as tuple
      val localTip = this.globalToLocal(Vec3d(tx, ty)) //local tip, as Vec3d
      (localTip.getX, localTip.getY + Height - 2*BottomHeight)
    }
    
    /**
    * Returns whether the specified point is in the edit area of this tool, provided the point is on the tool.
    */
    def pointInEditArea(point: (Float, Float)) = {
      import Tool._
      val (tx, ty) = this.localTipPoint
      val localArcPoint = (tx, ty + ArcDelta) //local arc center point
      Vector.euclideanDistance(localArcPoint, point) <= ArcWidth/2 || point._2 <= localArcPoint._2 //inside arc or above
    }
    
    
    private def shape: Array[Vertex] = {
      this.shape(this.color)
   }
   
    private def shape(color: MTColor): Array[Vertex] = {
      import Tool._
      val centerPos = this.getCenterPointLocal
      val (x, y) = (centerPos.getX, centerPos.getY)
      Array(
          new Vertex(x, y - Height/2, 0, color.getR(), color.getG(), color.getB(), 0), //top
          new Vertex(x - Width/2, y + Height/2 - BottomHeight, 0, color.getR(), color.getG(), color.getB(), 150), //left point
          new Vertex(x, y + Height/2, 0, color.getR(), color.getG(), color.getB(), 200), //bottom
          new Vertex(x + Width/2, y + Height/2 - BottomHeight, 0, color.getR(), color.getG(), color.getB(), 150), //right point
          new Vertex(x, y - Height/2, 0, color.getR(), color.getG(), color.getB(), 0) //back to top
      )
   }   
    
    
    private def setupInteraction() = {
      //remove defaults
      this.unregisterAllInputProcessors() //no default rotate, scale & drag processors
      this.removeAllGestureEventListeners() //no default listeners as well
            
        
      //register input processors
      this.registerInputProcessor(new DragProcessor(app))
      val tapProcessor = new TapProcessor(app)
      tapProcessor.setEnableDoubleTap(true)
      this.registerInputProcessor(tapProcessor)
      
      //add gesture listeners
      this.addGestureListener(classOf[DragProcessor], new ManipulatingDragAction(app, this)) 
      //this.addGestureListener(classOf[DragProcessor], new DefaultDragAction())
      //this.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)     
      this.addGestureListener(classOf[TapProcessor], new ToolTapListener(this))
    }
    
    /**
    * Draws this tool.
    */
    override def drawComponent(g: PGraphicsOpenGL) = {
      import Tool._
      val color = this.color
      if (this.isEditing) color.setA(ActiveStrokeAlpha) else color.setA(StrokeAlpha)
      this.setStrokeColor(color)
      super.drawComponent(g)  
      g.noFill()
      g.stroke(color.getR, color.getG, color.getB, color.getA)
      g.strokeWeight(StrokeWeight)
      g.arc(this.localTipPoint._1, this.localTipPoint._2 + ArcDelta, ArcWidth, ArcWidth, 5*PI/16, 11*PI/16)
      //TODO maybe add more affordance
      this.propertyType.drawSymbol(g, this.localSymbolPoint, color)
    }
    
    /**
    * Sets the rotation angle (in degrees) of this tool in global space, with the rotation point being the tip of this tool.
    */
    def setRotation(angle: Float): Unit = {
      this.setRotation(angle, this.tipPoint)
    }
    
    /**
    * Sets the rotation angle (in degrees) of this tool in global space while also specifying the point around which to rotate.
    */
    def setRotation(angle: Float, point: (Float, Float)): Unit = {
      val (x,y) = point
      this.rotateZ(Vec3d(x,y), -this.rotationAngle, TransformSpace.GLOBAL) //reset rotation of this tool
      this.rotateZ(Vec3d(x,y), angle, TransformSpace.GLOBAL) //then apply new rotation
      this.rotationAngle = angle //finally update current rotation angle
    }
    
    
    override def giveFeedback(event: FeedbackEvent) = {
      if (event.name == "ILLEGAL ACTION") {
        wrongActionAnimation.start()
      }
    }
    
    
    private def wrongActionAnimation = {
      val color = this.color
      val oldR = color.getR
      val maxR = 200
      val oldG = color.getG
      val oldB = color.getB
      val oldAlpha = color.getA
      val me = this
      val interpolator = new MultiPurposeInterpolator(oldR, maxR, 150, 0.0f, 1.0f, 1)
      val animation = new Animation("WRONG ACTION FADE IN", interpolator, this)
      animation.addAnimationListener(new IAnimationListener() { 
        def processAnimationEvent(ae: AnimationEvent) {
          if(ae.getId() == AnimationEvent.ANIMATION_ENDED) { //if the animation has been played back uninterrupted
              //then start animation back to default appearance of tool
              val interpolator2 = new MultiPurposeInterpolator(maxR, oldR, 500, 0.0f, 1.0f, 1)
              val animation2 = new Animation("WRONG ACTION FADE OUT", interpolator2, me)
              animation2.addAnimationListener(new IAnimationListener() { 
                def processAnimationEvent(ae: AnimationEvent) {
                  me.setColor(new MTColor(ae.getValue, oldG - oldG*ae.getValue/maxR, oldB - oldB*ae.getValue/maxR, oldAlpha)) //works because oldR is 0 in this case
                  me.setVertices(me.shape(me.color))
                }
              }).start()
          }
          else {
            me.setColor(new MTColor(ae.getValue, oldG - oldG*ae.getValue/maxR, oldB - oldB*ae.getValue/maxR, oldAlpha)) //works because oldR is 0 in this case
            me.setVertices(me.shape(me.color))
          }
        }
      })
      animation
    }

    
}
