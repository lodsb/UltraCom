package ui.events

import ui.paths._
import ui.tools._

object RegisterToolWithLocationEvent {

  def apply(tool: Tool, connection: ManipulableBezierConnection, connectionParameter: Float, manipulationRadius: Float) = {
    new RegisterToolWithLocationEvent(tool, connection, connectionParameter, manipulationRadius)
  }
}

class RegisterToolWithLocationEvent(tool: Tool, val connection: ManipulableBezierConnection, val connectionParameter: Float, val manipulationRadius: Float) extends RegisterToolEvent(tool) {
}

