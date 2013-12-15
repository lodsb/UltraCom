package ui

import org.mt4j.{Scene, Application}
import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.MT4jSettings
import org.mt4j.components.TransformSpace

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex

import processing.opengl.PGraphicsOpenGL
import processing.core.PImage

import ui.menus.main._
import processing.opengl.PGraphicsOpenGL

object NodeSpace {
  
  val BorderWeight = 1.0f
  val BorderColor = new MTColor(0,20,80,50)
  
  def apply(app: Application) = {
    new NodeSpace(app)
  }
  
}

/**
* This class visually represents the space where nodes can be dragged around and/or connected.
*/
class NodeSpace(app: Application) extends MTRectangle(app, Menu.Space, Menu.Space, app.width - 2 * Menu.Space, app.height - 2 * Menu.Space) {
  this.setupRepresentation()
  this.setupInteraction()
 
  private def setupRepresentation() = {
    this.setStrokeWeight(NodeSpace.BorderWeight)
    this.setStrokeColor(NodeSpace.BorderColor)
    //this.setNoStroke(true)
    
    val visOption = Ui.audioInterface.timbreSpace.visualRepresentation
    visOption.foreach(visualization => this.setTexture(this.processImage(visualization)))
  }
  
  private def setupInteraction() = {
    this.unregisterAllInputProcessors() //no default rotate, scale & drag processors
    this.removeAllGestureEventListeners() //no default listeners as well
    this.setPickable(false) //we don't want to get this component if we pick
  }
  
  
  /**
  * Returns whether the specified vector is inside this timbre space.
  */
  def contains(vector: Vector3D) = {
   vector.getX >= Menu.Space && vector.getX <= app.width - Menu.Space && vector.getY >= Menu.Space && vector.getY <= app.height - Menu.Space
  }
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
  }

  
  private def processImage(image: PImage) = {
    //val (min, max) = this.luminanceRange(image)
    //println("max is " + max + ", min is " + min)
    
    val resizedImage = this.resizeImage(image)

    /*
    for (x <- 0 until resizedImage.width) {
      for (y <- 0 until resizedImage.height) {
        val argb = resizedImage.get(x,y)
        val color = this.argbToColor(argb)
        //val mean = ((color.getR + color.getG + color.getB)/3.0f).toInt
        if (x%6 > 1 && x%6 < 5 && y%6 > 1 && y%6 < 5) {
          resizedImage.set(x, y, this.colorToArgb(new MTColor(color.getR, color.getG, color.getB)))
        }
        else resizedImage.set(x, y, this.colorToArgb(new MTColor(255, 255, 255)))
      }
    }  */
    resizedImage
  }
  
  /**
  * Resizes the specified image by either stretching or contracting it.
  *
  * Be aware that information may be lost or redundantly added in the process.
  */
  private def resizeImage(image: PImage) = {
    val width = this.getWidthXY(TransformSpace.GLOBAL)
    val height = this.getHeightXY(TransformSpace.GLOBAL)
    val resizedImage = new PImage(width.toInt, height.toInt)
    for (x <- 0 until resizedImage.width) {
      for (y <- 0 until resizedImage.height) {
        val argb = image.get((x/width * image.width).toInt, (y/height * image.height).toInt)
        resizedImage.set(x, y, argb)
      }
    }
    resizedImage
  }
  
  
  private def luminanceRange(image: PImage) = {
    var maxLuminance = 0.0
    var minLuminance = 255.0
    for (x <- 0 until image.width) {
      for (y <- 0 until image.height) {
        val argb = image.get(x,y)
        val color = this.argbToColor(argb)
        maxLuminance = math.max(this.luminanceFromRGB(color.getR, color.getG, color.getB), maxLuminance)
        minLuminance = math.min(this.luminanceFromRGB(color.getR, color.getG, color.getB), minLuminance)
      }
    }    
    (minLuminance.toFloat, maxLuminance.toFloat)
  }
  
  
  /**
  * Returns the perceived luminance for the given rgb color.
  */
  private def luminanceFromRGB(r: Float, g: Float, b: Float) = {
    (0.299*r + 0.587*g + 0.114*b)
  }
  
  private def argbToColor(argb: Int): MTColor = {
    val a = (argb >> 24) & 0xFF
    val r = (argb >> 16) & 0xFF
    val g = (argb >> 8) & 0xFF
    val b = argb & 0xFF
    new MTColor(r,g,b,a)
  }
  
  
  private def colorToArgb(color: MTColor): Int = {
    (color.getA.toInt << 24) | (color.getR.toInt << 16) | (color.getG.toInt << 8) | color.getB.toInt
  }  
  
  
}
