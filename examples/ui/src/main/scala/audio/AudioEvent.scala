package ui.audio

import scala.collection.mutable._

object AudioEvent {
  def apply(channels: Array[Int], x: Int, y: Int, pitch: Float, volume: Float) = {
    new AudioEvent(channels, x, y, pitch, volume)
  }
}

class AudioEvent(val channels: Array[Int], val x: Int, val y: Int, val pitch: Float, val volume: Float) {
  
  override def toString = {
    "(" + (channels: ArrayOps[Int]).toString() + ", " + x + ", " + y + ", " + pitch + ", " + volume + ")"
  }
  
}
