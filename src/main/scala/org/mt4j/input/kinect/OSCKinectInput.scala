package org.mt4j.input.kinect

/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 07.01.14
 * Time: 07:39
 */
import org.mt4j.util.math.Vector3D
import java.net.InetSocketAddress
import org.lodsb.reakt.async.VarA
import org.mt4j.input.osc.SignalingOSCReceiver
import org.mt4j.input.osc.OSCCommunication
import org.mt4j.input.osc.{SignalingOSCReceiver, OSCCommunication}
import de.sciss.osc.{Bundle, Packet, Message, UDP}
import org.mt4j.util.math.Vector3D
import org.lodsb.reakt.async.VarA
import scala.collection.mutable.ArrayBuffer
import scala.Float

class Skeleton {
  private val initCoord = new Vector3D(1f, 1f, 1f)

  val alive = new VarA[Boolean](false) //?

  //val hipCentre = new VarA[Vector3D](initCoord)
  //val spine = new VarA[Vector3D](initCoord)
  //val shoulderCentre = new VarA[Vector3D](initCoord)
  //val head = new VarA[Vector3D](initCoord);
  //val leftShoulder = new VarA[Vector3D](initCoord);
  //val leftElbow = new VarA[Vector3D](initCoord);
  //val leftWrist = new VarA[Vector3D](initCoord);
  val leftHand = new VarA[Vector3D](initCoord);
  //val rightShoulder = new VarA[Vector3D](initCoord);
  //val rightElbow = new VarA[Vector3D](initCoord);
  //val rightWrist = new VarA[Vector3D](initCoord);
  val rightHand = new VarA[Vector3D](initCoord);
  //val leftHip = new VarA[Vector3D](initCoord);
  //val leftKnee = new VarA[Vector3D](initCoord);
  //val leftAnkle = new VarA[Vector3D](initCoord);
  //val leftFoot = new VarA[Vector3D](initCoord);
  //val rightHip = new VarA[Vector3D](initCoord);
  //val rightKnee = new VarA[Vector3D](initCoord);
  //val rightAnkle = new VarA[Vector3D](initCoord);
  //val rightFoot = new VarA[Vector3D](initCoord);

}

class KinectSkeletonSource(osceletonAddress: InetSocketAddress) {
  private val oscReceiver = OSCCommunication.createOSCReceiver(OSCCommunication.UDP, osceletonAddress);
  var started = false;

  def start(): Unit = {
    started = true;
  }


  // currently predefined
  val sk: Map[Int, Skeleton] = Map(
    0 -> new Skeleton,
    1 -> new Skeleton,
    2 -> new Skeleton,
    3 -> new Skeleton,
    4 -> new Skeleton,
    5 -> new Skeleton
  )


  var skeletonList: Map[Int, Skeleton] = Map()
  var newSkeletonCallback: Skeleton => Unit = null

  def skeletons(id: Int): Skeleton = {
    try {
      return skeletonList(id)
    } catch {
      case e: Exception =>
        skeletonList += (id -> new Skeleton)
        if (newSkeletonCallback != null) {
          newSkeletonCallback(skeletonList(id))
        }
        return skeletonList(id)
    }
  }

  var zaehler = 0
  oscReceiver.receipt.observe {
    (x: (Message, InetSocketAddress)) => {
      if (started) {
        try {
          val foo: Message = x._1
        } catch {
          case e: ClassCastException => {
            val bundle: Bundle = x._1.asInstanceOf[Bundle]
            val packets: Seq[Packet] = bundle.packets
            packets.foreach((p: Packet) => {
              val m = p.asInstanceOf[Message]
              val (xCoord, yCoord, zCoord) = (m(0).asInstanceOf[Float], m(1).asInstanceOf[Float], m(2).asInstanceOf[Float])
              val messageComponents = m.name.split("/")
              val messageType = messageComponents(1)
              val id = messageComponents(if(messageType=="joint") 3 else 2).toInt
              messageType match {
                case "position" =>
                case "new_skel" =>
                  skeletons(id).alive() = true
                case "new_user" =>
                  skeletons(id).alive() = false
                case "lost_user" =>
                  skeletons(id).alive() = false
                case "joint" =>
                  //println("Got joint message: "+p.asInstanceOf[Message])
                  val bodyPart = messageComponents(2)
                  bodyPart match {
                    //case "head" => skeletons(id).head() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "shoulder_centre" => skeletons(id).shoulderCentre() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "hip_centre" => skeletons(id).hipCentre() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "spine" => skeletons(id).spine() = new Vector3D(xCoord, yCoord, zCoord)

                    //case "shoulder_right" => skeletons(id).rightShoulder() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "elbow_right" => skeletons(id).rightElbow() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "wrist_right" => skeletons(id).rightWrist() = new Vector3D(xCoord, yCoord, zCoord)
                    case "hand_right" => skeletons(id).rightHand() = new Vector3D(xCoord, yCoord, zCoord)

                    //case "shoulder_left" => skeletons(id).leftShoulder() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "elbow_left" => skeletons(id).leftElbow() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "wrist_left" => skeletons(id).leftWrist() = new Vector3D(xCoord, yCoord, zCoord)
                    case "hand_left" => skeletons(id).leftHand() = new Vector3D(xCoord, yCoord, zCoord)

                    //case "hip_right" => skeletons(id).rightHip() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "knee_right" => skeletons(id).rightKnee() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "ankle_right" => skeletons(id).rightAnkle() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "foot_right" => skeletons(id).rightFoot() = new Vector3D(xCoord, yCoord, zCoord)

                    //case "hip_left" => skeletons(id).leftHip() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "knee_left" => skeletons(id).leftKnee() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "ankle_left" => skeletons(id).leftAnkle() = new Vector3D(xCoord, yCoord, zCoord)
                    //case "foot_left" => skeletons(id).leftFoot() = new Vector3D(xCoord, yCoord, zCoord)
                  }
                case _ =>
              }
            })
          }
        }
      }
    }
    true
  }

}

