package ui.events

import ui.paths._

object NodeMoveEvent {

  def apply(node: Node) = {
    new NodeMoveEvent(node)
  }
}

class NodeMoveEvent(node: Node) extends NodeEvent("NODE_MOVED", node) {
}
