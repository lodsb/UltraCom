/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 11 :: 4 : 53
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
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

package org.mt4j.input.kinect

import org.mt4j.util.math.Vector3D
import java.net.InetSocketAddress
import org.mt4j.input.osc.OSCCommunication
import de.sciss.osc.UDP
import react.{Observing, Var}

class Skeleton {
	private val initCoord = new Vector3D(1f,1f,1f)

	val alive = new Var[Boolean](false);
	val head = new Var[Vector3D](initCoord);
	val neck = new Var[Vector3D](initCoord);
	val rightCollar = new Var[Vector3D](initCoord);
	val rightShoulder = new Var[Vector3D](initCoord);
	val rightElbow = new Var[Vector3D](initCoord);
	val rightWrist = new Var[Vector3D](initCoord);
	val rightHand = new Var[Vector3D](initCoord);
	val rightFinger = new Var[Vector3D](initCoord);
	val leftCollar = new Var[Vector3D](initCoord);
	val leftShoulder = new Var[Vector3D](initCoord);
	val leftElbow = new Var[Vector3D](initCoord);
	val leftWrist = new Var[Vector3D](initCoord);
	val leftHand = new Var[Vector3D](initCoord);
	val leftFinger = new Var[Vector3D](initCoord);
	val torso = new Var[Vector3D](initCoord);
	val rightHip = new Var[Vector3D](initCoord);
	val rightKnee = new Var[Vector3D](initCoord);
	val rightAnkle = new Var[Vector3D](initCoord);
	val rightFoot = new Var[Vector3D](initCoord);
	val leftHip = new Var[Vector3D](initCoord);
	val leftKnee = new Var[Vector3D](initCoord);
	val leftAnkle = new Var[Vector3D](initCoord);
	val leftFoot = new Var[Vector3D](initCoord);
}

class KinectSkeletonSource(osceletonAddress: InetSocketAddress) extends Observing {
	private val oscReceiver = OSCCommunication.createOSCReceiver(UDP, osceletonAddress)


	def start(): Unit = {
		oscReceiver.start
	}

	// currently predefined
	val skeletons = Map(
		0 -> new Skeleton,
		1 -> new Skeleton,
		2 -> new Skeleton,
		3 -> new Skeleton,
		4 -> new Skeleton,
		5 -> new Skeleton
	)


	observe(oscReceiver.receipt) {
		x => {
			val message = x._1

			if (message.name.equals("/joint")) {
				val args = message.args
				val id = args(1).asInstanceOf[Int]

				val xCoord = args(2).asInstanceOf[Float]
				val yCoord = args(3).asInstanceOf[Float]
				val zCoord = args(4).asInstanceOf[Float]

				args(0) match {
					case "head" => skeletons(id).head() = new Vector3D(xCoord, yCoord, zCoord)
					case "neck" => skeletons(id).neck() = new Vector3D(xCoord, yCoord, zCoord)

					case "r_collar" => skeletons(id).rightCollar() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_shoulder" => skeletons(id).rightShoulder() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_elbow" => skeletons(id).rightElbow() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_wrist" => skeletons(id).rightWrist() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_hand" => skeletons(id).rightHand() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_finger" => skeletons(id).rightFinger() = new Vector3D(xCoord, yCoord, zCoord)

					case "l_collar" => skeletons(id).leftCollar() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_shoulder" => skeletons(id).leftShoulder() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_elbow" => skeletons(id).leftElbow() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_wrist" => skeletons(id).leftWrist() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_hand" => skeletons(id).leftHand() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_finger" => skeletons(id).leftFinger() = new Vector3D(xCoord, yCoord, zCoord)

					case "torso" => skeletons(id).torso() = new Vector3D(xCoord, yCoord, zCoord)

					case "r_hip" => skeletons(id).rightHip() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_knee" => skeletons(id).rightKnee() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_ankle" => skeletons(id).rightAnkle() = new Vector3D(xCoord, yCoord, zCoord)
					case "r_foot" => skeletons(id).rightFoot() = new Vector3D(xCoord, yCoord, zCoord)

					case "l_hip" => skeletons(id).leftHip() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_knee" => skeletons(id).leftKnee() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_ankle" => skeletons(id).leftAnkle() = new Vector3D(xCoord, yCoord, zCoord)
					case "l_foot" => skeletons(id).leftFoot() = new Vector3D(xCoord, yCoord, zCoord)
				}
			} else if (message.name.equals("/new_skel")) {
				val args = message.args
				val id = args(0).asInstanceOf[Int]

				skeletons(id).alive() = true;
			} else if (message.name.equals("/lost_user")) {
				val args = message.args
				val id = args(0).asInstanceOf[Int]

				skeletons(id).alive() = false;
			} else if (message.name.equals("/new_user")) {
				val args = message.args
				val id = args(0).asInstanceOf[Int]

				println("Found user " + id + "! Please callibrate...");
			}

			true;
		}

	}
}
