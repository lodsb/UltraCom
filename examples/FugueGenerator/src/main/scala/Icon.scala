package de.ghagerer.FugueGenerator

import de.ghagerer.FugueGenerator.app
import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MTColor
import org.mt4j.types.Vec3d
import org.mt4j.util.math.Vector3D

/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 06.01.14
 * Time: 21:14
 */


object Icon {
  def apply(filename: String, position: Vector3D, scaleFactor: Float, rotationDegrees: Float = 0f) = new Icon("pictures/"+filename, position, scaleFactor, rotationDegrees)
}

class Icon(val filename: String, position: Vector3D, scaleFactor: Float, rotationDegrees: Float) extends MTRectangle(app, app.loadImage(filename)) {

  setStrokeColor(new MTColor(255,255,255,0))
  scale(scaleFactor)
  setPositionGlobal(position)
  rotateZGlobal(globalPosition(), rotationDegrees)

  def scale(factor: Float) {
    setHeightLocal(getHeightLocal*factor)
    setWidthLocal(getWidthLocal*factor)
  }

}
