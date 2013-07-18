package ui.events

import ui.paths.types._

object NodeTypeFeedforwardEvent {

  def apply(nodeType: NodeType, value: Float) = {
    new NodeTypeFeedforwardEvent(nodeType, value)
  }
}

class NodeTypeFeedforwardEvent(val nodeType: NodeType, value: Float) extends FeedforwardEvent("NODE_TYPE_FEEDFORWARD", value) {
}


