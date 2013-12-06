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


import scala.collection._
import scala.collection.mutable.Builder
import scala.collection.generic._
import scala.collection.immutable.VectorBuilder


// Taken from
// http://www.crosson.org/2013/01/simple-custom-scala-collection-examples.html
/*
object PitchedVector {

  def apply[P <: Pitched](Ps: P*) = fromSeq(Ps.toVector)

  def fromSeq[P <: Pitched](buf: Vector[P]): PitchedVector[P] =
    new PitchedVector[P](buf)

  def newBuilder[P <: Pitched]: Builder[P, PitchedVector[P]] =
    new VectorBuilder mapResult fromSeq

  implicit def canBuildFrom[P <: Pitched]:
  CanBuildFrom[PitchedVector[_], P, PitchedVector[P]] =
    new CanBuildFrom[PitchedVector[_], P, PitchedVector[P]] {
      def apply(): Builder[P, PitchedVector[P]] = newBuilder
      def apply(from: PitchedVector[_]): Builder[P, PitchedVector[P]] =
        newBuilder
    }
}


abstract class PitchedVector[P <: Pitched] protected (buf: Vector[P])
  extends IndexedSeq[P]
  with IndexedSeqLike[P, PitchedVector[P]] {

  val buffer = processVector(buf)

  protected def processVector(buf: Vector[P]): Vector[P] = buf

  override protected[this] def newBuilder: Builder[P, PitchedVector[P]] //= PitchedVector.newBuilder

  def apply(idx: Int): P = {
    if (idx < 0 || length <= idx) throw new IndexOutOfBoundsException
    buffer(idx)
  }

  def length = buffer.length
}
*/