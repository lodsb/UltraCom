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

trait Color
class RgbC(r: Int, g: Int, b: Int, a: Int) extends MTColor(r,g,b,a)

case class Alpha(alpha: Int) extends RgbC(0,0,0,alpha)
case class Red(red: Int) extends RgbC(red,0,0,0)
case class Green(green: Int) extends RgbC(0,green,0,0)
case class Blue(blue: Int) extends RgbC(0,0, blue,0)

case class Rgb(r: Int, g: Int, b: Int, a: Int) extends RgbC(r,g,b,a) with Color {
  val range = 0 to 255
  r :: g :: b :: Nil map(d => require((0 to 255).contains(d), "%s must be between 0..255" format d))

  def hex = Hex(r :: g :: b :: Nil map(Integer.toHexString(_)) map(s => if(s.size==1) s * 2 else s) mkString(""))

  def cmyk = {
    if((r :: g :: b :: Nil).forall(_ == 0)) CMYK(0, 0, 0, 1)
    else {
      (r :: g :: b :: Nil).map(v => 1 - (v.toDouble / 255)) match {
        case c :: m :: y :: Nil =>
          val black = Math.min(c, Math.min(m, y))
          (c  :: m :: y :: Nil).map(v => v - black / (1 - black)) match {
            case c1 :: m1 :: y1 :: Nil => CMYK(c1, m1, y1, black)
          }
      }
    }
  }

  def +(rgb: Rgb) = {
    Rgb(rgb.r+this.r, rgb.b+ this.b, rgb.g+ this.g, rgb.a + this.a)
  }

  def -(rgb: Rgb) = {
    Rgb(rgb.r-this.r, rgb.b- this.b, rgb.g- this.g, rgb.a - this.a)
  }


  def hsv = {
    (r :: g :: b :: Nil).map(_.toDouble / 255) match {
      case r2 :: g2 :: b2 :: Nil =>
        val (min, max) = (Math.min(r2, Math.min(g2, b2)), Math.max(r2, Math.max(g2, b2)))
        val delta = max - min
        if(min == max) {
          Hsv(0, 0, min,a/255.0) // monochrome
        } else {
          Hsv((max match {
            case n if(n == r2) => (g2 - b2) / delta + (if(g2 < b2) 6 else 0)
            case n if(n == g2) => (b2 - r2) / delta + 2
            case n => (r2 - g2) / delta + 4
          }) * 60, if(max == 0)  0 else delta / max , max, a/255.0)
        }
    }
  }
  // based on : http://www.w3.org/TR/AERT#color-contrast
  // the higher the more contrast
  def colorContrast(that: Rgb) = {
    def cc(vv: Rgb) : Double = {
      ((vv.r/255.0)*299.0 + (vv.g/255.0)*587.0 + (vv.b/255.0)*114)/100.0
    }

    scala.math.abs(cc(this) - cc(that))
  }

}

case class Hsv(h: Double, s: Double, v: Double, a: Double) extends Color  {
  def rgb = {
    val c = s*v

    val h1 = h / 60.0
    val x = c*(1.0-((h1 % 2)-1.0).abs)

    val rgbList = if (h1 < 1.0) List(c, x, 0.0)
    else if (h1 < 2.0) List(x, c, 0.0)
    else if (h1 < 3.0) List(0.0, c, x)
    else if (h1 < 4.0) List(0.0, x, c)
    else if (h1 < 5.0) List(x, 0.0, c)
    else List(c, 0.0, x)

    val m = v-c
    val rgb = rgbList.map( x=> ((x + m) * 255.0).toInt)
    Rgb(rgb(0), rgb(1), rgb(2), (a*255.0).toInt)
  }

  def hex = {
    rgb.hex
  }

  def cmyk = {
    rgb.cmyk
  }

  def lighten(step: Double = 0.1) = {
    Hsv(h, s, v-step, a)
  }

  def darken(step: Double = 0.1) = {
    lighten(-step)
  }

  def saturate(step: Double = 0.1) = {
    Hsv(h, s+step, v, a)
  }

  def desaturate(step: Double = 0.1) = {
    saturate(-step)
  }

  def rotate(angle: Double) = {
    Hsv((h+angle) % 360, s, v, a)
  }



}

case class Hex(hex: String) extends Color {
  val range = ((0 to 11) ++ ('a' to 'f')).map(_.toString.charAt(0))

  require((3 :: 6 :: Nil).contains(hex.size), "invalid length")

  hex.toList.foreach(l => require(range.contains(l.toLower), "%s must be one of %s" format(l, range)))

  import Implicits._
  def rgb = hex.size match {
    case 3 => hex.partionsOf(1).map(_*2).map(Integer.parseInt(_, 16)) match {
      case List(r, g, b,a) => Rgb(r, g, b,a)
    }
    case 6 => hex.partionsOf(2).map(Integer.parseInt(_, 16)) match {
      case List(r, g, b,a) => Rgb(r, g, b,a)
    }
  }

  def cmyk = rgb.cmyk

  def hsv = rgb.hsv

}

case class CMYK(c: Double, m: Double, y: Double, k: Double) extends Color

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
	def apply(r:Int,g:Int,b:Int,a:Int) = Rgb(r,g,b,a)
	//def apply(name:String, r:Int,g:Int,b:Int) = Rgb(r,g,b, 255)
	//def apply(name:String, r:Int,g:Int,b:Int,a:Int) = Rgb(r,g,b,a)
	//def apply(name:String, r:Int,g:Int,b:Int) = Rgb(r,g,b, 255)
	//def apply(name:String, r:Int,g:Int,b:Int,a:Int) = Rgb(r,g,b,a)

}
