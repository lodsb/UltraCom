package ui.events

import ui.tools._

object RegisterToolEvent {

  def apply(tool: Tool) = {
    new RegisterToolEvent(tool)
  }
}

class RegisterToolEvent(val tool: Tool) extends UiEvent("REGISTER_TOOL") {
}


