package ui.events

object ToggleInputChannelEvent {

  def apply(channel: Int) = {
    new ToggleInputChannelEvent(channel)
  }  
  
}

class ToggleInputChannelEvent(val channel: Int) extends UiEvent("TOGGLE_INPUT_CHANNEL") {
}


