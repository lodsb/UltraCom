package ui.events

import ui.paths._
import ui.properties.types._

object NodeManipulationEvent {

  def apply(propertyType: PropertyType, value: Float) = {
    new NodeManipulationEvent(propertyType, value)
  }
}

class NodeManipulationEvent(val propertyType: PropertyType, val value: Float) extends UiEvent("NODE_MANIPULATED") {
}


