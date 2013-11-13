package ui.events

import ui.paths._
import ui.properties.types._

object PathFastForwardEvent {

  def apply(time: Float) = {
    new PathFastForwardEvent(time)
  }
}

class PathFastForwardEvent(val time: Float) extends UiEvent("PATH_FAST_FORWARDED") {
}


