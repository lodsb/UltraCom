package ui.events

import ui.paths.types._

object PathPlaybackTypeEvent {

  def apply(playbackType: NodeType) = {
    new PathPlaybackTypeEvent(playbackType)
  }
}

class PathPlaybackTypeEvent(val playbackType: NodeType) extends UiEvent("PLAYBACK_TYPE_CHANGED") {
}
