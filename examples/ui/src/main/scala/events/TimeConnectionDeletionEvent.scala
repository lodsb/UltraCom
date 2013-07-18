package ui.events

import ui.paths._

object TimeConnectionDeletionEvent {

  def apply(connection: TimeConnection) = {
    new TimeConnectionDeletionEvent(connection)
  }
}

class TimeConnectionDeletionEvent(val connection: TimeConnection) extends UiEvent("TIME_CONNECTION_DELETED") {
}
