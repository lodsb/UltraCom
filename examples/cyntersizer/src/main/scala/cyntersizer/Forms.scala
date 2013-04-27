package cyntersizer

import org.mt4j.util.MTColor
import org.mt4j.components.visibleComponents.shapes.{MTPolygon, MTRectangle, MTEllipse}
import org.mt4j.util.math.{Vector3D, Vertex}
import org.mt4j.components.visibleComponents.shapes.mesh.{MTTriangleMesh, MTCube, MTSphere}
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DProcessor

import org.mt4j.util.math.Tools3D
import org.mt4j.input.gestureAction.{DefaultScaleAction, DefaultRotateAction, Rotate3DAction}
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.{ScaleEvent, ScaleProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor
import org.mt4j.util.opengl.{GLTextureSettings, GLTexture}
import org.mt4j.components.MTComponent
import org.mt4j.util.modelImporter.ModelImporterFactory
import java.io.FileNotFoundException

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

trait Form {
  var _radius: Float
  var color: MTColor

  def radius(): Float = _radius
  def radius(newRadius: Float) = _radius = newRadius
}

class SquareForm(var _radius: Float, var color: MTColor)
  //extends MTRectangle(app, new Vertex(app.center.subtractLocal(new Vector3D(_radius, _radius))), 2*_radius, 2*_radius)
  extends MTCube(app, 2*_radius)
  with Form {

  val centerCopy = app.center.getCopy
  centerCopy.setZ(SourceNode().form.getCenterPointGlobal.getZ+1)
  setPositionGlobal(centerCopy)

  setFillColor(color)

  setTexture(new GLTexture(app, "worldmap.jpg", new GLTextureSettings(GLTexture.TEXTURE_TARGET.RECTANGULAR, GLTexture.SHRINKAGE_FILTER.Trilinear, GLTexture.EXPANSION_FILTER.Bilinear, GLTexture.WRAP_MODE.CLAMP_TO_EDGE, GLTexture.WRAP_MODE.CLAMP_TO_EDGE)));

}

class TriangleForm(var _radius: Float, var color: MTColor)
  //extends MTPolygon(app, Array(new Vertex(),new Vertex(),new Vertex()))
  extends MTComponent(app)
  with Form {
/*
  // create all vertices from the 2-dimensional triangle
  val upperVertex = new Vertex(0,_radius,0)
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
*/

  setPositionGlobal(app.center.getScaled(0.33f))
  println("TriangleForm First: "+getCenterPointGlobal)
  val meshGroup = new MTComponent(app)
  val meshes = ModelImporterFactory.loadModel(app, "AndroBot.3ds", 180, true, false )
  meshes.foreach(mesh =>{
    meshGroup.addChild(mesh)
    mesh.unregisterAllInputProcessors()
    mesh.setPickable(true)
    val normals = mesh.getGeometryInfo().getNormals()
    normals.foreach( vector3d => {
      vector3d.scaleLocal(-1)
    })
    mesh.getGeometryInfo().setNormals(mesh.getGeometryInfo().getNormals(), mesh.isUseDirectGL(), mesh.isUseVBOs());
    if (mesh.getVertexCount() > 20) {
      mesh.generateAndUseDisplayLists()
    }
    mesh.setDrawNormals(false)
  })
  meshGroup.setComposite(true)
  meshGroup.setPositionGlobal(app.center)
  addChild(meshGroup)

  println("TriangleForm Second: "+getCenterPointGlobal.toString)
  println(meshGroup.getCenterPointGlobal.toString)

}

class CircleForm(var _radius: Float, var color: MTColor)
//extends MTEllipse(app, app.center, _radius, _radius)
extends MTSphere(app, "", 40, 40, _radius)
  with Form {

  try {
    SourceNode().form
    val centerCopy = app.center.getCopy
    centerCopy.setZ(SourceNode().form.getCenterPointGlobal.getZ+1)
    setPositionGlobal(centerCopy)

  } catch {
    case npe: NullPointerException => {
      setPositionGlobal(app.center)
    }
  }
  setFillColor(color)

  setTexture(new GLTexture(app, "moonmap.jpg", new GLTextureSettings(GLTexture.TEXTURE_TARGET.RECTANGULAR, GLTexture.SHRINKAGE_FILTER.Trilinear, GLTexture.EXPANSION_FILTER.Bilinear, GLTexture.WRAP_MODE.CLAMP_TO_EDGE, GLTexture.WRAP_MODE.CLAMP_TO_EDGE)));

}
