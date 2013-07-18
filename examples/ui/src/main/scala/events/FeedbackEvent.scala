package ui.events

object FeedbackEvent {

  def apply(name: String, value: Float) = {
    new FeedbackEvent(name, value)
  }
  
  def apply(name: String) = {
    new FeedbackEvent(name, 0)
  }  
  
}

class FeedbackEvent(name: String, val value: Float) extends UiEvent(name) {
}


