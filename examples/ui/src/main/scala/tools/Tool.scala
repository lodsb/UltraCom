package ui.tools

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTPolygon

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

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.input._
import ui.properties._
import ui.properties.types._
import ui.util._

/**
* Companion object defining the basic look of tools.
*/
object Tool {
  val StrokeWeight = 1
  val StrokeAlpha = 70
  val ActiveStrokeAlpha = 170
  
  val Width = Ui.width/40f //width of a tool relative to the ui width
  val Height = Width*2.1f //height of a tool, already relative to ui (so do not change if you want to keep current proportions)
  val BottomHeight = Width/3.0f //height of the bottom area of a tool, already relative to ui 
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
class Tool(app: Application, center: (Float, Float), pType: PropertyType) extends MTPolygon(app, Array(new Vertex(center._1, center._2))) { 
    private var rotationAngle = 0.0f //the current rotation angle of this tool  
    private var editing = false //whether this tool is currently in edit mode
    
    this.setupRepresentation()
    this.setupInteraction()
    
    private def setupRepresentation() = {
      val color = this.color //deep copy of color
      color.setAlpha(Tool.StrokeAlpha)
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
    * Returns the specific color of this tool.
    * Note that this is a deep copy of this tool's property.
    */
    protected def color() = {
      this.propertyType.color
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
    
    
    private def shape = {
      import Tool._
      val color = this.color
      val x = center._1
      val y = center._2
      Array(
          new Vertex(x, y - Height/2, 0, color.getR(), color.getG(), color.getB(), 0), //top
          new Vertex(x - Width/2, y + Height/2 - BottomHeight, 0, color.getR(), color.getG(), color.getB(), 130), //left point
          new Vertex(x, y + Height/2, 0, color.getR(), color.getG(), color.getB(), 180), //bottom
          new Vertex(x + Width/2, y + Height/2 - BottomHeight, 0, color.getR(), color.getG(), color.getB(), 130), //right point
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
      this.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)     
      this.addGestureListener(classOf[TapProcessor], new ToolTapListener(this))
    }
    
    /**
    * Draws this tool.
    */
    override def drawComponent(g: PGraphics) = {
      import Tool._
      val color = this.color
      if (this.isEditing) color.setAlpha(ActiveStrokeAlpha) else color.setAlpha(StrokeAlpha)
      this.setStrokeColor(color)
      super.drawComponent(g)  
      g.noFill()
      g.stroke(color.getR, color.getG, color.getB, color.getAlpha)
      g.strokeWeight(StrokeWeight)
      g.arc(this.localTipPoint._1, this.localTipPoint._2 + ArcDelta, ArcWidth, ArcWidth, 5*PI/16, 11*PI/16)
      this.propertyType.drawSymbol(g, this)
    }
    
    /**
    * Sets the rotation angle of this tool, with the rotation point being the tip of this tool.
    */
    def setRotation(angle: Float): Unit = {
      this.setRotation(angle, this.tipPoint)
    }
    
    /**
    * Sets the rotation angle for this tool while also specifying the point around which to rotate.
    */
    def setRotation(angle: Float, point: (Float, Float)): Unit = {
      val (x,y) = point
      this.rotateZ(Vec3d(x,y), -this.rotationAngle, TransformSpace.GLOBAL) //reset rotation of this tool
      this.rotateZ(Vec3d(x,y), angle, TransformSpace.GLOBAL) //then apply new rotation
      this.rotationAngle = angle //finally update current rotation angle
    }
}
