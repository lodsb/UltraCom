package ui.menus.context

import org.mt4j.Application

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import ui._
import ui.paths._
import ui.menus.main._
import ui.util._
import ui.audio._


object NodeContextMenu {  
  private var registry = Set[NodeContextMenu]()
  
  def +=(menu: NodeContextMenu) = {   
    this.registry += menu
  }
  
  def -=(menu: NodeContextMenu) = {
    this.registry -= menu
  }
  
  def isMenuVisible(node: Node) = {
    this.registry.exists(_.node == node)
  }  
}

abstract class NodeContextMenu(app: Application, val node: Node) extends ContextMenu(app) {

  protected[context] override def remove() = {
    NodeContextMenu -= this
    Ui -= this
  }  
  
}
