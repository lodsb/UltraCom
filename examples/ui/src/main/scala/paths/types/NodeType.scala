package ui.paths.types

import processing.core.PGraphics
import org.mt4j.Application
import org.mt4j.input.ComponentInputProcessorSupport
import org.mt4j.input.GestureEventSupport

import ui._
import ui.paths._


object NodeType {
  val Radius = Ui.width/120
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
      //node.getInputListeners.foreach(listener => println(listener))
      //node.getInputListeners.foreach(listener => node.removeInputListener(listener))
      //val processorSupport = new ComponentInputProcessorSupport(app, node)
      //val gestureSupport = new GestureEventSupport(app, node)
      //node.addInputListener(processorSupport)
      this.setupInteractionImpl(app, node)
      //processorSupport.getInputProcessors.foreach(processor => println(processor))
    }
    
    protected def setupInteractionImpl(app: Application, node: Node)

    /**
    * Draws a node.
    */
    def drawComponent(g: PGraphics, node: Node)
    
    /**
    * Returns the radius of a node.
    */
    def radius = {
      NodeType.Radius
    }
    
    /**
    * Returns the maximum distance from a node of this type which is still considered vicinity.
    */    
    def vicinity: Float
}
