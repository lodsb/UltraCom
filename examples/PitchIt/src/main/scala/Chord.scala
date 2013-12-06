package PitchIt

import collection.generic.CanBuildFrom
import collection.immutable.VectorBuilder
import collection.IndexedSeqLike
import collection.mutable.Builder
import Conversions._

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

object Chord {
  def apply[P <: Pitched[_]](Ps: P*) = this.fromSeq(Ps.toVector)
  //def apply[P <: PitchBase](seq: Seq[P]) = PitchedVector.fromSeq(this.makeDistinct(seq).toVector)

  protected[Chord] def makeDistinct[P <: Pitched[_]](seq: Vector[P]) : Vector[P] = {
    var ret = Vector[P]()

    seq.foreach {
      x =>
        if (!ret.contains(x)) {
            ret = ret :+ x
        }
    }

    ret
  }

  def fromSeq[P <: Pitched[_]](buf: IndexedSeq[P]): Chord[P] =
    new Chord[P](makeDistinct(buf.toVector))


  def newBuilder[P <: Pitched[_]]: Builder[P, Chord[P]] =
    new VectorBuilder mapResult fromSeq

  implicit def canBuildFrom[P <: Pitched[_],From]:
  CanBuildFrom[Chord[_], P, Chord[P]] =
    new CanBuildFrom[Chord[_], P, Chord[P]] {
      def apply(): Builder[P, Chord[P]] = Chord.newBuilder
      def apply(from: Chord[_]): Builder[P, Chord[P]] =
        Chord.newBuilder
    }
}

class Chord[P <: Pitched[_]] protected (buf: Vector[P])
extends IndexedSeq[P] with Pitched[Chord[P]]
with IndexedSeqLike[P, Chord[P]] {

  val buffer = processVector(buf)

  def apply(idx: Int): P = {
    if (idx < 0 || length <= idx) throw new IndexOutOfBoundsException
    buffer(idx)
  }

  def length = buffer.length
  protected def processVector(buf: Vector[P]): Vector[P] = Chord.makeDistinct(buf)

  override protected[this] def newBuilder: Builder[P, Chord[P]] = Chord.newBuilder
}


object Triad extends Function1[Pitch, Chord[Pitch]]{
  def apply(Ps: Pitch) : Chord[Pitch] = {
    Chord(Ps, Pitch(Ps.number+2, Ps.octave), Pitch(Ps.number+4, Ps.octave))
  }
}