package mutator

import org.mt4j.MTApplication
import org.mt4j.util.math.{Tools3D, Vector3D}
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh
import org.mt4j.components.{TransformSpace, MTComponent}
import java.io.File
import org.lodsb.reakt.async.VarA
import org.lodsb.reakt.sync.VarS
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.inputProcessors.MTGestureEvent._
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.{ScaleProcessor, ScaleEvent}
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.{RotateProcessor, RotateEvent}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragProcessor, DragEvent}
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.{Rotate3DProcessor, Rotate3DEvent}
import java.awt.event.KeyEvent._
import scala.math._
import org.mt4j.types.{Rotation, Vec3d}
import org.mt4j.util.SessionLogger._
import org.mt4j.util.{Color, MTColor, SessionLogger}
import org.mt4j.util.opengl.GLMaterial
import org.mt4j.components.visibleComponents.shapes.MTEllipse


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

object RandomNodeForm {
  def apply(pos: Vector3D): NodeForm = {
    val node = Import.form(FileImporter.randomFormFile)
    node.setPositionGlobal(pos)
    node
  }
}

object SeqNodeForm {
  def apply(pos: Vector3D, no: Int): NodeForm = {
    val node = Import.form(FileImporter.numFormFile(no))
    node.setPositionGlobal(pos)
    node
  }
}

class NodeForm(val file: File, app: org.mt4j.Application) extends MTComponent(app) {

  val defaultFill = Color.WHITE;//Color(80,80,80,255)

  // for locking certain parameters from being modified
  var xRotLocked = true
  var yRotLocked = true
  var zRotLocked = true
  var scaleLocked = true

  var isGrey = false
  val minimumScaleFactor = 0.5f
  val maximumScaleFactor = 2f
  val maxDeg = 362.0

  var isMoveable = false;

  //using synchronous signals to circumvent huge delays by scheduliung and message passing
  var scaleFactor = new VarS[Float](1f)
  var rotationX = new VarS[Float](0f)
  var rotationY = new VarS[Float](0f)
  var rotationZ = new VarS[Float](0f)

  private val appCenter = Vec3d(app.height/2,app.width/2)
  private val nodeZ = -20


  setName(file.getName)
  //setLight(app.light)

  // Set up a material to react to the light
  var material = (FileImporter.cacheGLMaterial(new File(file.getAbsolutePath.replace(".obj", "_material.scala")))).copy

  println("MY MATERIAL "+material.name + " this "+this)

  val materialCopy = material.copy

  // meshes, that build the form
  val meshes = FileImporter.triangleMesh(file)

  // Get the biggest mesh and extract its width
  private var biggestWidth = Float.MinValue
  var biggestMesh: MTTriangleMesh = null
  meshes.foreach( mesh => {
    val width = mesh.getWidthXY(TransformSpace.GLOBAL)
    if (biggestWidth < width){
      biggestWidth = width
      biggestMesh = mesh
    }
  })

  var scale = app.width*0.08f/biggestWidth

  meshes.foreach( mesh => {
    println("meshes "+meshes.size)
    addChild(mesh)

    mesh.unregisterAllInputProcessors()
    mesh.removeAllGestureEventListeners()

    mesh.setPickable(true)

    //If the mesh has more than 20 vertices, use a Vertex Buffer Object for faster drawing
    //if (mesh.getVertexCount > 20) {
    //  mesh.setUseVBOs(true)
    //}
    //material.setDefaults

    //Set the material to the mesh  (determines the reaction to the lightning)
    println(material.getAmbient)
    println("loaded ... "+material.name)
    mesh.setMaterial(material)
    mesh.setDrawNormals(false)
    mesh.setFillColor(defaultFill)

    // translate to center
    mesh.translateGlobal(appCenter.getSubtracted(position))
    mesh.translateGlobal(Vec3d(0,0,nodeZ))

  })

  // that all events are treated for the whole 3d object, not only its parts
  setComposite(true)

  // scale 3d object to adequate size on screen
  scaleGlobal(scale, scale, scale, position)

  // register input listeners
  this.registerInputProcessor(new Rotate3DProcessor(app, this))
  this.addGestureListener(classOf[Rotate3DProcessor], this)
  this.registerInputProcessor(new RotateProcessor(app))
  this.addGestureListener(classOf[RotateProcessor], this)
  this.registerInputProcessor(new ScaleProcessor(app))
  this.addGestureListener(classOf[ScaleProcessor], this)
  this.registerInputProcessor(new DragProcessor(app))



  val xCircle = createCircle(biggestMesh.getWidthXY(TransformSpace.GLOBAL)*3f/5f)
  rotationX.map( x => {xCircle.setDegrees(x);xCircle.create()} )

  val yCircle = createCircle(biggestMesh.getWidthXY(TransformSpace.GLOBAL)*3f/5f+4f)
  rotationY.map( y => {yCircle.setDegrees(y);yCircle.create()} )

  val zCircle = createCircle(biggestMesh.getWidthXY(TransformSpace.GLOBAL)*3f/5f+8f)
  rotationZ.map( z => {zCircle.setDegrees(z);zCircle.create()})


  // METHODS -----

  /**
   * Override center method. The biggest mesh is the center
   * @return Vector3D The center as 3D vector
   */
  override def getCenterPointGlobal: Vector3D = {
    biggestMesh.getCenterPointGlobal
  }
  override def setPositionGlobal(newPosition: Vector3D) {
    translateGlobal(newPosition.getSubtracted(position))
    xCircle.setPositionGlobal(newPosition)
    yCircle.setPositionGlobal(newPosition)
    zCircle.setPositionGlobal(newPosition)
  }

  val X_AXIS = 1
  val Y_AXIS = 2
  val Z_AXIS = 3
  val NONE = 0
  var rotationAxis = NONE

  // stuff for rotator

  def reset() = {
    //this.scaleGlobal(minimumScaleFactor,minimumScaleFactor,minimumScaleFactor, position)
    //this.scaleFactor() = 1.0f
    //this.scale(1.0f)

    rotationX() = 0f
    rotationY() = 0f
    rotationZ() = 0f

    //this.rotateXGlobal(position, -this.globalRotation().degreeX)
    //this.rotateYGlobal(position, -this.globalRotation().degreeX)
    //this.rotateZGlobal(position, -this.globalRotation().degreeX)



    val r = this.globalRotation()
    val rn = Rotation(position, -r.degreeX, -r.degreeY, -r.degreeZ)
    this.globalRotation() = rn

  }

  def updateXRoation(rot : Float) = {
    rotationX.update(rot)
    rotateXGlobal(position, rot)
  }

  def updateYRoation(rot : Float) = {
    rotationY.update(rot)
    rotateYGlobal(position, rot)
  }

  def updateZRoation(rot : Float) = {
    rotationZ.update(rot)
    rotateZGlobal(position, rot)
  }

  def updateScale(s: Float) = {
    scale(s, s)
  }

  val highlightColor = Color.PURPLE;//(255,255,255,255)
  val highlightMaterialRGB=Array(0.0f,0.0f,1f,1f)
  var isHighlighted = false;


  def setHighlighted(component: Int, enable: Boolean) = {
    // highlight different components of a form,
    // main form 0 and cycles 1-3

    component match {
      case 0 => { // form
        if(enable && !isHighlighted){

          meshes.foreach{x => x.setFillColor(highlightColor)}
          isHighlighted = true

        } else if(!enable && isHighlighted) {
          isHighlighted = false

          meshes.foreach{x => x.setFillColor(defaultFill)}
        }
      }

      case 1 => {
        if(enable) {
          this.zCircle.setStrokeColor(highlightColor)
        } else {
          this.zCircle.setStrokeColor(Color.WHITE)
        }
      }

      case 2 => {
        if(enable) {
          this.yCircle.setStrokeColor(highlightColor)
        } else {
          this.yCircle.setStrokeColor(Color.WHITE)
        }
      }

      case 3 => {
        if(enable) {
          this.xCircle.setStrokeColor(highlightColor)
        } else {
          this.xCircle.setStrokeColor(Color.WHITE)
        }
      }

      case 4 => {//ignored for now, should be scaling
      }

      case _ => throw new Exception("Wrong component")
    }
  }


  override def processGestureEvent(e: MTGestureEvent) : Boolean = {
    println("GOT EVENT"+ e)

    e match {

      case e: ScaleEvent =>
        if(!scaleLocked) {
          scale(e.getScaleFactorX, e.getScaleFactorY)
        }

      case e: RotateEvent =>
        if(!zRotLocked) {

          val nextRot = rotationZ() + e.getRotationDegrees
          if(scala.math.abs(nextRot) <= maxDeg) {
            rotateZGlobal(position, e.getRotationDegrees)
            rotationZ() += e.getRotationDegrees
          }

        }
        /*
        if (e.getId == GESTURE_UPDATED) {
          zRot.degrees(e.getRotationDegrees)
        } else if (e.getId == GESTURE_ENDED) {
          zRot.run()
        }
        */

      case e: DragEvent =>
        println(e)
        if (e.getId == GESTURE_UPDATED && app.keyPressed) {
          // when a key is pressed -> emulate multi-touch via mouse actions

          app.keyCode match {
            case VK_SHIFT => // scale while holding shift-key
              if(!scaleLocked) {
                if (position.distance(e.getFrom) < position.distance(e.getTo)) {
                  scale(1.03f)
                } else {
                  scale(0.97f)
                }
              }

            case VK_CONTROL => // rotate while holding ctrl-key

              rotationAxis = Z_AXIS

              // normalize vectors
              val from = e.getFrom.getSubtracted(position).getNormalized
              val to = e.getTo.getSubtracted(position).getNormalized

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

              if(!zRotLocked) {
                rotateZGlobal(position, angle)
                rotationZ() += angle
              }

              //zRot.degrees(angle)

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
                  if(!xRotLocked) {
                    rotateXGlobal(position, -direction.getY)
                    rotationX() += -direction.getY
                  }
                  //zRot.degrees(-direction.getY)
                case Y_AXIS =>
                  if(!yRotLocked) {
                    rotateYGlobal(position, direction.getX)
                    rotationY() += direction.getX
                  }
                //zRot.degrees(direction.getX)
                case _ =>
              }
          }

        } else if(isMoveable) {
          // if no key ist pressed, make a simple translation, just as usual...
          translateGlobal(e.getTranslationVect)
          xCircle.translateGlobal(e.getTranslationVect)
          yCircle.translateGlobal(e.getTranslationVect)
          zCircle.translateGlobal(e.getTranslationVect)
        }
        /*
        if (e.getId == GESTURE_ENDED) {
          rotationAxis match {
            case X_AXIS => xRot.run()
            case Y_AXIS => yRot.run()
            case Z_AXIS => zRot.run()
            case _ =>
          }
        }
        */
        if (!app.keyPressed && rotationAxis != NONE || e.getId == GESTURE_ENDED) {
          // reset rotation axis after completed action
          rotationAxis = NONE
        }

      case e: Rotate3DEvent =>
        val firstCursorPosition = new Vector3D(e.getFirstCursor.getStartPosX, e.getFirstCursor.getStartPosY)
        val secondCursorPosition = new Vector3D(e.getSecondCursor.getStartPosX, e.getSecondCursor.getStartPosY)
        val connection = firstCursorPosition.getSubtracted(secondCursorPosition).getNormalized
        val xNormal = new Vector3D(1,0,0)
        val angle = xNormal.angleBetween(connection)*180f/Pi // to degree
        if ((315<angle || angle<45) || (135<angle && angle<225)) {

          if(!xRotLocked) {
            val nextRot = rotationY() + e.getRotationDegreesY
            if(scala.math.abs(nextRot) <= maxDeg) {
              val degrees = e.getRotationDirection*e.getRotationDegreesX
              rotateXGlobal(e.getRotationPoint, degrees)
              rotationX() += degrees
            }

          }


            /*
          if (e.getId == GESTURE_UPDATED) {
            xRot.degrees(degrees)
          }
          */
        } else {
          if(!yRotLocked) {

            val nextRot = rotationY() + e.getRotationDegreesY
            if(scala.math.abs(nextRot) <= maxDeg) {
              val degrees = e.getRotationDirection*e.getRotationDegreesY
              rotateYGlobal(e.getRotationPoint, degrees)
              rotationY() += degrees
            }

          }
            /*
          if (e.getId == GESTURE_UPDATED) {
            xRot.degrees(degrees)
          }
          */
        }
        /*
        if (e.getId == GESTURE_ENDED) {
           xRot.run()
        }
        */

      case _ =>
    }

    true
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
      max = minimumScaleFactor/scaleFactor()
      scaleFactor() = minimumScaleFactor
      makeGrey(makeItGrey = true)
    } else if (maximumScaleFactor < newScaleFactor) {
      max =  maximumScaleFactor/scaleFactor()
      scaleFactor() = maximumScaleFactor
    } else {
      scaleFactor() = newScaleFactor
      makeGrey(makeItGrey = false)
    }

    scaleGlobal(max, max, max, position)
    xCircle.scaleGlobal(max, max, max, position)
    yCircle.scaleGlobal(max, max, max, position)
    zCircle.scaleGlobal(max, max, max, position)
  }

  def makeGrey(makeItGrey: Boolean) {
/*    if (makeItGrey && !isGrey) {
      isGrey = true
      material.setAmbient(Array(0.8f,0.8f,0.8f,1f))
      material.setSpecular(Array(0.8f,0.8f,0.8f,1f))
      material.setDiffuse(Array(0.8f,0.8f,0.8f,1f))
    } else if (!makeItGrey && isGrey) {
      isGrey = false
      material.setAmbient(materialCopy.getAmbient)
      material.setSpecular(materialCopy.getSpecular)
      material.setDiffuse(materialCopy.getDiffuse)
    }*/
  }


  /**
   * Syntactic sugar
   * @return the global center of this form
   */
  def position = getCenterPointGlobal

  def createCircle(radius: Float): WrapperEllipse = {
    println("CIRCLE CREATED")
    val circle = new WrapperEllipse(app, appCenter, radius, radius)
    circle.setNoFill(true)
    circle.setStrokeColor(new MTColor(255,255,255))
    //Mutator.scene.canvas.addChild(circle)
    circle.setPositionGlobal(position)
    circle.unregisterAllInputProcessors()
    circle.removeAllGestureEventListeners()
    //globalPosition.map(z => circle.globalPosition() = Vec3d(z.x,z.y,z.z+10f))
    circle
  }

}
