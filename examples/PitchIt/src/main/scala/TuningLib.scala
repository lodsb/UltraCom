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

object TuningLib {
  import Conversions._

  private val tuningMap = Map[String, (ScaledPitch) => TunedPitch](
    ( "Equal" -> {sp: ScaledPitch =>
      val f = scala.math.pow(2, (sp.note - 69.0) / 12.0)*440 // midi , TODO: should be 60?
      ConcretePitch(f)
    })

  )

  def apply(s: String) : Option[Tuning] = {
    val tFunc = tuningMap.get(s)
    var ret : Option[Tuning] = None

    if (tFunc.isDefined) {
      ret = Some(new Tuning(tFunc.get))
    }

    ret
  }

}
