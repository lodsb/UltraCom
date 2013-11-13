package ui.events

import ui.paths._

object NodeAppendEvent {

  def apply(node: Node) = {
    new NodeAppendEvent(node)
  }
}

class NodeAppendEvent(node: Node) extends NodeEvent("NODE_APPENDED", node) {
}
