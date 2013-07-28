package ui.util

object Identifier {
	var nextIdentifier = 0
	
	def nextID: Int = {
		val returnValue = this.nextIdentifier
		this.nextIdentifier = (this.nextIdentifier + 1)%Int.MaxValue //an overflow will almost certainly never happen in practice, but to be save...	
		returnValue
	}
}

trait Identifier {

	private val identifier = Identifier.nextID

	def id = {
		this.identifier
	}

}
