package ui.events

import ui.paths._
import ui.properties.types._

object PathRewindEvent {

  def apply(time: Float) = {
    new PathRewindEvent(time)
  }
}

class PathRewindEvent(val time: Float) extends UiEvent("PATH_REWINDED") {
}
