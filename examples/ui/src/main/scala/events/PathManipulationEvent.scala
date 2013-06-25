package ui.events

import ui.paths._
import ui.properties.types._

object PathManipulationEvent {

  def apply(connection: ManipulableBezierConnection, connectionParameter: Float, propertyType: PropertyType, manipulationRadius: Float, value: Float) = {
    new PathManipulationEvent(connection, connectionParameter, propertyType, manipulationRadius, value)
  }
}

class PathManipulationEvent(val connection: ManipulableBezierConnection, val connectionParameter: Float, val propertyType: PropertyType, val manipulationRadius: Float, val value: Float) extends UiEvent("PATH_MANIPULATED") {
}


