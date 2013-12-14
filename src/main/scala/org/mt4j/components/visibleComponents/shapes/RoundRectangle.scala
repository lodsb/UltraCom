package org.mt4j.components.visibleComponents.shapes

import org.mt4j.MTApplication
import org.mt4j.util.math.Vector3D

/**
 * Created by lodsb on 12/13/13.
 */
object RoundRectangle {
  def apply(width: Float, height: Float, arcWidth:Float = 15f, arcHeight: Float = 15f): MTRoundRectangle = {

    val app = MTApplication.getInstance();

    val center = new Vector3D(app.width / 2f, app.height / 2f)

    val rectangle = new MTRoundRectangle(app, center.x, center.y, center.z, width, height, arcWidth, arcHeight);

    rectangle
  }

}