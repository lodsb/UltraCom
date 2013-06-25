package ui.audio

object AudioEvent {
  def apply(channel: Int, x: Int, y: Int, pitch: Float, volume: Float) = {
    new AudioEvent(channel, x, y, pitch, volume)
  }
}

class AudioEvent(val channel: Int, val x: Int, val y: Int, val pitch: Float, val volume: Float) {
  
  override def toString = {
    "(" + channel + ", " + x + ", " + y + ", " + pitch + ", " + volume + ")"
  }
  
}
