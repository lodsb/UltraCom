package ui.events

object ToggleOutputChannelEvent {

  def apply(channel: Int) = {
    new ToggleOutputChannelEvent(channel)
  }  
  
}

class ToggleOutputChannelEvent(val channel: Int) extends UiEvent("TOGGLE_OUTPUT_CHANNEL") {
}


