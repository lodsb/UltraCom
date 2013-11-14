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
import ui.util._


object ChannelContextMenu {
  def apply(app: Application, node: Node) = {
    new ChannelContextMenu(app, node)
  }
}

class ChannelContextMenu(app: Application, node: Node) extends NodeContextMenu(app, node) {
  
  //since this is a node context menu and thus a child of the node, we have to calculate positions in local node space
  val nodeVector = node.getCenterPointLocal
  for (index <- 0 until Ui.audioInterface.Channels) {
    val (x,y) = Functions.positionOnCircle(nodeVector.getX, nodeVector.getY, 2.5f*node.radius, 2*math.Pi.toFloat, index, Ui.audioInterface.Channels + MIDIInputChannels.InputChannels) //get position around node in local space
    this.addChild(OutputChannelItem(app, this, Vec3d(x,y), index))
  } 
  
  
  for (index <- Ui.audioInterface.Channels until Ui.audioInterface.Channels + MIDIInputChannels.InputChannels) {
    val (x,y) = Functions.positionOnCircle(nodeVector.getX, nodeVector.getY, 2.5f*node.radius, 2*math.Pi.toFloat, index, Ui.audioInterface.Channels + MIDIInputChannels.InputChannels) //get position around node in local space
    this.addChild(InputChannelItem(app, this, Vec3d(x,y), index - Ui.audioInterface.Channels))
  } 
 
  
}
