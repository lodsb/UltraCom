package TreeQuencer

import org.mt4j.util.math.{Vertex, Ray, Tools3D, Vector3D}
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh
import org.mt4j.util.opengl.GLMaterial
import org.mt4j.components.{TransformSpace, MTComponent}
import org.mt4j.util.modelImporter.ModelImporterFactory
import java.io.File
import org.mt4j.components.bounds.{BoundingSphere, IBoundingShape}
import org.mt4j.util.camera.IFrustum
import collection.mutable.ArrayBuffer


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

class ImportedForm(val file: File) extends MTComponent(app) {

  //Set up a material to react to the light
  val material = new GLMaterial(Tools3D.getGL(app))
  material.setAmbient(Array( .5f, .5f, .5f, 1f ))
  material.setDiffuse(Array( .8f, .8f, .8f, 1f ))
  material.setEmission(Array( .0f, .0f, .0f, 1f ))
  material.setSpecular(Array( 0.9f, 0.9f, 0.9f, 1f ))  // almost white: very reflective
  material.setShininess(110) // 0=no shine,  127=max shine
  setLight(app.light)

  var meshes = ModelImporterFactory.loadModel(app, file.getAbsolutePath, 180, true, false)

  //Get the biggest mesh and extract its width
  private var biggestWidth = Float.MinValue
  var biggestMesh: MTTriangleMesh = null
  meshes.foreach( mesh => {
    val width = mesh.getWidthXY(TransformSpace.GLOBAL)
    if (biggestWidth < width){
      biggestWidth = width
      biggestMesh = mesh
    }
  })

  var scale = app.width*0.1f/biggestWidth

  val vertices = new ArrayBuffer[Vector3D]()

  meshes.foreach( mesh => {
    addChild(mesh)

    mesh.unregisterAllInputProcessors()
    mesh.removeAllGestureEventListeners()

    mesh.setPickable(true)

    //If the mesh has more than 20 vertices, use a Vertex Buffer Object for faster drawing
    if (mesh.getVertexCount > 20) {
      mesh.setUseVBOs(true)
    }

    //Set the material to the mesh  (determines the reaction to the lightning)
    mesh.setMaterial(material)
    mesh.setDrawNormals(false)

    if (biggestMesh != null) {
      // translate to center
      val translationToScreenCenter = new Vector3D()
      translationToScreenCenter.setValues(app.center.subtractLocal(getCenterPointGlobal()))
      //translationToScreenCenter.addLocal(new Vector3D(0f,0f,200f)) // bring it to front
      mesh.translateGlobal(translationToScreenCenter)
    }

    vertices ++= mesh.getVerticesGlobal

  })

  // that all events are treated for the whole 3d object, not only its parts
  setComposite(true)

  // scale 3d object to adequate size on screen
  scaleGlobal(scale, scale, scale, getCenterPointGlobal())


  // METHODS

  /**
   * Override center method. The biggest mesh is the center
   * @return Vector3D The center as 3D vector
   */
  override def getCenterPointGlobal: Vector3D = {
    biggestMesh.getCenterPointGlobal
  }
  override def setPositionGlobal(newPosition: Vector3D) {
    translateGlobal(newPosition.getSubtracted(getCenterPointGlobal))
  }
}
