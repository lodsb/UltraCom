package ui.paths.types

import processing.opengl.PGraphicsOpenGL
import org.mt4j.Application
import org.mt4j.input.ComponentInputProcessorSupport
import org.mt4j.input.GestureEventSupport
import org.mt4j.util.Color

import ui._
import ui.util._
import ui.paths._
import ui.usability._


object NodeType {
  protected[paths] val BackgroundColor = Color(255, 255, 255, 80)
  protected[paths] val ForegroundColor = Color(0, 0, 0, 200)
  protected[paths] val StrokeColor = Color(0, 0, 0, 100)
  protected[paths] val TapColor = Color(0, 0, 0, 40)
  val StrokeWeight = 1
  val Radius = Ui.width/95
}

/**
* This abstract class represents a node type.
* Node types are used by nodes to define their visual appearance as well as their interactive behaviour,
* and can easily be changed at runtime, thus realizing the strategy pattern.
*
*/
abstract class NodeType{  
  
    /**
    * Sets up the interaction on a node.
    * Note that all previously registered input processors and gesture listeners will be removed permanently.
    */
    def setupInteraction(app: Application, node: Node) = {
      node.unregisterAllInputProcessors()
      node.removeAllGestureEventListeners()
      this.setupInteractionImpl(app, node)
    }
    
    protected def setupInteractionImpl(app: Application, node: Node)
    
    /**
    * Returns the radius of a node.
    */
    def radius = {
      NodeType.Radius
    }
    
    /**
    * Returns the background color of a node.
    */
    def backgroundColor = {
      Color(NodeType.BackgroundColor.getR, NodeType.BackgroundColor.getG, NodeType.BackgroundColor.getB, NodeType.BackgroundColor.getA)
    }
    
    /**
    * Returns the foreground color of a node.
    */
    def foregroundColor = {
      Color(NodeType.ForegroundColor.getR, NodeType.ForegroundColor.getG, NodeType.ForegroundColor.getB, NodeType.ForegroundColor.getA)
    }
    
    /**
    * Returns the stroke color of a node.
    */
    def strokeColor = {
      Color(NodeType.StrokeColor.getR, NodeType.StrokeColor.getG, NodeType.StrokeColor.getB, NodeType.StrokeColor.getA)
    }
    
    /**
    * Returns the tap color of a node.
    */
    def tapColor = {
      Color(NodeType.TapColor.getR, NodeType.TapColor.getG, NodeType.TapColor.getB, NodeType.TapColor.getA)
    
    }
    
    /**
    * Returns the stroke weight of a node. 1 by default.
    */
    def strokeWeight = {
      NodeType.StrokeWeight
    }
    
    /**
    * Returns - as an option - the symbol of a node, or None if the node does not have a symbol (which is the default).
    */
    def symbol: Option[Symbol] = {
      None
    }
    
    /**
    * Returns the maximum distance from a node of this type which is still considered vicinity. 0 by default.
    */    
    def vicinity: Float = {
      0.0f
    }
    
    /**
    * Returns the relative size of a node. 1 by default.
    */
    def size: Float = {
      1.0f
    }
}
