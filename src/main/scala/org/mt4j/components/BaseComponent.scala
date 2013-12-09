package org.mt4j.components

import org.lodsb.reakt.property.{Attribute, Property, VarDeferor}
import org.mt4j.components.PropertyAndAttributeWrappers.PropertyAndAttributeWrapper
import org.mt4j.util.math.{Vertex, Vector3D}
import org.mt4j.components.visibleComponents.font.IFont
import processing.core.PImage
import org.mt4j.util._
import org.mt4j.types.Rotation
import org.mt4j.types.Rotation
import scala.unchecked


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

  private var colorStack: List[MTColor] = List[org.mt4j.util.MTColor]()

  protected def propagateColorTransformationToChildren(ctrans: ColorTransformation)

  def colorTransformation(ctrans: ColorTransformation) = {
    // this is dirty, but there should be also some typesafe and direct way to do this

    propertiesAndAttributes.foreach { pa =>
      if(pa.name.toLowerCase.contains("color")) {
        pa match {
        case x:PropertyWrapper[MTColor] => {
          val currentColor = x.property.get()
          ctrans match {
            case PushColorState => {
              colorStack = colorStack :+ (currentColor)
            }

            case PopColorState => {
              if(!colorStack.isEmpty) {
                val oldColor = colorStack.head
                colorStack = colorStack.tail

                x.property() = oldColor
              }
            }

            case Saturation(v) => {
              val w = scala.math.min(scala.math.max(v, 0), 255)
              val newColor = Color.fromMtColor(currentColor).hsv.saturate(w).rgb
              x.property() = newColor
            }

            case Lightness(v) => {
              val w = scala.math.min(scala.math.max(v, 0), 255)
              val newColor = Color.fromMtColor(currentColor).hsv.lighten(w).rgb
              x.property() = newColor
            }

            case Invert(v) => {

              val newColor = currentColor;//Color.fromMtColor(currentColor).hsv.rotate(v*180).rgb
              x.property() = newColor
            }

            case Opacity(v) => {
              println(x.property.name)
              println("c "+currentColor)
              println("comp "+this)
              val newColor = Color.fromMtColor(currentColor);//.moreOpaque(v*254)
              x.property.set(currentColor)
            }

            case Colorize(r,g,b) => {
              val newColor = Color.fromMtColor(currentColor) + Red(r) + Green(g) + Blue(b)
              x.property() = newColor
            }


          }
        }
        case _ =>
      }
    }
    }
    propagateColorTransformationToChildren(ctrans)
  }
}

object PropertyAndAttributeWrappers {
  abstract class PropertyAndAttributeWrapper(val name: String)
  case class PropertyWrapper[T](property: Property[T]) extends PropertyAndAttributeWrapper(property.name)
  case class AttributeWrapper[T](attribute: Attribute[T]) extends PropertyAndAttributeWrapper(attribute.name)

}