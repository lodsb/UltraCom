package ui.events

import ui.paths._

object PathPlaybackEvent {
  def apply(path: Path, isPlaying: Boolean) = {
    new PathPlaybackEvent(path, isPlaying)
  }
}

class PathPlaybackEvent(val path: Path, val isPlaying: Boolean) extends UiEvent("PATH_PLAYBACK") {
}
