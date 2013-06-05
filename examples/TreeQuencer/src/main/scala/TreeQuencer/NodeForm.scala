package main.scala.TreeQuencer

import org.mt4j.util.math.{Tools3D, Vector3D}
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh
import org.mt4j.util.opengl.GLMaterial
import org.mt4j.components.{TransformSpace, MTComponent}
import java.io.File
import org.lodsb.reakt.async.VarA
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.inputProcessors.MTGestureEvent._
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleEvent
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateEvent
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DEvent
import java.awt.event.KeyEvent._
import scala.math._


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

class NodeForm(val file: File) extends MTComponent(app) {

  val minimumScaleFactor: Float = 0.8f
  val maximumScaleFactor: Float = 3f
  val scaleFactor = new VarA[Float](1f)
  val rotationX = new VarA[Float](0f)
  val rotationY = new VarA[Float](0f)
  val rotationZ = new VarA[Float](0f)

  //Set up a material to react to the light
  val material = new GLMaterial(Tools3D.getGL(app))
  //material.setAmbient(Array( 1f, .1f, .5f, 1f ))
  //material.setDiffuse(Array( 1f, .2f, .2f, 1f ))
  material.setEmission(Array( .0f, .0f, .0f, 1f ))
  material.setSpecular(Array( 0.2f, 0.2f, 0.2f, 1f ))  // almost white: very reflective
  material.setShininess(80) // 0=no shine,  127=max shine
  setLight(app.light)

  var meshes: Array[MTTriangleMesh] = FileImporter.cacheMTTriangleMesh(file)

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
      translationToScreenCenter.setValues(app.center.subtractLocal(center))
      //translationToScreenCenter.addLocal(new Vector3D(0f,0f,200f)) // bring it to front
      mesh.translateGlobal(translationToScreenCenter)
    }

  })

  // that all events are treated for the whole 3d object, not only its parts
  setComposite(true)

  // scale 3d object to adequate size on screen
  scaleGlobal(scale, scale, scale, center)

  // METHODS

  /**
   * Override center method. The biggest mesh is the center
   * @return Vector3D The center as 3D vector
   */
  override def getCenterPointGlobal: Vector3D = {
    biggestMesh.getCenterPointGlobal
  }
  override def setPositionGlobal(newPosition: Vector3D) {
    translateGlobal(newPosition.getSubtracted(center))
  }

  val X_AXIS = 1
  val Y_AXIS = 2
  val NONE = 0
  var rotationAxis = NONE
  def handleEvent(e: MTGestureEvent) {
    e match {

      case e: ScaleEvent =>
        scale(e.getScaleFactorX, e.getScaleFactorY)

      case e: RotateEvent =>
        rotateZGlobal(center, e.getRotationDegrees)
        rotationZ() += e.getRotationDegrees

      case e: DragEvent =>
        if (e.getId == GESTURE_UPDATED && app.keyPressed) {
          // when a key is pressed -> emulate multi-touch via mouse actions

          app.keyCode match {
            case VK_SHIFT => // scale while holding shift-key

              if (center.distance(e.getFrom) < center.distance(e.getTo)) {
                scale(1.01f)
              } else {
                scale(0.99f)
              }

            case VK_CONTROL => // rotate while holding ctrl-key

              // normalize vectors
              val from = e.getFrom.getSubtracted(center).getNormalized
              val to = e.getTo.getSubtracted(center).getNormalized

              // set the reference vector,
              // to know what is the directorion for positive angles.
              // It's 90 degrees to the positive direction within the x-y-plane.
              val referenceForward = from.getCross(new Vector3D(0,0,1))

              // get the sign for the angle
              val sign = if(0<to.dot(referenceForward)) -1f else 1f

              // get the angle
              var angle = sign * from.angleBetween(to)

              // transform angle in radians to degrees
              angle *= 180/Pi.toFloat

              rotateZGlobal(center, angle)
              rotationZ() += angle

            case VK_ALT => // rotate 3D while pressing Alt-key

              val direction = e.getTranslationVect

              // get angle between x-axis and drag direction
              val angle = new Vector3D(1,0,0).angleBetween(direction)*180f/Pi

              rotationAxis match {
                case NONE =>
                  // set rotation axis
                  if ((315<angle || angle<45) || (135<angle && angle<225)) {
                    rotationAxis = Y_AXIS
                  } else {
                    rotationAxis = X_AXIS
                  }
                case X_AXIS =>
                  rotateXGlobal(center, -direction.getY)
                  rotationX() += -direction.getY
                case Y_AXIS =>
                  rotateYGlobal(center, direction.getX)
                  rotationY() += direction.getX
                case _ =>
              }
          }

        } else {
          // if no key ist pressed, make a simple translation, just as usual...
          translateGlobal(e.getTranslationVect)
        }

        if (!app.keyPressed && rotationAxis != NONE || e.getId == GESTURE_ENDED) {
          // reset rotation axis after completed action
          rotationAxis = NONE
        }

      case e: Rotate3DEvent =>
        val firstCursorPosition = new Vector3D(e.getFirstCursor.getStartPosX, e.getFirstCursor.getStartPosY)
        val secondCursorPosition = new Vector3D(e.getSecondCursor.getStartPosX, e.getSecondCursor.getStartPosY)
        val connection = firstCursorPosition.getSubtracted(secondCursorPosition).getNormalized
        connection.setZ(0)
        val xNormal = new Vector3D(1,0,0)
        val angle = xNormal.angleBetween(connection)*180f/Pi
        if ((315<angle || angle<45) || (135<angle && angle<225)) {
          rotateXGlobal(e.getRotationPoint, e.getRotationDirection*e.getRotationDegreesX)
          rotationX() += e.getRotationDirection*e.getRotationDegreesX
        } else {
          rotateYGlobal(e.getRotationPoint, e.getRotationDirection*e.getRotationDegreesY)
          rotationY() += e.getRotationDirection*e.getRotationDegreesY
        }

      case _ =>
    }
  }

  /**
   * Modified scaling function, that also saves the global scale factor.
   * The scale factor is the scaling memory of this form.
   * The scaling factor in all directions (x,y,z) is the same with this function.
   * @param xVal must
   * @param yVal (optional)
   * @param zVal (optional)
   */
  def scale(xVal: Float, yVal: Float = 0f, zVal: Float = 0f) {
    var max = List(xVal,yVal).max
    val newScaleFactor = scaleFactor()*max

    if (newScaleFactor < minimumScaleFactor) {
      max =  minimumScaleFactor/scaleFactor()
      scaleFactor() = minimumScaleFactor
    } else if (maximumScaleFactor < newScaleFactor) {
      max =  maximumScaleFactor/scaleFactor()
      scaleFactor() = maximumScaleFactor
    } else {
      scaleFactor() = newScaleFactor
    }

    scaleGlobal(max, max, max, center)
  }

  /**
   * Syntactic sugar
   * @return the global center of this form
   */
  def center = getCenterPointGlobal

}
