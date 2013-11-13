package ui.events

import ui.paths.types._

object FeedforwardEvent {

  def apply(name: String, value: Float) = {
    new FeedforwardEvent(name, value)
  }
}

class FeedforwardEvent(name: String, val value: Float) extends UiEvent(name) {
}


