package main.scala.TreeQuencer

import org.mt4j.components.visibleComponents.shapes.MTEllipse
import processing.core.PApplet
import org.mt4j.util.math.{Vertex, Vector3D}

/**
 * Created with IntelliJ IDEA.
 * User: ghagerer
 * Date: 01.07.13
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
class WrapperEllipse(pApplet: PApplet, centerPoint: Vector3D, radiusX: Float, radiusY: Float)
  extends MTEllipse(pApplet: PApplet, centerPoint: Vector3D, radiusX: Float, radiusY: Float) {

  override def getVertices(resolution: Int): Array[Vertex] = {
    val vertices2 = super.getVertices(resolution)
    val vertices = new Array[Vertex](vertices2.size)
    for(i <- 0 to vertices2.size-2) {
      vertices(i+1) = vertices2(i)
    }
    vertices(0) = new Vertex(centerPoint)
    vertices(vertices.size-1) = new Vertex(centerPoint)

    //FIXME, multiple rotations
    if(this.getDegrees == 0f ) vertices2 else vertices

  }


}
