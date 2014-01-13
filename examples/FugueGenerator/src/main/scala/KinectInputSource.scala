package de.ghagerer.FugueGenerator

import java.net.{InetAddress, InetSocketAddress}
import org.mt4j.input.kinect.{Skeleton, KinectSkeletonSource}
import org.mt4j.util.math.Vector3D
import org.mt4j.input.inputData.{ActiveCursorPool, MTFingerInputEvt, InputCursor}
import org.mt4j.input.inputData.AbstractCursorInputEvt._
import org.mt4j.input.inputSources.AbstractInputSource
import akka.actor.{Props, ActorSystem, Actor}
import scala.collection.mutable.ArrayBuffer
import org.lodsb.reakt.async.{ReactiveA, ObserverReactiveA, VarA}
import org.mt4j.components.visibleComponents.shapes.{Ellipse, MTEllipse}
import org.mt4j.types.Vec3d
import org.mt4j.util.MTColor._

/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 08.01.14
 * Time: 15:40
 */
class KinectInputSource extends AbstractInputSource[MTFingerInputEvt](app) {

  // set network / osc data
  private val localhost = InetAddress.getLocalHost
  private val port = 3336
  private val socketAddress = new InetSocketAddress(localhost, port)
  val source = new KinectSkeletonSource(socketAddress)

  //synth.parameters <~ blah.skeletons(0).rightFoot.map(x: Vector3D => ("freq",x.x))


  /**
   * Pass a callback method to the input source, which gets called when a new skeleton appears
   * newSkeleton is the freshly recognized new skeleton
   */
  source.newSkeletonCallback = (newSkeleton: Skeleton) => {
    println("----------------------")
    println("Starting observation of a new skeleton....")

    val body = new SkeletonBody(this, newSkeleton)

    //InactivityAlarm.valuesToCheck += newSkeleton.rightHand
  }


  class SkeletonBody(val source: KinectInputSource, val skeleton: Skeleton) {

    val rightHand = new SkeletonHand(source, skeleton.rightHand)
    val leftHand = new SkeletonHand(source, skeleton.leftHand)

    var one: ReactiveA[Vector3D, Vector3D] = null
    var two: ReactiveA[Vector3D, Vector3D] = null
/*
    skeleton.alive.observe(observing => {
      if (observing) {
        if (one == null) {
          one = skeleton.rightHand.observe(rightHand.updatePosition)
        }
        if (two == null) {
          two = skeleton.leftHand.observe(leftHand.updatePosition)
        }
      } else {
        rightHand.stopMotion()
        leftHand.stopMotion()
        skeleton.rightHand.disconnectAll
        skeleton.leftHand.disconnectAll
      }
      true
    })
*/
  }

  /**
   * A class that represents a hand from a skeleton recognized by kinect
   * @param source the input source where the input signals come form
   */
  class SkeletonHand(val source: KinectInputSource, val hand: VarA[Vector3D]) {
    var handX: Float = 0
    var handY: Float = 0
    var handCursor: InputCursor = null
    var handEvent: MTFingerInputEvt = null
    var handID: Long = 0
    val guiCursor = Ellipse()
    guiCursor.setNoFill(true)
    guiCursor.setNoStroke(false)
    guiCursor.setStrokeColor(WHITE)
    guiCursor.removeAllGestureEventListeners()
    guiCursor.setPositionGlobal(Vec3d(guiCursor.globalPosition().getX, guiCursor.globalPosition().getY, -10f))

    app.scene.canvas().addChild(guiCursor)

    hand.observe(updatePosition)


    def stopMotion() {
      guiCursor.setVisible(false)
    }

    def updatePosition(position: Vector3D) = {

      if (!guiCursor.isVisible) {
        guiCursor.setVisible(true)
      }

      // calculate gesture coordinates
      handX = coordinateX(position.getX)
      handY = coordinateY(position.getY)

      guiCursor.setPositionGlobal(Vec3d(handX, handY, -10f))

      if (position.getZ < 2.0f) {
        if (handCursor == null) {
          // start gesture
          //println("_______________ starting gesture")
          handCursor = new InputCursor
          handID = handCursor.getId
          handEvent = new MTFingerInputEvt(source, handX, handY, INPUT_DETECTED, handCursor)
          ActiveCursorPool.getInstance.putActiveCursor(handID, handCursor)
        } else {
          // continue gesture
          //println("_______________ continuing gesture")
          handEvent = new MTFingerInputEvt(source, handX, handY, INPUT_UPDATED, handCursor)
        }
      } else if (handCursor != null) {
        // stop gesture
        //println("_______________ stopping gesture")
        handEvent = new MTFingerInputEvt(source, handX, handY, INPUT_ENDED, handCursor)
        ActiveCursorPool.getInstance.removeCursor(handID)
        handCursor = null
      } else {
        handEvent = null
      }

      if (handEvent != null) {
        // fire gesture input event
        println("_______________ before enqueuing")
        source.enqueueInputEvent(handEvent)
      }

      true
    }

    def coordinateX(xIn: Float) = app.width * (xIn+1)*0.5f
    def coordinateY(xIn: Float) = app.height * (1f - (xIn+1)*0.5f)

  }

  source.start()
  //InactivityAlarm() ! "start"

}



object InactivityAlarm {
  val system = ActorSystem("InactivityAlarmSystem")
  val inactivityAlarm = system.actorOf(Props[InactivityAlarm], name = "InactivityAlarm")
  val valuesToCheck = new ArrayBuffer[VarA[Vector3D]]()
  val memory = new ArrayBuffer[Vector3D]()
  def apply() = inactivityAlarm
  def addValue(value: VarA[Vector3D]) {
    valuesToCheck += value
    memory += value()
  }
  class InactivityAlarm extends Actor {
    private var running = false
    def receive = {
      case "start" =>
        running = true
        startCheckingActivity()
      case "stop" =>
        running = false
      case _ =>
    }
    def startCheckingActivity() {
      while (running) {
        for(i <- 0 to valuesToCheck.size-1) {
          if (valuesToCheck(i)().equalsVector(memory(i))) {
            // make i invisible
          }
        }
        Thread.sleep(2000l)
      }
    }
  }
}
