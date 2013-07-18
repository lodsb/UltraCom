package ui.menus.context

import org.mt4j.Application

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}
import org.mt4j.components.TransformSpace

import ui._
import ui.paths._
import ui.menus.main._
import ui.util._
import ui.audio._
import ui.properties.types._


object ToolContextMenu {
  
  val Radius = 30
  
  def apply(app: Application, position: Vector3D) = {
    new ToolContextMenu(app, position)
  }
  
  private var registry = Set[ToolContextMenu]()
  
  def +=(menu: ToolContextMenu) = {   
    this.registry += menu
  }
  
  def -=(menu: ToolContextMenu) = {
    this.registry -= menu
  }
  
  def isMenuInProximity(vector: Vector3D) = {
    this.registry.exists(menu => {
      Vector.euclideanDistance((menu.position.getX, menu.position.getY), (vector.getX, vector.getY)) <= 2*Radius
    })
  }  
}

class ToolContextMenu(app: Application, val position: Vector3D) extends ContextMenu(app) {

  import ToolContextMenu._
  
  
  val propertyType = Array(SpeedPropertyType, VolumePropertyType, PitchPropertyType)
  for (index <- 0 until propertyType.size) { //for each property type
    val (x,y) = this.positionOnCircle(position, Radius, 2*math.Pi.toFloat, index, propertyType.size) //get position around center in local space
    val (tx,ty) = Functions.gradient((position.getX, position.getY), (x,y))
    val angle = Functions.gradientToDegrees((ty,tx))
    val toolItem = ToolItem(app, this, Vec3d(x,y), propertyType(index))
    toolItem.rotateZ(toolItem.getCenterPointLocal, -angle, TransformSpace.LOCAL)
    this.addChild(toolItem)
  }
 
  
  protected[context] override def remove() = {
    ToolContextMenu -= this
    Ui -= this
  }  
  
}
