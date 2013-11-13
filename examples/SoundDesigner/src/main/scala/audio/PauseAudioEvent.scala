package ui.audio

object PauseAudioEvent {
  def apply(callerID: Int) = {
    new PauseAudioEvent(callerID)
  }
}

class PauseAudioEvent(callerID: Int) extends AudioEvent(callerID) {}             
