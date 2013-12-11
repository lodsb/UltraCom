/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 2 - 15 :: 1 : 16
    >>  Origin: mt4j (project) / UltraCom (module)
    >>
  +3>>
    >>  Copyright (c) 2013:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.

    Part of this Code is taken from:
    https://github.com/softprops/chroma/blob/master/src/main/scala/chroma.scala
    => (c) doug tangren

    and
    http://henkelmann.eu/2010/11/03/hsv_to_rgb_in_scala
    => Christoph Henkelmann
 */

package org.mt4j.util

//TODO: Palette

object Palette {
  // https://code.google.com/p/colorlib/source/browse/trunk/colorLib/src/colorLib/Palette.java
}

private object Implicits {
  /** break a string into partions of n size */
  implicit def s2p2(str: String) = new {
    def partionsOf(n: Int) = {
      def par[T](l: List[T])(mkt: List[T] => T): List[T] = l match {
        case l @ Nil => l
        case xs if(xs.size == n) => mkt(xs) :: Nil
        case xs => mkt(xs.take(n)) :: par(xs.drop(n))(mkt)
      }
      par(str.toList.map(_.toString))(_.mkString(""))
    }
  }
}

trait Color { // maybe statemonad with reset? for transformations to be resetable?
  def hex : Hex
  def cmyk : Cmyk
  def hsv : Hsv
  def rgb : Rgb
}
class RgbC(val r: Int, val g: Int, val b: Int, val a: Int) extends MTColor(r,g,b,a)

case class Alpha(alpha: Int) extends RgbC(0,0,0,alpha)
case class Red(red: Int) extends RgbC(red,0,0,0)
case class Green(green: Int) extends RgbC(0,green,0,0)
case class Blue(blue: Int) extends RgbC(0,0, blue,0)

case class Rgb(override val r: Int, override val g: Int, override val b: Int, override val a: Int) extends RgbC(r,g,b,a)
with Color {

  val range = 0 to 255
  //r :: g :: b :: Nil map(d => require((0 to 255).contains(d), "%s must be between 0..255" format d))
  override def rgb = this

  override def hex = Hex(r :: g :: b :: Nil map(Integer.toHexString(_)) map(s => if(s.size==1) s * 2 else s) mkString(""))

  override def cmyk = {
    if((r :: g :: b :: Nil).forall(_ == 0)) Cmyk(0, 0, 0, 1, a/255)
    else {
      (r :: g :: b :: Nil).map(v => 1 - (v.toFloat / 255)) match {
        case c :: m :: y :: Nil =>
          val black = Math.min(c, Math.min(m, y))
          (c  :: m :: y :: Nil).map(v => v - black / (1 - black)) match {
            case c1 :: m1 :: y1 :: Nil => Cmyk(c1, m1, y1, black, a/255)
          }
      }
    }
  }

  private def cl(v: Int) = {
    scala.math.min(scala.math.max(0, v),255)
  }

  private def cl(v: Float) : Int = {
    scala.math.min(scala.math.max(0, v),255).toInt
  }

  def +(rgb: Rgb) = {
    Rgb(cl(rgb.r+this.r), cl(rgb.b+ this.b), cl(rgb.g+ this.g), cl(rgb.a + this.a))
  }

  def -(rgb: Rgb) = {
    Rgb(cl(rgb.r-this.r), cl(rgb.b- this.b), cl(rgb.g- this.g), cl(rgb.a - this.a))
  }

  def +(rgb: RgbC) = {
    Rgb(cl(rgb.r+this.r), cl(rgb.b+ this.b), cl(rgb.g+ this.g), cl(rgb.a + this.a))
  }

  def -(rgb: RgbC) = {
    Rgb(cl(rgb.r-this.r), cl(rgb.b- this.b), cl(rgb.g- this.g), cl(rgb.a - this.a))
  }

  def *(v: Float) = {
    Rgb(cl(v*this.r), cl(v*this.b), cl(v* this.g), cl(v* this.a))
  }

  def interpolate(step: Float=0.01f, that: Rgb) : Rgb = {
    (this*(1-step)) + (that*step)
  }



  override def hsv = {
    (r :: g :: b :: Nil).map(_.toFloat / 255) match {
      case r2 :: g2 :: b2 :: Nil =>
        val (min, max) = (Math.min(r2, Math.min(g2, b2)), Math.max(r2, Math.max(g2, b2)))
        val delta = max - min
        if(min == max) {
          Hsv(0, 0, min,a/255.0f) // monochrome
        } else {
          Hsv((max match {
            case n if(n == r2) => (g2 - b2) / delta + (if(g2 < b2) 6 else 0)
            case n if(n == g2) => (b2 - r2) / delta + 2
            case n => (r2 - g2) / delta + 4
          }) * 60, if(max == 0)  0 else delta / max , max, a/255.0f)
        }
    }
  }
  // based on : http://www.w3.org/TR/AERT#color-contrast
  // the higher the more contrast
  def colorContrast(that: Rgb) = {
    def cc(vv: Rgb) : Float = {
      (((vv.r/255.0)*299.0 + (vv.g/255.0)*587.0 + (vv.b/255.0)*114)/100.0).toFloat
    }

    scala.math.abs(cc(this) - cc(that))
  }

  def moreOpaque(step: Float) = {
    Rgb(r,g,b, cl((a+step).toInt))
  }

  def moreTransparent(step: Float) = {
    Rgb(r,g,b, cl((a - step).toInt))
  }

  def opacity(v: Float) = {
    Rgb(r,g,b, cl((v*255).toInt) )
  }


}

case class Hsv(h: Float, s: Float, v: Float, a: Float) extends Color  {
  def rgb = {
    val c = s*v

    val h1 = h / 60.0
    val x = c*(1.0f-((h1 % 2)-1.0f).abs).toFloat

    val rgbList = if (h1 < 1.0) List(c, x, 0.0f)
    else if (h1 < 2.0f) List(x, c, 0.0f)
    else if (h1 < 3.0f) List(0.0f, c, x)
    else if (h1 < 4.0f) List(0.0f, x, c)
    else if (h1 < 5.0f) List(x, 0.0f, c)
    else List(c, 0.0f, x)

    val m = (v-c)
    val rgb = rgbList.map( x=> ((x + m) * 255.0).toInt).map(x => scala.math.min(scala.math.max(x, 0),255))
    Rgb(rgb(0), rgb(1), rgb(2), (a*255.0).toInt)
  }

  override def hex = {
    rgb.hex
  }

  override def cmyk = {
    rgb.cmyk
  }

  override def hsv = this

  private def cl(v: Float) = {
    scala.math.min(scala.math.max(v,0), 1.0).toFloat
  }

  def darken(step: Float = 0.1f) = {
    Hsv(h, s, cl(v-step), a)
  }

  def lighten(step: Float = 0.1f) = {
    darken(-step)
  }

  def lightness(r: Float) = {
    Hsv(h, s, r, a)
  }

  def saturation(r: Float) = {
    Hsv(h, r, v, a)
  }

  def desaturate(step: Float = 0.1f) = {
    saturate(-step)
  }

  def saturate(step: Float = 0.1f) = {
    Hsv(h, cl(s+step), v, a)
  }

  def rotate(angle: Float=1f) = {
    Hsv((h+angle) % 360, s, v, a)
  }

  def rotation(angle: Float=1f) = {
    Hsv(angle % 360, s, v, a)
  }

}

case class Hex(hexs: String) extends Color {
  // FIXME , bogus, length??? recheck!
  val range = ((0 to 11) ++ ('a' to 'f')).map(_.toString.charAt(0))

  require((3 :: 6 :: Nil).contains(hexs.size), "invalid length")

  hexs.toList.foreach(l => require(range.contains(l.toLower), "%s must be one of %s" format(l, range)))

  import Implicits._
  def rgb = hexs.size match {
    case 3 => hexs.partionsOf(1).map(_*2).map(Integer.parseInt(_, 16)) match {
      case List(r, g, b,a) => Rgb(r, g, b,a)
    }
    case 6 => hexs.partionsOf(2).map(Integer.parseInt(_, 16)) match {
      case List(r, g, b,a) => Rgb(r, g, b,a)
    }
  }

  override def cmyk = rgb.cmyk

  override def hsv = rgb.hsv

  override def hex = this



}

//FIXME !!!
case class Cmyk(c: Float, m: Float, y: Float, k: Float, alpha: Float)// extends Color

object Color {

	type Color = MTColor

	val RED = Rgb(255, 0, 0, 255);
 	val GREEN = Rgb(0, 128, 0, 255);
 	val BLUE = Rgb(0, 0, 255, 255);
 	val BLACK = Rgb(0, 0, 0, 255);
 	val WHITE = Rgb(255, 255, 255, 255);
 	val GREY = Rgb(128, 128, 128, 255);
 	val GRAY = Rgb(128, 128, 128, 255);
 	val SILVER = Rgb(192, 192, 192, 255);
 	val MAROON = Rgb(128, 0, 0, 255);
 	val PURPLE = Rgb(128, 0, 128, 255);
 	val FUCHSIA = Rgb(255, 0, 255, 255);
 	val LIME = Rgb(0, 255, 0, 255);
 	val OLIVE = Rgb(128, 128, 0, 255);
 	val YELLOW = Rgb(255, 255, 0, 255);
 	val NAVY = Rgb(0, 0, 128, 255);
	val TEAL = Rgb(0, 128, 128, 255);
 	val AQUA = Rgb(0, 255, 255, 255);

	def apply(r:Int,g:Int,b:Int) = Rgb(r,g,b, 255)
  def apply(r:Int) = Rgb(r,r,r, 255)
  def apply(r:Int, a:Int) = Rgb(r,r,a, 255)
	def apply(r:Int,g:Int,b:Int,a:Int) = Rgb(r.toInt,g.toInt,b.toInt,a.toInt)
  def apply(r:Float,g:Float,b:Float) = Rgb(r.toInt,g.toInt,b.toInt, 255)
  def apply(r:Float) = Rgb(r.toInt,r.toInt,r.toInt, 255)
  def apply(r:Float, a:Float) = Rgb(r.toInt,r.toInt,a.toInt, 255)
  def apply(r:Float,g:Float,b:Float,a:Float) = Rgb(r.toInt,g.toInt,b.toInt,a.toInt)

  def fromMtColor(c: MTColor) : Rgb = {
    Rgb(c.getR.toInt, c.getG.toInt, c.getB.toInt, c.getA.toInt)
  }
	//def apply(name:String, r:Int,g:Int,b:Int) = Rgb(r,g,b, 255)
	//def apply(name:String, r:Int,g:Int,b:Int,a:Int) = Rgb(r,g,b,a)
	//def apply(name:String, r:Int,g:Int,b:Int) = Rgb(r,g,b, 255)
	//def apply(name:String, r:Int,g:Int,b:Int,a:Int) = Rgb(r,g,b,a)
}

abstract class ColorTransformation
case class ColorSaturation(value: Float) extends ColorTransformation
case class ColorOpacity(value: Float) extends ColorTransformation
case class ColorLightness(value: Float) extends ColorTransformation
case class ColorRotation(angle: Float) extends ColorTransformation
case class ColorColorize(color: Color) extends ColorTransformation
case class ColorInterpolateTo(step: Float=0.1f, destination: Color) extends ColorTransformation
case class ColorInterpolation(step: Float, source: Color, destination: Color) extends ColorTransformation
case class ColorDesaturate(step: Float=0.1f) extends ColorTransformation
case class ColorSaturate(step: Float=0.1f) extends ColorTransformation
case class ColorLighten(step: Float=0.1f) extends ColorTransformation
case class ColorDarken(step: Float=0.1f) extends ColorTransformation
case class ColorMoreOpaque(step: Float=0.1f) extends ColorTransformation
case class ColorMoreTransparent(step: Float=0.1f) extends ColorTransformation
case class ColorRotate(step: Float=0.01f) extends ColorTransformation
object ColorPushState extends ColorTransformation
object ColorPopState extends ColorTransformation

