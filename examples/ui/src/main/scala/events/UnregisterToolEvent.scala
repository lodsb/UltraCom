package ui.events

import ui.tools._

object UnregisterToolEvent {

  def apply(tool: Tool) = {
    new UnregisterToolEvent(tool)
  }
}

class UnregisterToolEvent(val tool: Tool) extends UiEvent("UNREGISTER_TOOL") {
}


