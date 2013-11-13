package ui.events

import ui.paths._

object TimeNodeAddEvent {

  def apply(node: TimeNode) = {
    new TimeNodeAddEvent(node)
  }
}

class TimeNodeAddEvent(override val node: TimeNode) extends NodeEvent("TIME_NODE_ADDED", node) {
}
