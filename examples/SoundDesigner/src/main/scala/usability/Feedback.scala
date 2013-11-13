package ui.usability

import ui.events._


trait Feedback {

  /**
  * Gives feedback for the specified event.
  */
	def giveFeedback(event: FeedbackEvent)

}
