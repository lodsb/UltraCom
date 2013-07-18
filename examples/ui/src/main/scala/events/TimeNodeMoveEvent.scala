package ui.events

import ui.paths._

object TimeNodeMoveEvent {

  def apply(node: TimeNode, x: Float, y: Float) = {
    new TimeNodeMoveEvent(node, x, y)
  }
}

class TimeNodeMoveEvent(override val node: TimeNode, val x: Float, val y: Float) extends NodeEvent("TIME_NODE_MOVED", node) {
}
