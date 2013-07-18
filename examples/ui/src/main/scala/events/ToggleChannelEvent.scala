package ui.events

object ToggleChannelEvent {

  def apply(channel: Int) = {
    new ToggleChannelEvent(channel)
  }  
  
}

class ToggleChannelEvent(val channel: Int) extends UiEvent("TOGGLE_CHANNEL") {
}


