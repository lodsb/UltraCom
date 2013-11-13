package ui.audio

object ResumeAudioEvent {
  def apply(callerID: Int) = {
    new ResumeAudioEvent(callerID)
  }
}

class ResumeAudioEvent(callerID: Int) extends AudioEvent(callerID) {}             
