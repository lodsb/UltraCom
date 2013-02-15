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
 */

package org.mt4j.util

object Color {

	val RED = new MTColor(255, 0, 0, 255);
 	val GREEN = new MTColor(0, 128, 0, 255);
 	val BLUE = new MTColor(0, 0, 255, 255);
 	val BLACK = new MTColor(0, 0, 0, 255);
 	val WHITE = new MTColor(255, 255, 255, 255);
 	val GREY = new MTColor(128, 128, 128, 255);
 	val GRAY = new MTColor(128, 128, 128, 255);
 	val SILVER = new MTColor(192, 192, 192, 255);
 	val MAROON = new MTColor(128, 0, 0, 255);
 	val PURPLE = new MTColor(128, 0, 128, 255);
 	val FUCHSIA = new MTColor(255, 0, 255, 255);
 	val LIME = new MTColor(0, 255, 0, 255);
 	val OLIVE = new MTColor(128, 128, 0, 255);
 	val YELLOW = new MTColor(255, 255, 0, 255);
 	val NAVY = new MTColor(0, 0, 128, 255);
	val TEAL = new MTColor(0, 128, 128, 255);
 	val AQUA = new MTColor(0, 255, 255, 255);

	def apply(r:Float,g:Float,b:Float) = new MTColor(r,g,b)
	def apply(r:Float,g:Float,b:Float,a:Float) = new MTColor(r,g,b,a)
	def apply(name:String, r:Float,g:Float,b:Float) = new MTColor(name, r,g,b)
	def apply(name:String, r:Float,g:Float,b:Float,a:Float) = new MTColor(name, r,g,b,a)
	def apply(name:String, r:Int,g:Int,b:Int) = new MTColor(name, r,g,b)
	def apply(name:String, r:Int,g:Int,b:Int,a:Int) = new MTColor(name, r,g,b,a)

}
