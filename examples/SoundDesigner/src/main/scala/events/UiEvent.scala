package ui.events

object UiEvent {
  def apply(string: String) = {
    new UiEvent(string)
  }
}

class UiEvent(string: String) {
  
  def name = {
    string
  }
}
