package ui.events

import org.mt4j.input.inputData.AbstractCursorInputEvt

object CustomCursorEvent {

  abstract class EventType {}
  object Started extends EventType {}
  object Updated extends EventType {}    
  object Ended extends EventType {}  
  
  def apply(cursorEvent: AbstractCursorInputEvt, time: Long, eventType: EventType) = {
    new CustomCursorEvent(cursorEvent, time, eventType)
  }
}

class CustomCursorEvent(val cursorEvent: AbstractCursorInputEvt, val time: Long, val eventType: CustomCursorEvent.EventType) {
}
