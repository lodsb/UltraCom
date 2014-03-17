package mutator

import org.mt4j.input.osc.OSCTransmitter
import de.sciss.osc.Message
import mutant5000._
import mutant5000.IntegerEncoding
import mutant5000.Gene
import scala.util.Random

/**
 * Created by lodsb on 3/2/14.
 */
class ControlGlue(oscTransmit: OSCTransmitter, nodeForm: NodeForm,
                   parameters: List[String], ranges: List[(Int,Int)], deviation: List[Int],
                   highlightSubdivision: Option[(Int,Int)]) {

  var xRot : Int = 0;
  var yRot : Int = 0;
  var zRot : Int = 0;
  var scale : Int = 0;

  var locked = false

  private val r = new Random()

  /*
  if(highlightSubdivision.isEmpty) {
    nodeForm.setHighlighted(0, true)
  }
  */

  parameters.zip(ranges).zipWithIndex.foreach({ x =>
    val index = x._2
    val parm = x._1._1
    val rangeLo = x._1._2._1
    val rangeHi = x._1._2._2


    index match {

      case 1 => {

        zRot = rangeLo + r.nextInt(rangeHi-rangeLo)

        nodeForm.zRotLocked = false

        oscTransmit.send <~ nodeForm.rotationZ.map({ vv =>
          val v = scala.math.abs(vv)

          println("Z rot"+v)
          // clip at angles > 360 and map to parameter range, convert to Integer
          val vMapped = Util.linlin(scala.math.max(0,scala.math.min(v, 360f)), 0, 360, rangeLo, rangeHi).toInt;

          if(!locked) {
            zRot = vMapped

            // create OSCMessage
            Message(parm, vMapped)
          } else {
            Message(parm, zRot)
          }
        })
      }

      case 2 => {

        yRot = rangeLo + r.nextInt(rangeHi-rangeLo)
        nodeForm.yRotLocked = false

        oscTransmit.send <~ nodeForm.rotationY.map({ vv =>
          val v = scala.math.abs(vv)

        // clip at angles > 360 and map to parameter range, convert to Integer
          val vMapped: Int = Util.linlin(scala.math.max(0,scala.math.min(v, 360f)), 0, 360, rangeLo, rangeHi).toInt;

          if(!locked) {
            yRot = vMapped

            // create OSCMessage
            Message(parm, vMapped)
          } else {
            Message(parm, yRot)
          }
        })
      }

      case 3 => {

        xRot = rangeLo + r.nextInt(rangeHi-rangeLo)
        nodeForm.xRotLocked = false

        oscTransmit.send <~ nodeForm.rotationX.map({ vv =>
          val v = scala.math.abs(vv)

        // clip at angles > 360 and map to parameter range, convert to Integer
          val vMapped: Int = Util.linlin(scala.math.max(0,scala.math.min(v, 360f)), 0, 360, rangeLo, rangeHi).toInt;
          if(!locked) {
            xRot = vMapped

            // create OSCMessage
            Message(parm, vMapped)
          } else {
            Message(parm, xRot)
          }

        })
      }

      case 0 => {

        scale = rangeLo + r.nextInt(rangeHi-rangeLo)
        nodeForm.scaleLocked = false

        oscTransmit.send <~ nodeForm.scaleFactor.map({ vv =>
          val v = scala.math.abs(vv)

        // clip at angles > 360 and map to parameter range, convert to Integer
          println("SCALE "+v)
          val vMapped: Int = Util.linlin(scala.math.max(0,scala.math.min(v, 3)), nodeForm.minimumScaleFactor, nodeForm.maximumScaleFactor, rangeLo, rangeHi).toInt;
          if(!locked) {
            scale = vMapped

            // create OSCMessage
            Message(parm, vMapped)
          } else {
            Message(parm, scale)
          }

        })
      }

      case _ =>  throw new Exception("Too many mappings given!")



    }

  })

  def bang = {
    // message conv functions auslagern?
    parameters.zipWithIndex.foreach{ x=>
      x._2 match {
        case 1 =>  nodeForm.rotationZ.bang
        case 2 =>  nodeForm.rotationY.bang
        case 3 =>  nodeForm.rotationX.bang
        case 0 =>  nodeForm.scaleFactor.bang
      }
    }
  }


  def updateHighlighting(currentSubdivision: Int) = {
    if(highlightSubdivision.isDefined) {
      val highlightWhen = highlightSubdivision.get

      if(highlightWhen._1 <= currentSubdivision && highlightWhen._2 >= currentSubdivision) {
        nodeForm.setHighlighted(0, true)
      } else {
        nodeForm.setHighlighted(0, false)
      }
    }
  }

  def name = parameters.toString


  def generateGene :  Gene = {
    val encSeq: List[IntegerEncoding] = ranges.zip(deviation).zipWithIndex.map{ x=>
      x._2 match {
        // basic deviation is 1
        case 1 => new IntegerEncoding(zRot, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case 2 => new IntegerEncoding(yRot, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case 3 => new IntegerEncoding(xRot, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case 0 => new IntegerEncoding(scale, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case _ =>  throw new Exception("Too many mappings given!")
      }
    }

    Gene(this.name, encSeq, SimpleGeneMutation, MultipleConcatenatingGeneCombination)
  }

  def generateRandomGene :  Gene = {
    val encSeq: List[IntegerEncoding] = ranges.zip(deviation).zipWithIndex.map{ x=>
      val rangeLo = x._1._1._1
      val rangeHi = x._1._1._2

      val randomValue = scala.math.max(scala.math.min(rangeLo + r.nextInt(rangeHi-rangeLo), rangeHi),rangeLo)

      x._2 match {
        // basic deviation is 1
        case 1 => new IntegerEncoding(randomValue, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case 2 => new IntegerEncoding(randomValue, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case 3 => new IntegerEncoding(randomValue, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case 0 => new IntegerEncoding(randomValue, new IntegerEncodingMutation2(x._1._1._1, x._1._1._2, x._1._2))
        case _ =>  throw new Exception("Too many mappings given!")
      }
    }

    Gene(this.name, encSeq, SimpleGeneMutation, MultipleConcatenatingGeneCombination)
  }

  def updateFromGene(gene: Gene) {

    nodeForm.reset()

    gene.sequence.zip(ranges).zip(parameters).zipWithIndex.foreach({ x=>
      val index = x._2
      val enc = x._1._1._1
      val rangeLo = x._1._1._2._1
      val rangeHi = x._1._1._2._2
      val parm = x._1._2

      val value = scala.math.max(scala.math.min(enc.toInt, rangeHi), rangeLo)

      println("ENCODING "+enc + " -- "+enc.toInt + " for parameter "+ parm + " r lo r hi"+rangeLo+ " "+ rangeHi
        + "\n value " + value+" " + Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat + "  --- "+Util.linlin(enc.toInt, rangeLo, rangeHi, 0, 360).toFloat)



      // these updates will trigger an update for the controlglue as well (sending the osc message)
      index match {
        case 1 => {
          nodeForm.updateZRoation( Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat )
        }

        case 2 => {
          nodeForm.updateYRoation( Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat )
        }

        case 3 => {
          nodeForm.updateXRoation( Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat )
        }

        case 0 => {
          nodeForm.updateScale( Util.linlin(value, rangeLo, rangeHi, nodeForm.minimumScaleFactor, nodeForm.maximumScaleFactor).toFloat )
        }

        case _ =>  throw new Exception("Too many mappings given!")
      }

    })



  }


}
