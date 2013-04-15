package cyntersizer

import org.mt4j.util.MTColor
import org.mt4j.components.visibleComponents.shapes.{MTPolygon, MTRectangle}
import org.mt4j.util.math.{Vector3D, Vertex}
import processing.core.PApplet

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  13.03.13 :: 16:36
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 *  ^     ^
 *   ^   ^
 *   (o o)
 *  {  |  }                  (Wong)
 *     "
 *
 * Don't eat the pills!
 */


class SquareForm(width: Float, color: MTColor)
  extends MTRectangle(app, new Vertex(app.center), width, width) {

  setFillColor(color)
}

class TriangleForm(width: Float, color: MTColor)
  extends MTPolygon(app, Array(new Vertex(),new Vertex(),new Vertex())) {

  // create all vertices from the 2d triangle
  val upperVertex = new Vertex(0,width,0)
  val nextVertex = new Vertex(upperVertex.getCopy.rotateAroundAxisLocal(new Vector3D(0,0,1), (120*math.Pi/180).toFloat))
  val anotherVertex = new Vertex(nextVertex.getCopy.rotateAroundAxisLocal(new Vector3D(0,0,1), (120*math.Pi/180).toFloat))

  // translate the triangle to center
  upperVertex.addLocal(app.center)
  nextVertex.addLocal(app.center)
  anotherVertex.addLocal(app.center)

  // set vertices for the triangle
  super.setVertices(Array(upperVertex,nextVertex,anotherVertex))

  // set the color
  setFillColor(color)
}