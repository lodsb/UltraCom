package ui.audio

import scala.collection.mutable._

object MIDIControlEvent {
  
  final val LowestID = 21
  
  def apply(controllerID: Int, controllerValue: Float) = {
    new MIDIControlEvent(controllerID, controllerValue)
  }
}

class MIDIControlEvent(val controllerID: Int, val controllerValue: Float) {
}
