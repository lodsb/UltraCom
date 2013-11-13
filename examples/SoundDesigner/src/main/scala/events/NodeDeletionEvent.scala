package ui.events

import ui.paths._

object NodeDeletionEvent {

  def apply(node: Node) = {
    new NodeDeletionEvent(node)
  }
}

class NodeDeletionEvent(node: Node) extends NodeEvent("NODE_DELETED", node) {
}
