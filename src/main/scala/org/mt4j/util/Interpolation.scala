import org.mt4j.types.Vec3d
import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D

/*
  +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013-12-11 :: 20:06
    >>  Origin: org.mt4j.util
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


object Interpolation {
  def apply(v: Float, src:Color, dst: Color) = {
    src.rgb.interpolate(v, dst.rgb)
  }

  def apply(v: Float, src: Vector3D, dst: Vector3D) = {
    src.getScaled(1-v).getAdded(dst.getScaled(v))
  }
}
