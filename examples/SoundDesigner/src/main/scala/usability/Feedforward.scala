package ui.usability

import ui.events._


trait Feedforward {

  /**
  * Gives feedforward for the specified event.
  */
	def giveFeedforward(event: FeedforwardEvent)

}
