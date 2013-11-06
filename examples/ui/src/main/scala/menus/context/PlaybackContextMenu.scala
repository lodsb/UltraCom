package ui.menus.context

import org.mt4j.Application

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import ui._
import ui.paths._
import ui.paths.types._
import ui.menus.main._
import ui.util._


object PlaybackContextMenu {
  def apply(app: Application, node: Node) = {
    new PlaybackContextMenu(app, node)
  }
}

class PlaybackContextMenu(app: Application, node: Node) extends NodeContextMenu(app, node) {
  
  val playbackType = Array[EndNodeType](StopNodeType, RepeatNodeType, ReverseNodeType)
  
  //since this is a node context menu and thus a child of the node, we have to calculate positions in local node space
  val nodeVector = node.getCenterPointLocal
  for (index <- 0 until playbackType.size) {
    val (x,y) =  Functions.positionOnCircle(nodeVector.getX, nodeVector.getY, 2.5f*node.radius, 2*math.Pi.toFloat, index, playbackType.size) //get position around node in local space
    //val (x,y) = Functions.transform(node.position, (localTangent.getY, -localTangent.getX), position) //transformation using orhtogonal vector wrt global tangent
    this.addChild(PlaybackItem(app, this, Vec3d(x,y), playbackType(index)))
  } 
  
}
