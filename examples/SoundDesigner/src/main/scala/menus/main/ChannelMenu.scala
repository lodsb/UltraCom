package ui.menus.main

import org.mt4j.Application
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle
import org.mt4j.components.TransformSpace

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}
import org.mt4j.components.visibleComponents.widgets.MTSlider

import org.mt4j.util.MTColor

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor 

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.DefaultRotateAction

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator
import org.mt4j.util.SessionLogger

import processing.opengl.PGraphicsOpenGL

import scala.actors._

import ui.menus._
import ui._
import ui.util._
import ui.audio._
import ui.input._
import processing.opengl.PGraphicsOpenGL


object ChannelMenu {
  
  final val Width = Ui.width/10f
  final val Height = Width
  final val Color = new MTColor(0,20,80,40)
  final val SliderWidth = 3*Width/5f
  final val SliderHeight = Height/7f
  final val DotWidth = Width/10f
  
  private var registry = Set[ChannelMenu]()
  
  def +=(menu: ChannelMenu) = {   
    this.registry += menu
  }
  
  def -=(menu: ChannelMenu) = {
    this.registry -= menu
  }
  
  def isMenuVisible(channelNumber: Int) = {
    this.registry.exists(_.channelNumber == channelNumber)
  }  
  
  def menuFromChannelNumber(channelNumber: Int) : Option[ChannelMenu] = {
    this.registry.find(_.channelNumber == channelNumber)
  }
  
  def apply(app: Application, center: Vector3D, channelNumber: Int) = {
      new ChannelMenu(app, center, channelNumber: Int)
  }  
  
}


class ChannelMenu(app: Application, center: Vector3D, val channelNumber: Int) 
      extends MTRoundRectangle(app, center.getX - ChannelMenu.Width/2f, center.getY - ChannelMenu.Height/2f, 0, ChannelMenu.Width, ChannelMenu.Height, ChannelMenu.Width/10f, ChannelMenu.Width/10f) {
        
    this.setFillColor(ChannelMenu.Color)
    val strokeCol = new MTColor(ChannelMenu.Color.getR, ChannelMenu.Color.getG, ChannelMenu.Color.getB, (ChannelMenu.Color.getA + 20)%255)
    this.setStrokeColor(strokeCol)
    this.setStrokeWeight(1)
    //this.setNoStroke(true)
    this.setupInteraction()
    
    private def setupInteraction() = {
      //remove defaults
      this.unregisterAllInputProcessors() //no default rotate, scale & drag processors
      this.removeAllGestureEventListeners() //no default listeners as well
                    
      //register input processors
      this.registerInputProcessor(new DragProcessor(app))
     // this.registerInputProcessor(new RotateProcessor(app))
      val tapProcessor = new TapProcessor(app)
      tapProcessor.setEnableDoubleTap(true)
      this.registerInputProcessor(tapProcessor)
      
      //add gesture listeners
      //this.addGestureListener(classOf[RotateProcessor], new DefaultRotateAction(this)) 
      this.addGestureListener(classOf[DragProcessor], new BoundedDragAction(0, 0, Ui.width, Ui.height)) 
      this.addGestureListener(classOf[TapProcessor], new ChannelMenuTapListener(this))
      
      
      val translation = ChannelMenu.Width/3f
      
      for (index <- 0 until MIDIInputChannels.Parameters) {
        val sliderPos = Vec3d(center.getX - ChannelMenu.SliderWidth/2f, center.getY - ChannelMenu.SliderHeight/2f)   
        val slider = new MTSlider(app, sliderPos.getX, sliderPos.getY, ChannelMenu.SliderWidth, ChannelMenu.SliderHeight, 0f, 1f)
        
        slider.getOuterShape.setFillColor(ChannelMenu.Color)
        slider.getOuterShape.setStrokeColor(ChannelMenu.Color)
        slider.getOuterShape.setStrokeWeight(0)
        slider.getOuterShape.setNoStroke(true)
                
        val h = (index * 1f)/MIDIInputChannels.Parameters
        val s = 1f
        val l = 0.5f
        val a = 150       
        val (r,g,b) = Functions.hslToRgb(h,s,l)
        val color = new MTColor(r,g,b,a)   

        slider.getKnob.setFillColor(color)
        slider.getKnob.setNoStroke(true)        
        
        slider.translate(Vec3d(0,translation))
        slider.rotateZ(center, 360f/MIDIInputChannels.Parameters * index)
        
        slider.value.observe({newValue => {
          SessionLogger.log("Channel parameters changed; channel: " + this.channelNumber + ", parameter: " + MIDIControlEvent.LowestNumber + index,SessionLogger.SessionEvent.Event, this, null, null)
          Ui.audioInterface ! MIDIControlEvent(this.channelNumber, MIDIControlEvent.LowestNumber + index, newValue)
          true
        }})
        
        this.addChild(slider)
      }
    }    

    
    override def drawComponent(g: PGraphicsOpenGL) = {
      super.drawComponent(g)
      this.drawSymbol(g)
    }  
  
    def drawSymbol(graphics: PGraphicsOpenGL) = {
      val center = this.getCenterPointLocal()
      val cx = center.getX()
      val cy = center.getY()  
      
      graphics.noStroke()
      val (r,g,b,a) = (ChannelMenu.Color.getR, ChannelMenu.Color.getG, ChannelMenu.Color.getB, ChannelMenu.Color.getA)
      graphics.fill(r, g, b, (a+50)%255)
  
      if (channelNumber == 0) {
        graphics.ellipse(cx, cy, ChannelMenu.DotWidth, ChannelMenu.DotWidth)
      }
      else {
        (1 to channelNumber + 1).foreach(item => {
          val (x,y) = Functions.positionOnCircle(cx, cy, 0.5f * ChannelMenu.Width/4f, 2*math.Pi.toFloat, item, channelNumber + 1)
          graphics.ellipse(x, y, ChannelMenu.DotWidth, ChannelMenu.DotWidth)
        })
      }    
      
    }     
    
  
}
