package PitchIt

/*
  +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013-11-06 :: 00:22
    >>  Origin:
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

class Tuning(private val tuningFunc: ScaledPitch => TunedPitch) {
  def apply(sp: ScaledAndPitched) : TunedPitch = {
    sp match {
        case x:ScaledPitch => tuningFunc(x)
        case _ => UndefinedTunedPitch
    }
  }
}

object Tuning {
  def apply(s: String) = TuningLib(s)
}







