package org.mt4j.components.visibleComponents.widgets

/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 12 :: 10 : 2
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

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.math.Vector3D
import org.mt4j.input.kinect.{Skeleton, KinectSkeletonSource}
import processing.core.{PGraphics, PApplet}
import org.mt4j.util.MTColor
import org.mt4j.components.visibleComponents.font.FontManager
import java.io.File
import collection.mutable.{WrappedArray}

class KinectSkeletonViewer(applet: PApplet, x: Int, y: Int, width: Int, height: Int, depth: Int,
						   private val skeleton: Skeleton)
	extends MTRectangle(applet, x, y, width, height) {


	val joints = Array(
		/* head 		  */ new Vector3D(0, 0, 0),
		/* neck 		  */ new Vector3D(0, 0, 0),
		/* rightCollar 	  */ new Vector3D(0, 0, 0),
		/* rightShoulder  */ new Vector3D(0, 0, 0),
		/* rightElbow 	  */ new Vector3D(0, 0, 0),
		/* rightWrist 	  */ new Vector3D(0, 0, 0),
		/* rightHand 	  */ new Vector3D(0, 0, 0),
		/* rightFinger 	  */ new Vector3D(0, 0, 0),
		/* leftCollar 	  */ new Vector3D(0, 0, 0),
		/* leftShoulder   */ new Vector3D(0, 0, 0),
		/* leftElbow 	  */ new Vector3D(0, 0, 0),
		/* leftWrist 	  */ new Vector3D(0, 0, 0),
		/* leftHand 	  */ new Vector3D(0, 0, 0),
		/* leftFinger 	  */ new Vector3D(0, 0, 0),
		/* torso 		  */ new Vector3D(0, 0, 0),
		/* rightHip 	  */ new Vector3D(0, 0, 0),
		/* rightKnee 	  */ new Vector3D(0, 0, 0),
		/* rightAnkle 	  */ new Vector3D(0, 0, 0),
		/* rightFoot 	  */ new Vector3D(0, 0, 0),
		/* leftHip 		  */ new Vector3D(0, 0, 0),
		/* leftKnee 	  */ new Vector3D(0, 0, 0),
		/* leftAnkle 	  */ new Vector3D(0, 0, 0),
		/* leftFoot 	  */ new Vector3D(0, 0, 0),
		/* leftFoot 	  */ new Vector3D(0, 0, 0)
	)


		skeleton.head.observe {
			vec => joints(0) = vec; true;
		}

		skeleton.neck.observe {
			vec => joints(1) = vec; true;
		}

		skeleton.rightCollar.observe {
			vec => joints(3) = vec; true;
		}

		skeleton.leftCollar.observe {
			vec => joints(4) = vec; true;
		}

		skeleton.rightShoulder.observe {
			vec => joints(5) = vec; true;
		}

		skeleton.leftShoulder.observe {
			vec => joints(6) = vec; true;
		}

		skeleton.rightElbow.observe {
			vec => joints(7) = vec; true;
		}

		skeleton.leftElbow.observe {
			vec => joints(8) = vec; true;
		}

		skeleton.rightWrist.observe {
			vec => joints(9) = vec; true;
		}

		skeleton.leftWrist.observe {
			vec => joints(10) = vec; true;
		}

		skeleton.rightHand.observe {
			vec => joints(11) = vec; true;
		}

		skeleton.leftHand.observe {
			vec => joints(12) = vec; true;
		}

		skeleton.rightFinger.observe {
			vec => joints(13) = vec; true;
		}

		skeleton.leftFinger.observe {
			vec => joints(14) = vec; true;
		}

		skeleton.torso.observe {
			vec => joints(15) = vec; true;
		}

		skeleton.rightHip.observe {
			vec => joints(16) = vec; true;
		}

		skeleton.leftHip.observe {
			vec => joints(17) = vec; true;
		}

		skeleton.rightKnee.observe {
			vec => joints(18) = vec; true;
		}

		skeleton.leftKnee.observe {
			vec => joints(19) = vec; true;
		}

		skeleton.rightAnkle.observe {
			vec => joints(20) = vec; true;
		}

		skeleton.leftAnkle.observe {
			vec => joints(21) = vec; true;
		}

		skeleton.rightFoot.observe {
			vec => joints(22) = vec; true;
		}

		skeleton.leftFoot.observe {
			vec => joints(23) = vec; true;
		}

	private var color = new MTColor(200f, 120f, 6f);

	private def drawArrow(x1: Int, y1: Int, x2: Int, y2: Int) {
		val renderer = this.getRenderer

		renderer.strokeWeight(2.5f)
		renderer.line(x1, y1, x2, y2);


		renderer.pushMatrix();
		renderer.translate(x2, y2);
		renderer.scale(0.1f)
		val a = Math.atan2(x1 - x2, y2 - y1);
		renderer.rotate(a.toFloat);
		renderer.line(0, 0, -1, -1);
		renderer.line(0, 0, 1, -1);

		renderer.popMatrix();

		renderer.strokeWeight(1)

	}


	var r = 0.0f;

	private def drawJoint(renderer: PApplet, x: Vector3D) = {
		renderer.pushMatrix
		renderer.translate(x.x, x.y, -x.z)
		renderer.sphere(0.02f)
		renderer.popMatrix
	}

	override def drawComponent(g: PGraphics): Unit = {
		//this.setStrokeColor(color)
		//this.setNoFill(true)
		//super.drawComponent(g);
		val renderer = this.getRenderer();

		renderer.pushMatrix
		renderer.noFill

		renderer.translate(this.x + this.width / 2, this.y + this.height / 2)
		renderer.rotateY(r)
		r = r + 0.01f

		renderer.box(this.width, this.height, this.depth)

		renderer.scale(this.width, this.height, this.depth)


		renderer.stroke(255f, 0f, 0f);

		renderer.pushMatrix
		renderer.translate(0, -0.5f, 0.5f)

		renderer.pushMatrix
		renderer.translate(0.40f, -0.15f)
		renderer.scale(0.25f)
		renderer.line(0, 0, 0.5f, 0.5f)
		renderer.line(0, 0.5f, 0.5f, 0)
		renderer.popMatrix

		renderer.scale(0.5f)
		this.drawArrow(1, 0, 0, 0);
		renderer.popMatrix

		renderer.stroke(0f, 255f, 0f)

		renderer.pushMatrix
		renderer.translate(0.5f, -0.5f, 0.5f)


		renderer.pushMatrix
		renderer.translate(0.10f, 0)
		renderer.scale(0.25f)
		renderer.line(0, 0, 0.25f, 0.2f)
		renderer.line(0.25f, 0.20f, 0.5f, 0.0f)
		renderer.line(0.25f, 0.2f, 0.25f, 0.5f)
		renderer.popMatrix

		renderer.scale(0.5f)
		this.drawArrow(0, 0, 0, 1);
		renderer.popMatrix


		renderer.stroke(0f, 0f, 255f)

		renderer.pushMatrix
		renderer.translate(0.5f, -0.5f, 0.5f)
		renderer.rotateX((-Math.Pi / 2f).toFloat)

		renderer.pushMatrix
		renderer.translate(0.10f, 0)
		renderer.scale(0.25f)
		renderer.line(0.5f, 0, 0, 0)
		renderer.line(0.0f, 0, 0.5f, 0.5f)
		renderer.line(0.5f, 0.5f, 0, 0.5f)
		renderer.popMatrix

		renderer.scale(0.5f)
		this.drawArrow(0, 0, 0, 1);
		renderer.popMatrix


		renderer.stroke(color.getR, color.getG, color.getB)
		renderer.fill(color.getR, color.getG, color.getB)

		renderer.translate(-0.5f, -0.5f, 1.5f)

		//joints.foreach(drawJoint(renderer, _) )

		/*joints.foreach({
			x => {
				renderer.pushMatrix
				renderer.translate(x.x, x.y, -x.z)
				renderer.sphere(0.02f)
				renderer.popMatrix
			}
		})*/

		var i = 0;
		var x: Vector3D = joints(0)

		while(i < joints.size) {
			//renderer.pushMatrix
			renderer.translate(x.x, x.y, -x.z)
			renderer.box(0.02f)
			renderer.translate(-x.x, -x.y, x.z)
			//renderer.popMatrix
			x = joints(i)
			i += 1
		}

		renderer.popMatrix

	}


}
