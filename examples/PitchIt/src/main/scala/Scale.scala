package PitchIt

import collection.{mutable, immutable, IndexedSeqLike}
import reflect.ClassTag
import scala.collection.immutable.{BitSet}
import scala.collection.mutable.{ArrayBuffer,ListBuffer, Builder}
import Conversions._

import scala.collection.generic._
import scala.collection.immutable.VectorBuilder

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


// transpose -> no octave: shift, octave: rotate
// num steps

// add undefined scale object?
// AD scale?

// scale == idx to tuning
// pitch & chord == idx to scale

//TODO: fix seq-like

class Scale protected (val buffer: BitSet, // for current debugging
                       private val cyclicOctaveSteps: Option[Int],
                       private val root: Chromatic) extends Ordered[Scale] {

  //extends IndexedSeq[Boolean]
  /*with IndexedSeqLike[Boolean, Scale] */

  /*
  override protected[this] def newBuilder: Builder[Boolean, Scale] =
    Scale.newBuilder
*/

  private val valueOfBitSet = BitSetOps.bitSet2Int(this.buffer, ifCyclicElse({cycle=>cycle-1})({buffer.toList.max}))
  def id = valueOfBitSet
  def name = ScaleLib.name(this.valueOfBitSet.toInt)


  private def ifCyclicElse[A](ifBlock: Int => A)(elseBlock: => A): A = {
    if(cyclicOctaveSteps.isDefined) {
      ifBlock(cyclicOctaveSteps.get)
    } else {
      elseBlock
    }
  }

  def classOf: Scale = {
    // rotate back = 0-class
    val newBitset = ifCyclicElse({cycle =>
      BitSetOps.rotate(this.buffer, -this.root, cycle)
    })({
      BitSetOps.shift(this.buffer, -this.root)
    })

    Scale(newBitset, cyclicOctaveSteps, 0)
  }

  // interval spectrum
  def intervalSpectrum : List[Int] = {
    ifCyclicElse(cycle => BitSetOps.cyclicAutoCorrelation(this.buffer, cycle ))(Nil)
  }

  def invert : Scale = {

    val length = ifCyclicElse({cycle => cycle})( {this.buffer.size})

    Scale(BitSetOps.invert(this.buffer, length),this.cyclicOctaveSteps, root)
  }

  def mirror(chroma: Int = 0) : Option[Scale] = {
    var ret: Option[Scale] = None

    ifCyclicElse({ cycle =>
      val scaleClass  = BitSetOps.rotate(this.buffer, -this.root+chroma, cycle)
      val mirrored    = BitSetOps.mirror(scaleClass, cycle)
      val rotatedBack = BitSetOps.rotate(mirrored, this.root, cycle)
      ret = Some(Scale(rotatedBack, cyclicOctaveSteps, root))
    })({})

    ret
  }

  def isInScale(sp: ScaledPitch): Boolean = {
    // positive and negative indices...
    val note = sp.note
    var ret = false

    val index  = ifCyclicElse(
    { cycle: Int =>
      var idx = note % cycle

      if(idx < 0) {
        idx = cycle - idx
      }
      idx
    })(note)

    try {
      ret = buffer(index)
    } catch {
      case _: Throwable =>
    }

    ret
  }

  // apply for Movement, so we can have AD Scales

  /**
   * applies a pitch to the scale, returning the absolute index on a tuning
   * @param p
   * @return
   */
  def apply(p: Pitch) : ScaledAndPitched = {
    var ret : ScaledAndPitched = UndefinedScaledPitch

    val ithNoteAndOctave = this(p.number)

    if(ithNoteAndOctave.isDefined) {
      val ithOctave = ithNoteAndOctave.get._2
      val ithNote = ithNoteAndOctave.get._1

      var note = ithNote + this.root

      if(cyclicOctaveSteps.isDefined) {
        note = note + ( (p.octave + ithOctave)* cyclicOctaveSteps.get)
      }

      ret = ScaledPitch(note)
    }

    ret
  }

  /*
  // arrrfggggfgfg -- type erasure!
  def apply(chord: Chord[Pitch]) : Chord[ScaledAndPitched] = Chord.fromSeq((chord.map(x => this.apply(x))))

  def apply[X: ClassTag](contour: Contour[Pitch]): Contour[ScaledAndPitched]= contour.map(x => this.apply(x))

  def apply[X: ClassTag, Y: ClassTag](contour: Contour[Chord[Pitch]]) : Contour[Chord[ScaledAndPitched]] = {
    contour.map(chord => chord.map{p => this.apply(p)})
  }    */

  //inverse
  def apply(sp: ScaledPitch) : PitchBase = {
    var ret: PitchBase = UndefinedPitch

    val noteList = buffer.toList
    val note = ifCyclicElse(cycle => sp.note % cycle)(sp.note)
    val octave=ifCyclicElse(cycle => sp.note / cycle)(0)


    val index = noteList.indexOf(note)

    if( index > -1) {
      ret = Pitch(note, octave)
    }

    ret
  }

  /**
   * Returns the ith note in the scale and possible carry from octave shift
   * @param i
   * @return
   */
  def apply(i: Chromatic) : Option[(Int,Int)] = {
    var idx = i
    var ret : Option[(Int, Int)] = None
    var octaveShift = 0

    idx = i % buffer.size
    octaveShift = i / buffer.size

    val noteList = buffer.toList

    ret = Some(noteList(idx), octaveShift)

    ret
  }

  def degrees : List[Int] = {
    ifCyclicElse({cycle =>
      val octavedBuffer = this.buffer + cycle
      BitSetOps.bitDistances(octavedBuffer)
    })({
      BitSetOps.bitDistances(this.buffer)
    })
  }

  // this doesnt make sense! a pitch is _ALWAYS_ snapped to scale
  // should be function for ScaledPitch -> snap to this pitch scale...
  // but it is better to invert scaledpitch and then apply it to another scale
  /*
  def snapToScale(p: Pitch) : PitchBase = {
    val nextPitchNumber = BitSetOps.findNearestSetBit(this.buffer, p.number)

    if (nextPitchNumber.isDefined) {
      Pitch(nextPitchNumber.get, p.octave)
    } else {
      UndefinedPitch
    }
  }
  */

  def length : Int = {
    var ret = buffer.size

    if (cyclicOctaveSteps.isDefined) {
      ret = cyclicOctaveSteps.get
    }

    ret
  }

  // TODO fixme...
  /*
  def apply(c: Contour) : ConcreteContour = {
    null
  }
  */

  def transpose(offset: Chromatic) : Scale = {

    var transposedBitSet : BitSet = BitSet.empty
    var currentRoot = this.root

    // if cyclic, transposition results in a rotation, otherwise it is a shift with the same root
    if(cyclicOctaveSteps.isDefined) {
      transposedBitSet = BitSetOps.rotate(this.buffer, offset, this.length)
      currentRoot = currentRoot + offset
    } else {
      transposedBitSet = BitSetOps.shift(this.buffer, offset)
    }

    Scale(transposedBitSet, this.cyclicOctaveSteps, currentRoot )
  }

  def compare(that: Scale): Int = this.valueOfBitSet.compare(that.valueOfBitSet)
}

//TODO: apply OPS for A/D scale
class AscendingDescendingScale(val ascending: Scale, val descending: Scale) {
  def apply(movement: Movement[Pitch]) : ScaledAndPitched = {
    movement match {
      case UpMovement(_, to) => ascending(to)
      case DownMovement(_, to) => descending(to)
      case StraightMovement(_,to) => ascending(to)
    }
  }

  def transpose(offset: Chromatic) : AscendingDescendingScale = {
    val a = ascending.transpose(offset)
    val d = descending.transpose(offset)

    AscendingDescendingScale(a,d)
  }
}

object AscendingDescendingScale {
  def apply(ascending: Scale, descending: Scale) = new AscendingDescendingScale(ascending, descending)
}

object Scale {
  def apply(code: Int, cyclicOctaveSteps: Option[Int] = Some(12), root: Chromatic = 0) : Scale = {
    new Scale(BitSetOps.int2BitSet(code), cyclicOctaveSteps, root)
  }

  def apply(bitSet: scala.collection.immutable.BitSet, cyclicOctaveSteps: Option[Int], root: Chromatic) : Scale = {
    new Scale(bitSet, cyclicOctaveSteps, root)
  }

  def apply(s: String): Option[Scale] = {
    var ret : Option[Scale] = None

    val id = ScaleLib.id(s)

    if (id.isDefined) {
      ret = Some(this.apply(id.get))
    }

    ret
  }

  def apply(ascending: String, descending: String) : Option[AscendingDescendingScale] = {
    var ret : Option[AscendingDescendingScale] = None

    val a = Scale(ascending)
    val d= Scale(descending)

    if (a.isDefined && d.isDefined) {
      ret = Some(AscendingDescendingScale(a.get, d.get))
    }

    ret
  }

  def apply(ascending: Int, descending: Int) : AscendingDescendingScale = {

    val a = Scale(ascending)
    val d = Scale(descending)


    AscendingDescendingScale(a, d)
  }

}
