package ui.persistence

trait Persitability {

  /**
  * Returns a valid XML string representing the object which inherits this trait.
  */
  def toXML: String

}
