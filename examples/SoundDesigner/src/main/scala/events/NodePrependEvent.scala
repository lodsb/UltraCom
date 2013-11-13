package ui.events

import ui.paths._

object NodePrependEvent {

  def apply(node: Node) = {
    new NodePrependEvent(node)
  }
}

class NodePrependEvent(node: Node) extends NodeEvent("NODE_PREPENDED", node) {
}
