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
import org.mt4j.components.visibleComponents.AbstractVisibleComponent
import org.lodsb.reakt.TVal


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

  /*
  protected def registerTVal[T](tval: TVal[T]) = {
    propertiesAndAttributes  = propertiesAndAttributes :+ TValWrapper(tval)
  }

  protected def registerTVar[T](tval: TVal[T]) = {
    propertiesAndAttributes  = propertiesAndAttributes :+ TVarWrapper(tval)
  } */


  def use(styles: StyleSpecification) = {
    // fixme, n goddamn type erasure, how to fix this code?
    styles.foreach{style =>
      propertiesAndAttributes.foreach { pa =>
        if(style.n == pa.name) {
          // since this cant be made typesafe for now
          style match {
            case x:StylePropertyT[_] => {
              pa match {
                case y: AttributeWrapper[_] => {
                  y.attribute.asInstanceOf[Attribute[Any]]() = x.value.asInstanceOf[Any]
                }

                case y: PropertyWrapper[_] => {
                  y.property.asInstanceOf[Property[Any]]() = x.value.asInstanceOf[Any]
                }

              }}
          }

          /*
          style match {
            case x:StylePropertyT[_] => {
              println(pa.m.erasure+ " | "+ x.value.getClass+ " " + pa.m.equals(x.value.getClass) + " | " + pa.m.erasure.equals(x.value.getClass))
            }
          }
          (pa: @unchecked) match {
            case x:AttributeWrapper[Vector3D] => {
              (style: @unchecked) match {
                case y:StylePropertyT[Vector3D] => {
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[Vector3D] => {
              (style: @unchecked) match {
                case y:StylePropertyT[Vector3D] => {
                  x.property() = y.value
                }
              }
            }
            case x:AttributeWrapper[Rotation] => {
              (style: @unchecked) match {
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
                  println(x.attribute.manifest.erasure == y.value.getClass)
                  x.attribute() = y.value
                }
              }
            }
            case x:PropertyWrapper[MTColor] => {
              style match {
                case y:StylePropertyT[MTColor] => {
                  println(x.property.manifest.erasure == y.value.getClass)
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
        */}
      }
    }
  }

  private var colorStack: List[MTColor] = List[org.mt4j.util.MTColor]()

  protected def propagateColorTransformationToChildren(ctrans: ColorTransformation)

  def colorTransformation(ctrans: ColorTransformation) = {
    // this is dirty, but there should be also some typesafe and direct way to do this

    propertiesAndAttributes.foreach { pa =>
      // ARRRGGHHHGDFFDSFGDSFGSDFGSDFG
      if(pa.name.toLowerCase.contains("color")) {
      (pa: @unchecked)  match {
        case x:PropertyWrapper[MTColor] => {
          val currentColor = x.property.get().asInstanceOf[MTColor]
          ctrans match {
            case ColorPushState => {
              colorStack = colorStack :+ (currentColor)
            }

            case ColorPopState => {
              if(!colorStack.isEmpty) {
                val oldColor = colorStack.head
                colorStack = colorStack.tail

                x.property() = oldColor
              }
            }

            case ColorSaturation(v) => {
              val w = scala.math.min(scala.math.max(v, 0), 255)
              val newColor = Color.fromMtColor(currentColor).hsv.saturation(w).rgb
              x.property() = newColor
            }

            case ColorLightness(v) => {
              val w = scala.math.min(scala.math.max(v, 0), 255)
              val newColor = Color.fromMtColor(currentColor).hsv.lightness(w).rgb
              x.property() = newColor
            }

            case ColorRotation(v) => {
              val newColor = Color.fromMtColor(currentColor).hsv.rotation(v).rgb
              x.property() = newColor
            }

            case ColorOpacity(v) => {
              val newColor = Color.fromMtColor(currentColor).opacity(v)
              x.property.set(newColor)
            }

            case ColorColorize(c) => {
              val newColor = Color.fromMtColor(currentColor) + c.rgb
              x.property() = newColor
            }

            case ColorInterpolateTo(v, c2) => {
              // slightly bogus?
              val newColor = Color.fromMtColor(currentColor).interpolate(v, c2.rgb)
              x.property() = newColor
            }

            case ColorInterpolation(v, c1, c2) => {
              val newColor = c1.rgb.interpolate(v, c2.rgb)
              x.property() = newColor
            }

            case ColorSaturate(v) => {
              val newColor = Color.fromMtColor(currentColor).hsv.saturate(v).rgb
              x.property() = newColor
            }

            case ColorDesaturate(v) => {
              val newColor = Color.fromMtColor(currentColor).hsv.desaturate(v).rgb
              x.property() = newColor
            }

            case ColorLighten(v) => {
              val newColor = Color.fromMtColor(currentColor).hsv.lighten(v).rgb
              x.property() = newColor
            }

            case ColorMoreOpaque(v) => {
              val newColor = Color.fromMtColor(currentColor).rgb.moreOpaque(v).rgb
              x.property() = newColor
            }

            case ColorMoreTransparent(v) => {
              val newColor = Color.fromMtColor(currentColor).rgb.moreTransparent(v).rgb
              x.property() = newColor
            }

            case ColorDarken(v) => {
              val newColor = Color.fromMtColor(currentColor).hsv.lighten(v).rgb
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
  import reflect.runtime.universe._

  abstract class PropertyAndAttributeWrapper(val name: String, val m: Manifest[_])
  case class PropertyWrapper[T](property: Property[T]) extends PropertyAndAttributeWrapper(property.name, property.manifest)
  case class AttributeWrapper[T](attribute: Attribute[T]) extends PropertyAndAttributeWrapper(attribute.name, attribute.manifest)
}