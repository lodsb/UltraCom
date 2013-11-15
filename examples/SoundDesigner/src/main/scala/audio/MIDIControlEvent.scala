package ui.audio

import scala.collection.mutable._

object MIDIControlEvent {
  
  final val LowestNumber = 21
  
  def apply(controllerChannel: Int, controllerNumber: Int, controllerValue: Float) = {
    new MIDIControlEvent(controllerChannel, controllerNumber, controllerValue)
  }
}

class MIDIControlEvent(val controllerChannel: Int, val controllerNumber: Int, val controllerValue: Float) {
}
