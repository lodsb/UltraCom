package ui.menus.main

import org.mt4j.Application
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.components.TransformSpace

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import org.mt4j.util.MTColor

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator

import scala.actors._

import ui.menus._
import ui._
import ui.util._
import ui.audio._


/**
* This object manages all menues currently visible to the users.
*/
object Menu {
  val fixedItems = 4 //number of items always displayed
  val Space = Ui.width/30.0f //width of area around the timbre space which is reserved for menu interaction
  val Padding = Space/6.0f
  val Width = (MIDIInputChannels.InputChannels + fixedItems)*(2*Button.Radius + Padding)
  val Height = Space
  val StrokeColor = new MTColor(200, 200, 200, 0)
  val Timeout = 5000 //number of milliseoncds without interaction before the menu fades out 
  val FadeInTime = 1000
  val FadeOutTime = 1500

  private var registry = Set[Menu]()
  
  def +=(menu: Menu) = {
    this.registry += menu
  }
  
  def -=(menu: Menu) = {
    this.registry -= menu
  }
  
  def apply(app: Application, center: Vector3D, rotationAngle: Float) = {
    new Menu(app, center, rotationAngle)
  }
  
  def isMenuInProximity(center: Vector3D, rotationAngle: Float) = {
    this.registry.filter(_.rotationAngle == rotationAngle).exists(menu => Vector.euclideanDistance((menu.center.getX, menu.center.getY), (center.getX, center.getY)) < Width)
  }
  
  /**
  * Returns for a specified vector and a rotation angle the ecnlosing bounds induced by surrounding menus,
  * that is, the range around the given vector before the first menu is found.
  */
  private def enclosingBounds(vec: Vector3D, rotationAngle: Float) = {
    val menus = this.registry.filter(_.rotationAngle == rotationAngle)
    
    if (rotationAngle == 90f || rotationAngle == 270f) { //left/right
      val topMenus = menus.filter(_.center.getY <= vec.getY)
      val bottomMenus = menus.filter(_.center.getY > vec.getY)      
      val min = if (topMenus.size > 0) topMenus.minBy(menu => vec.getY - menu.center.getY).center.getY + this.Width/2 else this.Space
      val max = if (bottomMenus.size > 0) bottomMenus.minBy(menu => menu.center.getY - vec.getY).center.getY - this.Width/2 else Ui.height - this.Space      
      (min, max)
    }
    else { //top/bottom
      val leftMenus = menus.filter(_.center.getX <= vec.getX)
      val rightMenus = menus.filter(_.center.getX > vec.getX)   
      val min = if (leftMenus.size > 0) leftMenus.minBy(menu => vec.getX - menu.center.getX).center.getX + this.Width/2 else this.Space
      val max = if (rightMenus.size > 0) rightMenus.minBy(menu => menu.center.getX - vec.getX).center.getX - this.Width/2 else Ui.width - this.Space      
      (min, max)
    }
   
  }
  
  /**
  * Returns - as an option - for a specified input vector a nearby position to place a new menu at, or None if the creation of a new menu is not possible at the moment.
  */
  def calculateMenuPosition(vec: Vector3D, rotationAngle: Float): Option[Vector3D] = {   
    val (min, max) = this.enclosingBounds(vec, rotationAngle)
    val desiredPos = if (rotationAngle == 90f || rotationAngle == 270f) vec.getY else vec.getX
    
    if (max - min < this.Width) {
      None //if there is not enough room, a menu cannot be created; we need at least 1*Width for a menu
    } 
    else {
      if (rotationAngle == 90f || rotationAngle == 270f) {
        val x = if (rotationAngle == 90f) this.Space/2 else Ui.width - this.Space/2
        val minDiff = vec.getY - min
        val maxDiff = max - vec.getY
        if (minDiff > this.Width/2 && maxDiff > this.Width/2) { //if the desired position is available
          Some(Vec3d(x, vec.getY)) //we only adjust the x value
        }
        else if (minDiff > maxDiff) { //if the desired position is too far to the right
          Some(Vec3d(x, max - this.Width/2)) //the new menu will be created just width/2 away from the other nearby menu
        }
        else { //maxDiff >= minDiff, desired position too far to the left
          Some(Vec3d(x, min + this.Width/2)) //the new menu will be created just width/2 away from the other nearby menu
        }
      }
      else {
        val y = if (rotationAngle == 180f) Space/2 else Ui.height - Space/2
        val minDiff = vec.getX - min
        val maxDiff = max - vec.getX
        if (minDiff > this.Width/2 && maxDiff > this.Width/2) { //if the desired position is available
          Some(Vec3d(vec.getX, y)) //we only adjust the y value
        }
        else if (minDiff > maxDiff) { //if the desired position is too far to the bottom
          Some(Vec3d(max - this.Width/2, y))
        }
        else { //maxDiff >= minDiff, desired position too far to the top
          Some(Vec3d(min + this.Width/2, y))
        }        
      }
    }
  }
  
}

/**
* This class represents the application's main menu.
* It provides means to create, load, save and play back sounds,
* and fades in and out smoothly based on user interaction (or the lack thereof).
*/
class Menu(app: Application, val center: Vector3D, val rotationAngle: Float) extends MTRectangle(app, center.getX - Menu.Width/2, center.getY - Menu.Height/2, Menu.Width, Menu.Height) with Actor {
  
  var inExistence = true
  this.setup()  
  
  private def setup() = {
    import Menu._
    
    this.unregisterAllInputProcessors()
    this.removeAllGestureEventListeners()
    
    val me = this
    this.registerInputProcessor(new TapProcessor(app))         
    this.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
        override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
              gestureEvent match {
                  case tapEvent: TapEvent => {
                      if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                        me ! "RESET_TIMER"
                      }
                      true
                  }
                  case someEvent => {
                      println("I can't process this particular event: " + someEvent.toString)
                      false
                  }
              }
        }
    })    
    
    val (cx, cy) = (center.getX, center.getY)
    this.setNoFill(true)
    this.setNoStroke(true)
    
    val baseValue = (MIDIInputChannels.InputChannels + fixedItems)
    val halfBaseValue = baseValue/2.0f
    
    this.addChild(FastForwardButton(app, this, Vec3d(cx + (baseValue - 1) * Button.Radius + halfBaseValue*Padding, cy)))
    this.addChild(PlayButton(app, this, Vec3d(cx + (baseValue - 3) * Button.Radius + (halfBaseValue - 1)*Padding, cy)))
    //this.addChild(RecordButton(app, this, Vec3d(cx + 4 * Button.Radius + 2*Padding, cy)))    
    this.addChild(StopButton(app, this, Vec3d(cx + (baseValue - 5) * Button.Radius + (halfBaseValue - 2)*Padding, cy)))
    this.addChild(RewindButton(app, this, Vec3d(cx + (baseValue - 7) * Button.Radius + (halfBaseValue - 3)*Padding, cy)))
    //this.addChild(SaveProjectButton(app, this, Vec3d(cx - 2 * Button.Radius - Padding, cy)))
    //this.addChild(LoadProjectButton(app, this, Vec3d(cx - 4 * Button.Radius - 2*Padding, cy)))
    //this.addChild(NewProjectButton(app, this, Vec3d(cx - 6 * Button.Radius - 3*Padding, cy)))
    for (index <- 0 until MIDIInputChannels.InputChannels) {
     this.addChild(InputChannelButton(app, this, Vec3d(cx - (-(baseValue - 1 - 2*fixedItems) + 2*index) * Button.Radius - (-(halfBaseValue - fixedItems)+ index)*Padding, cy), index))
    }
    
    this.rotateZ(Vec3d(center.getX, center.getY), rotationAngle, TransformSpace.GLOBAL) //then apply new rotation
    
    this.fadeInAnimation(0, FadeInTime).start() 
    this.start()
    this ! "CHECK_TIMEOUT"
  }
  
  
  def act() = {
    println("menu: starting to act!")
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()  
    var timeDiff = 0.0f
    var fadeIn = this.fadeInAnimation(0, Menu.FadeInTime)
    var fadeOut = this.fadeOutAnimation(255, Menu.FadeOutTime)
    var isFadingOut = false
    
    while (this.inExistence) {
      receive {
        case "CHECK_TIMEOUT" => {
          currentTime = System.nanoTime()
          timeDiff = (currentTime - lastTime)/1000000.0f //passed time in milliseconds
          if (timeDiff > Menu.Timeout) {
            fadeOut.restart()
            isFadingOut = true
          }
          else {
            this ! "CHECK_TIMEOUT"
            Thread.sleep(10) 
          }
        }
        case "RESET_TIMER" => {
          lastTime = System.nanoTime()
          if (isFadingOut) {
            isFadingOut = false
            fadeOut.stop()
            fadeIn = this.fadeInAnimation(fadeOut.getValue(), Menu.FadeInTime)
            fadeIn.start()
            this ! "CHECK_TIMEOUT"
          }
        }
        case "STOP_ACTING" => {
          exit()
        }
      }
    }
  }
  
  
  private def fadeInAnimation(startValue: Float, time: Float) = {
    val me = this
    val fadeInInterpolator = new MultiPurposeInterpolator(startValue, 255, time, 1.0f, 1.0f, 1) //accelerated fadein in 1,5 secs
    val animation = new Animation("FADE_IN", fadeInInterpolator, this)    
    animation.addAnimationListener(new IAnimationListener() { 
      def processAnimationEvent(ae: AnimationEvent) {
        val componentFillColor = me.getFillColor()
        val componentStrokeColor = me.getStrokeColor()
        componentFillColor.setA(ae.getValue)
        componentStrokeColor.setA(ae.getValue)
      
        me.setStrokeColor(componentStrokeColor)
        me.setFillColor(componentFillColor)
        
        me.getChildren.collect({case item: MenuItem => item}).foreach(item => {
          item.setOpacity(ae.getValue/255f)
        })
      }
    })
    animation
  }
  
  
  
  private def fadeOutAnimation(startValue: Float, time: Float) = {
    val me = this
    val fadeOutInterpolator = new MultiPurposeInterpolator(startValue, 0, time, 0.0f, 0.0f, 1) //decelerated fadeout in time milliseconds
    val animation = new Animation("FADE_OUT", fadeOutInterpolator, this)
    animation.addAnimationListener(new IAnimationListener() { 
      def processAnimationEvent(ae: AnimationEvent) {
        if(ae.getId() == AnimationEvent.ANIMATION_ENDED){ //if the animation has been played back uninterrupted
          inExistence = false
          me ! "STOP_ACTING"
          Menu -= me
          Ui -= me
          println("fadeout ended")
        }
        else {
          val componentFillColor = me.getFillColor()
          val componentStrokeColor = me.getStrokeColor()
          componentFillColor.setA(ae.getValue)
          componentStrokeColor.setA(ae.getValue)
        
          me.setStrokeColor(componentStrokeColor)
          me.setFillColor(componentFillColor)
          
          me.getChildren.collect({case item: MenuItem => item}).foreach(item => {
            item.setOpacity(ae.getValue/255f)
          })
        }
      }
    })
    animation
  }
  
  
}
