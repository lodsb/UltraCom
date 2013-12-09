package org.mt4j.input.inputProcessors.globalProcessors

import org.mt4j.input.inputData.{AbstractCursorInputEvt, MTFingerInputEvt}
import org.mt4j.types.Vec3d
import org.mt4j.util.camera.Icamera
import org.mt4j.components.MTCanvas
import org.mt4j.sceneManagement.Iscene
import org.mt4j.Scene

/**
 * Created with IntelliJ IDEA.
 * User: lodsb
 * Date: 12/9/13
 * Time: 8:55 AM
 * To change this template use File | Settings | File Templates.
 */
class CameraProcessor(scene: Scene) extends AbstractGlobalInputProcessor[MTFingerInputEvt] {
  var lx, ly, lz = 0.0f
  var mouseBusy = false;
  val cam = scene.getSceneCam

  def processInputEvtImpl(inputEvent: MTFingerInputEvt) {
    val gx = inputEvent.getScreenX
    val gy = inputEvent.getScreenY
    if (inputEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED && mouseBusy) {
      if (inputEvent.isMouseOrigin) {

        val dx = (lx - inputEvent.getScreenX)
        val dy = (ly - inputEvent.getScreenY)

        lx = gx
        ly = gy



        val pos = cam.getPosition
        val vp = cam.getViewCenterPos

        inputEvent.getModifiers match {
          case 0 => {
            cam.setPosition(Vec3d(pos.x + dx, pos.y + dy, pos.z, pos.w))
            cam.setViewCenterPos(Vec3d(vp.x + dx, vp.y + dy, vp.z, vp.w))
          }

          case 1 => {
            cam.setPosition(Vec3d(pos.x + dx, pos.y, pos.z + dy, pos.w))
          }

          case 2 => {
            cam.setViewCenterPos(Vec3d(vp.x + dx, vp.y + dy, vp.z, vp.w))
          }

          case 8 => {
            cam.setPosition(Vec3d(vp.x + dx, vp.y + dy, pos.z, pos.w))
            cam.setViewCenterPos(Vec3d(vp.x + dx, vp.y + dy, vp.z, vp.w))
          }

          case _ => {

          }

        }
      }
    } else if (inputEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
      mouseBusy = false;
    } else if (inputEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED) {
      if (scene.canvas == scene.canvas.getComponentAt(gx, gy)) {
        lx = gx
        ly = gy
        mouseBusy = true
      }
    }
  }
}
