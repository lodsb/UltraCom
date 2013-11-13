package ui.events

import ui.paths._

object TimeConnectionAddEvent {

  def apply(connection: TimeConnection) = {
    new TimeConnectionAddEvent(connection)
  }
}

class TimeConnectionAddEvent(val connection: TimeConnection) extends UiEvent("TIME_CONNECTION_ADDED") {
}
