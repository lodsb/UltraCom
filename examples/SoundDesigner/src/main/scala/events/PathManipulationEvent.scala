package ui.events

import ui.paths._
import ui.tools._

object PathManipulationEvent {

  def apply(connection: ManipulableBezierConnection, connectionParameter: Float, tool: Tool, manipulationRadius: Float, value: Float) = {
    new PathManipulationEvent(connection, connectionParameter, tool, manipulationRadius, value)
  }
}

class PathManipulationEvent(val connection: ManipulableBezierConnection, val connectionParameter: Float, tool: Tool, val manipulationRadius: Float, value: Float) extends ManipulationEvent(tool, value) {
}


