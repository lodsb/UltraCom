package org.mt4j.components.visibleComponents.style

/**
 * Created with IntelliJ IDEA.
 * User: lodsb
 * Date: 12/9/13
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */



object Style {
  type StyleSpecification = Seq[StyleProperty]

  abstract class StyleProperty(val n: String)
  case class StylePropertyT[T](name: String, val value: T) extends StyleProperty(name)

  class StyleString(val name: String) {
    def :=[T](value: T) : StylePropertyT[T] = {
      StylePropertyT(name, value)
    }
  }

  implicit def string2StyleString(s: String) = new StyleString(s)

  def apply(styles: StyleProperty*) : StyleSpecification = {
    styles
  }
}
