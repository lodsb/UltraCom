package ui.events

import ui.paths._

abstract class NodeEvent(string: String, val node: Node) extends UiEvent(string) {
}
