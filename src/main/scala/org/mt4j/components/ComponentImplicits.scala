/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 10 - 16 :: 8 : 19
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2012:
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

package org.mt4j.components

object ComponentImplicits {
	implicit def MT2MTE(comp:MTComponent) = new MTComponentExtension(comp);
	implicit def MT2MTArr1(comp: MTComponent):Seq[MTComponent] = Array(comp)
	implicit def MT2MTCA(comp: Array[MTComponent]) = new MTComponentArrayHelper(comp)
}

class MTComponentExtension(comp: MTComponent) {
	def ++(that:MTComponent): Array[MTComponent] = Array(comp, that)
	def ++(that:Array[MTComponent]): Array[MTComponent] = Array.concat(Array(comp), that)

	/// fun with sequences
	def +=(that: Seq[MTComponent]) = {
		that.foreach( c => comp.addChild(c) )
	}

	def -=(that: Seq[MTComponent]) = {
		that.foreach( c => comp.removeChild(c) )
	}
}

class MTComponentArrayHelper(comp: Array[MTComponent]) {
	def ++(that: MTComponent) = Array.concat(comp, Array(that));
}

