package ui.events

import ui.paths._
import ui.tools._

object ManipulationEvent {

  def apply(tool: Tool, value: Float) = {
    new ManipulationEvent(tool, value)
  }
}

class ManipulationEvent(val tool: Tool, val value: Float) extends UiEvent("MANIPULATED") {
}


