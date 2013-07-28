package ui.audio

object StopAudioEvent {
  def apply(callerID: Int) = {
    new StopAudioEvent(callerID)
  }
}

class StopAudioEvent(callerID: Int) extends AudioEvent(callerID) {}             
