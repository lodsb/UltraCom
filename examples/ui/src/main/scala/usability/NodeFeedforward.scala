package ui.usability

import org.mt4j.util.MTColor
import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator

import ui.paths.types._
import ui.events._
import ui._


/**
* This trait realizes visual feedforward for nodes.
*/
trait NodeFeedforward extends Feedforward {

  protected var feedforwardValue = 0.0f
  protected var feedforwardNodeType: NodeType = VirtualNodeType

  
  def setScale(scale: Float)
  
  def setColor(col: MTColor)
  
  def nodeType: NodeType
  
  def color: MTColor
	
	
	override def giveFeedforward(event: FeedforwardEvent) = {
	  this.feedforwardValue = event.value
	  event match {
		case nodeTypeEvent: NodeTypeFeedforwardEvent => {
		  if (this.feedforwardValue > 0) {
			this.feedforwardNodeType = nodeTypeEvent.nodeType
			this.setScale(this.feedforwardValue * this.feedforwardNodeType.size + (1 - this.feedforwardValue)*this.nodeType.size)
		  }
		  else {
			this.feedforwardNodeType = this.nodeType
			this.setScale(this.feedforwardNodeType.size)
		  }
		}
		case normalEvent => {
		  val color = this.nodeType.backgroundColor
		  val oldR = color.getR
		  val oldG = color.getG
		  val oldB = color.getB          
		  val oldAlpha = color.getAlpha
		  
		  if (normalEvent.name == "LEGAL ACTION") {
			val newColor = Color((1-this.feedforwardValue) * oldR, this.feedforwardValue * (255-oldG) + oldG, (1-this.feedforwardValue) * oldB, oldAlpha)
			this.setColor(newColor)           
			this.setScale(math.max(this.nodeType.size, 1.5f*this.feedforwardValue))
		  }
		  else if (normalEvent.name == "ILLEGAL ACTION") {
			val newColor = Color(this.feedforwardValue * (255-oldR) + oldR, (1-this.feedforwardValue) * oldG, (1-this.feedforwardValue) * oldB, oldAlpha)
			this.setColor(newColor)
			this.setScale(this.nodeType.size)
		  }
		}
	  }
	}  
	

}
