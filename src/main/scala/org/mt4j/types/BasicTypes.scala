/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 25 :: 4 : 12
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas KlÃŒgel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.mt4j.types

import org.mt4j.util.math.Vector3D

abstract class AbstractRotation {
	def rotationPos: Vector3D
	def degreeX: Float
	def degreeY: Float
	def degreeZ: Float
}

case class Rotation(rotationPos: Vector3D = Vector3D.ZERO_VECTOR,
		degreeX: Float = 0, 
		degreeY: Float = 0, 
		degreeZ: Float = 0) extends AbstractRotation

//case object ZeroRotation(Vector3D.ZERO_VECTOR,0,0,0) extends AbstractRotation

object Vec3d {

	val ZERO_VECTOR = new Vector3D(0, 0, 0);
 	val X_AXIS = new Vector3D(1, 0, 0);
	val Y_AXIS = new Vector3D(0, 1, 0);
	val Z_AXIS = new Vector3D(0, 0, 1);

	def apply() = new Vector3D()
 	def apply(x: Float, y:Float) = new Vector3D(x,y)
	def apply(x: Float, y:Float,z:Float) = new Vector3D(x,y,z)
	def apply(x: Float, y:Float,z:Float, w:Float) = new Vector3D(x,y,z,w)
	def copy(v:Vector3D) = new Vector3D(v)

}

