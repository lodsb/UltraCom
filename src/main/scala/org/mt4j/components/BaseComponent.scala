package org.mt4j.components

import org.lodsb.reakt.property.{Attribute, Property, VarDeferor}
import org.mt4j.components.PropertyAndAttributeWrappers.PropertyAndAttributeWrapper
import org.mt4j.util.math.{Vertex, Vector3D}
import org.mt4j.types.Rotation
import org.mt4j.util.MTColor
import org.mt4j.components.visibleComponents.font.IFont
import processing.core.PImage


/**
 * Created with IntelliJ IDEA.
 * User: lodsb
 * Date: 12/9/13
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class BaseComponent extends VarDeferor {
  protected var propertiesAndAttributes = List[PropertyAndAttributeWrapper]()

  import org.mt4j.components.PropertyAndAttributeWrappers._
  import org.mt4j.components.visibleComponents.style.Style._

  protected def registerAttribute[T](attribute: Attribute[T]) = {
    propertiesAndAttributes  = propertiesAndAttributes :+ AttributeWrapper(attribute)
  }
  protected def registerProperty[T](property: Property[T]) = {
    propertiesAndAttributes  = propertiesAndAttributes :+ PropertyWrapper(property)
  }

  def use(styles: StyleSpecification) = {
    styles.foreach{style =>
      propertiesAndAttributes.foreach { pa =>
        if(style.n == pa.name) {
          pa match {
            case x:AttributeWrapper[Vector3D] => {
              style match {
                case y:StylePropertyT[Vector3D] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Vector3D] => {
              style match {
                case y:StylePropertyT[Vector3D] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[Rotation] => {
              style match {
                case y:StylePropertyT[Rotation] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Rotation] => {
              style match {
                case y:StylePropertyT[Rotation] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[Double] => {
              style match {
                case y:StylePropertyT[Double] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Double] => {
              style match {
                case y:StylePropertyT[Double] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[Int] => {
              style match {
                case y:StylePropertyT[Int] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Int] => {
              style match {
                case y:StylePropertyT[Int] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[Float] => {
              style match {
                case y:StylePropertyT[Float] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Float] => {
              style match {
                case y:StylePropertyT[Float] => {
                  x.property() = y.value
                }
              }
            }/*
            case x:AttributeWrapper[java.lang.Float] => {
              style match {
                case y:StylePropertyT[java.lang.Float] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[java.lang.Float] => {
              style match {
                case y:StylePropertyT[java.lang.Float] => {
                  x.property() = y.value
                }
              }
            }     */
            case x:AttributeWrapper[String] => {
              style match {
                case y:StylePropertyT[String] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[String] => {
              style match {
                case y:StylePropertyT[String] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[MTColor] => {
              style match {
                case y:StylePropertyT[MTColor] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[MTColor] => {
              style match {
                case y:StylePropertyT[MTColor] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[Long] => {
              style match {
                case y:StylePropertyT[Long] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Long] => {
              style match {
                case y:StylePropertyT[Long] => {
                  x.property() = y.value
                }
              }
            }

            case x:AttributeWrapper[Vertex] => {
              style match {
                case y:StylePropertyT[Vertex] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Vertex] => {
              style match {
                case y:StylePropertyT[Vertex] => {
                  x.property() = y.value
                }
              }
            }

            case x:AttributeWrapper[PImage] => {
              style match {
                case y:StylePropertyT[PImage] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[PImage] => {
              style match {
                case y:StylePropertyT[PImage] => {
                  x.property() = y.value
                }
              }
            }

            case x:AttributeWrapper[IFont] => {
              style match {
                case y:StylePropertyT[IFont] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[IFont] => {
              style match {
                case y:StylePropertyT[IFont] => {
                  x.property() = y.value
                }
              }
            }

          }
        }
      }
    }
  }

}

object PropertyAndAttributeWrappers {
  abstract class PropertyAndAttributeWrapper(val name: String)
  case class PropertyWrapper[T](property: Property[T]) extends PropertyAndAttributeWrapper(property.name)
  case class AttributeWrapper[T](attribute: Attribute[T]) extends PropertyAndAttributeWrapper(attribute.name)

}