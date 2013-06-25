package ui.paths

import org.mt4j.Application
import org.mt4j.util.math.Vector3D
import org.mt4j.types.{Vec3d}

import processing.core.PGraphics

import scala.actors._

import ui.paths.types._
import ui.properties._
import ui.properties.types._
import ui.events._
import ui.audio._


object ManipulableNode {
  
  def apply(app: Application, center: (Float, Float)): Node = {
      new ManipulableNode(app, Vec3d(center._1, center._2))
  }
  
  def apply(app: Application, center: Vector3D): Node = {
      new ManipulableNode(app, center)
  }

}

/**
* This class represents a manipulable node.
* The following manipulations are supported:
* <ul>
*   <li> Change of pitch </li>
*   <li> Change of volume  </li>
* </ul>
*/
class ManipulableNode(app: Application, center: Vector3D) extends Node(app, PlayTimbreNodeType, None, center) with Actor {
  private var exists = true
  private var isPlaying = false
  private var playbackPos = 0.0f
  this.start() //note: do not put this in an override setup method, it won't work...
  
  private var properties = Map(
    (VolumePropertyType -> SimpleVolumeProperty(this)),
    (PitchPropertyType -> SimplePitchProperty(this))
  )  

  
  def act = {          
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()
    var timeDiff = 0.0f //passed time in milliseconds

    while (this.exists) {
      receive {
        
        case event: NodeManipulationEvent => {
          this.updateProperty(event.propertyType, event.value) 
        }
        
        case event: UiEvent => { //a 'simple' ui event
                  
          if (event.name == "START_PLAYBACK") {
            lastTime = System.nanoTime() //init time
            Synthesizer ! AudioEvent(0, math.round(this.position._1), math.round(this.position._2), this.properties(PitchPropertyType)(), this.properties(VolumePropertyType)())   
            this.isPlaying = true
            this ! UiEvent("PLAY")
          }
          
          else if (event.name == "PLAY") {
            if (this.isPlaying) {
              currentTime = System.nanoTime()
              val passedTime = (currentTime - lastTime)/1000000.0f
              if (passedTime <= 1000) {
                this.playbackPos = passedTime/1000
                this ! event
              }
              else {
                this.isPlaying = false
                this.playbackPos = 0.0f
              }
            }              
          }
          
          else println("OH NO!")
        }
      }
    }
  } 
  
  def playbackPosition: Float = {
    this.playbackPos
  }
  
  /**
  * Updates the specified property.
  */
  def updateProperty(propertyIdentifier: PropertyType, value: Float) = {
    if (this.properties.contains(propertyIdentifier)) {
      val property = this.properties(propertyIdentifier)
      property.update(this.value(property, value))
    }
  }
  
  /**
  * Returns a value which can be used to update the bucket of the specified property.
  */
  private def value(property: SimpleProperty, value: Float): Float = {
    val (min, max) = property.range
    (value/property.maxWidth) * (max - min) + min
  }
 
  /**
  * Destroys this node, which also implies stopping any associated threads.
  */
  override def destroy() = {
    this.exists = false 
    super.destroy()
  }  
  
  /**
  * Draws this node.
  */
  override def drawComponent(g: PGraphics) = {
    super.drawComponent(g)
    this.properties.values.foreach(_.draw(g))
    
  }   
}
