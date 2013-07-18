package ui.events

import ui.paths._
import ui.properties.types._

object ManipulationEvent {

  def apply(propertyType: PropertyType, value: Float) = {
    new ManipulationEvent(propertyType, value)
  }
}

class ManipulationEvent(val propertyType: PropertyType, val value: Float) extends UiEvent("MANIPULATED") {
}


