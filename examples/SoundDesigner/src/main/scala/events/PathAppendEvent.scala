package ui.events

import ui.paths._

object PathAppendEvent {

  def apply(path: Path) = {
    new PathAppendEvent(path)
  }
}

class PathAppendEvent(val path: Path) extends UiEvent("PATH_APPENDED") {
}
