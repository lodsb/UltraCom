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
                   parameters: List[String], ranges: List[(Int,Int)]) {

  var xRot : Int = 0;
  var yRot : Int = 0;
  var zRot : Int = 0;
  var scale : Int = 0;

  var locked = false

  private val deviation = Mutator.mutationDeviation;

  parameters.zip(ranges).zipWithIndex.foreach({ x =>
    val index = x._2
    val parm = x._1._1
    val rangeLo = x._1._2._1
    val rangeHi = x._1._2._2


    index match {
       // x rot
      case 0 => {

        xRot = rangeLo

        oscTransmit.send <~ nodeForm.rotationX.map({ v =>
          println("X rot"+v)
          // clip at angles > 360 and map to parameter range, convert to Integer
          val vMapped = Util.linlin(scala.math.max(0,scala.math.min(v, 360f)), 0, 360, rangeLo, rangeHi).toInt;

          if(!locked) {
            xRot = vMapped

            // create OSCMessage
            Message(parm, vMapped)
          } else {
            Message(parm, xRot)
          }
        })
      }

      case 1 => {

        yRot = rangeLo

        oscTransmit.send <~ nodeForm.rotationY.map({ v =>
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

      case 2 => {

        zRot = rangeLo

        oscTransmit.send <~ nodeForm.rotationZ.map({ v =>
        // clip at angles > 360 and map to parameter range, convert to Integer
          val vMapped: Int = Util.linlin(scala.math.max(0,scala.math.min(v, 360f)), 0, 360, rangeLo, rangeHi).toInt;
          if(!locked) {
            zRot = vMapped

            // create OSCMessage
            Message(parm, vMapped)
          } else {
            Message(parm, zRot)
          }

        })
      }

      case 3 => {

        scale = rangeLo

        oscTransmit.send <~ nodeForm.scaleFactor.map({ v =>
        // clip at angles > 360 and map to parameter range, convert to Integer
          println("SCALE "+v)
          val vMapped: Int = Util.linlin(scala.math.max(0,scala.math.min(v, 3)), 0.8, 3, rangeLo, rangeHi).toInt;
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


  def name = parameters.toString


  def generateGene :  Gene = {
    val encSeq: List[IntegerEncoding] = ranges.zipWithIndex.map{ x=>
      x._2 match {
        // basic deviation is 1
        case 0 => new IntegerEncoding(xRot, new IntegerEncodingMutation2(x._1._1, x._1._2, 1+(x._1._2*deviation).toInt))
        case 1 => new IntegerEncoding(yRot, new IntegerEncodingMutation2(x._1._1, x._1._2, 1+(x._1._2*deviation).toInt))
        case 2 => new IntegerEncoding(zRot, new IntegerEncodingMutation2(x._1._1, x._1._2, 1+(x._1._2*deviation).toInt))
        case 3 => new IntegerEncoding(scale, new IntegerEncodingMutation2(x._1._1, x._1._2,1+(x._1._2*deviation).toInt))
        case _ =>  throw new Exception("Too many mappings given!")
      }
    }

    Gene(this.name, encSeq, SimpleGeneMutation, ConcatenatingGeneCombination)
  }

  def updateFromGene(gene: Gene) {
    val r = new Random
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
        case 0 => {
          nodeForm.updateXRoation( Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat )
        }

        case 1 => {
          nodeForm.updateYRoation( Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat )
        }

        case 2 => {
          nodeForm.updateZRoation( Util.linlin(value, rangeLo, rangeHi, 0, 360).toFloat )
        }

        case 3 => {
          nodeForm.updateScale( Util.linlin(value, rangeLo, rangeHi, 0.8, 3).toFloat )
        }

        case _ =>  throw new Exception("Too many mappings given!")
      }

    })



  }


}
