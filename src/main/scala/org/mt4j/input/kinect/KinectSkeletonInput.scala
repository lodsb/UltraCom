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
import org.lodsb.reakt.async.VarA
import org.mt4j.input.osc.{SignalingOSCReceiver, OSCCommunication}
import de.sciss.osc.{Message, UDP}

class Skeleton {
	private val initCoord = new Vector3D(1f, 1f, 1f)

	val alive = new VarA[Boolean](false);
	val head = new VarA[Vector3D](initCoord);
	val neck = new VarA[Vector3D](initCoord);
	val rightCollar = new VarA[Vector3D](initCoord);
	val rightShoulder = new VarA[Vector3D](initCoord);
	val rightElbow = new VarA[Vector3D](initCoord);
	val rightWrist = new VarA[Vector3D](initCoord);
	val rightHand = new VarA[Vector3D](initCoord);
	val rightFinger = new VarA[Vector3D](initCoord);
	val leftCollar = new VarA[Vector3D](initCoord);
	val leftShoulder = new VarA[Vector3D](initCoord);
	val leftElbow = new VarA[Vector3D](initCoord);
	val leftWrist = new VarA[Vector3D](initCoord);
	val leftHand = new VarA[Vector3D](initCoord);
	val leftFinger = new VarA[Vector3D](initCoord);
	val torso = new VarA[Vector3D](initCoord);
	val rightHip = new VarA[Vector3D](initCoord);
	val rightKnee = new VarA[Vector3D](initCoord);
	val rightAnkle = new VarA[Vector3D](initCoord);
	val rightFoot = new VarA[Vector3D](initCoord);
	val leftHip = new VarA[Vector3D](initCoord);
	val leftKnee = new VarA[Vector3D](initCoord);
	val leftAnkle = new VarA[Vector3D](initCoord);
	val leftFoot = new VarA[Vector3D](initCoord);
}

class KinectSkeletonSource(osceletonAddress: InetSocketAddress) {
	private val oscReceiver = OSCCommunication.createOSCReceiver(OSCCommunication.UDP, osceletonAddress);
	var started = false;

	def start(): Unit = {
		started = true;
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


	oscReceiver.receipt.observe {
		x => {
			// TODO: should be re-tested
			if (started) x match {
				case (m@Message("/joint", joint: String, id: Int, xCoord: Float, yCoord: Float, zCoord: Float)) => {
					joint match {
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
				}
				case (m@Message("/new_skel", id: Int)) => {
					skeletons(id).alive() = true;
				}
				case (m@Message("/lost_user", id: Int)) => {
					skeletons(id).alive() = false;
				}
				case (m@Message("/new_user", id: Int)) => {
					skeletons(id).alive() = false;
					println("Found user " + id + "! Please callibrate...");
				}
			}

			true;
		}

	}
}
