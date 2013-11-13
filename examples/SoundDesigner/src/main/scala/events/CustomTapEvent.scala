package ui.events

object CustomTapEvent {

  abstract class EventType {}
  object Started extends EventType {}
  object Updated extends EventType {}    
  object Ended extends EventType {}  
  
  def apply(id: Long, x: Float, y: Float, time: Long, eventType: EventType) = {
    new CustomTapEvent(id, x, y, time, eventType)
  }
}

class CustomTapEvent(val id: Long, val x: Float, val y: Float, val time: Long, val eventType: CustomTapEvent.EventType) {
}
