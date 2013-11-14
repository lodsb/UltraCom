package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.globalProcessors.AbstractGlobalInputProcessor
import org.mt4j.input.inputProcessors.globalProcessors.RawFingerProcessor
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 

import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.inputData.MTFingerInputEvt
import org.mt4j.input.inputData.AbstractCursorInputEvt
import org.mt4j.input.inputData.InputCursor

import org.mt4j.components.interfaces.IMTComponent3D
import org.mt4j.components.ComponentImplicits._
import org.mt4j.components.MTCanvas
import org.mt4j.components.MTComponent
import org.mt4j.types.Vec3d
import org.mt4j.util.math.Vector3D

import org.lodsb.reakt.Implicits._

import ui._
import ui.paths._
import ui.tools._
import ui.paths.types._
import ui.events._
import ui.util._


object FeedforwardProcessor {
  private val MinDist = Ui.width/25
}


/**
* This class represents a global input processor which initiates visual feedforward based on user interaction.
*/
class FeedforwardProcessor(app: Application) extends AbstractGlobalInputProcessor[MTFingerInputEvt] {

  private var feedforwardMap = Map[Node, FeedforwardEvent]()
  
	def processInputEvtImpl(inputEvent: MTFingerInputEvt) {   
	  this.feedforwardMap = this.feedforwardMap.empty //reset feedforward map 
    inputEvent match {
      case cursorEvent: AbstractCursorInputEvt => {               
        this.nonVirtualNodes.foreach(node => { //evaluate feedforward for every non-virtual node
          this.nearestComponent(node, this.virtualNodes) match { //by looking at the closest virtual node
            case Some(virtualNode) => this.evaluateFeedforward(node, virtualNode) //and updating the feedforward map for all involved nodes (source, virtual, target)
            case None => {node.giveFeedforward(NodeTypeFeedforwardEvent(node.nodeType, 0.0f))} //or setting back the feedforward if there is no virtual node in proximity
          }
        })      
        this.feedforwardMap.foreach(entry => {entry._1.giveFeedforward(entry._2)}) //finally give strongest feedforward for every node
      }
      case someEvent => {
        println("I can't process this particular event: " + someEvent.toString)
      }
    }    
  }
  

  
  /**
  * Returns the currently available non-virtual nodes.
  */
  private def nonVirtualNodes: Iterable[Node] = {
    Ui.getCurrentScene.getCanvas.getChildren.filter(_ match {
      case node: Node => (node.nodeType != VirtualNodeType)
      case somethingElse => false
    }).collect({case node: Node => node})
    /*components.sortWith((c1, c2) => {
      val c1Pos = (c1.getCenterPointGlobal.getX, c1.getCenterPointGlobal.getY)
      val c2Pos = (c2.getCenterPointGlobal.getX, c2.getCenterPointGlobal.getY) 
      Vector.euclideanDistance(c1Pos, virtualNode.position) > Vector.euclideanDistance(c2Pos, virtualNode.position)
    })*/
  }
  
  
  /**
  * Returns the currently existing virtual nodes.
  */
  private def virtualNodes: Iterable[Node] = {
    CursorProcessor.virtualNodeMap.values
    /*Ui.getCurrentScene.getCanvas.getChildren.filter(_ match {
      case node: Node => (node.nodeType == VirtualNodeType)
      case somethingElse => false
    }).collect({case node: Node => node})*/
  }  
  
  
  /**
  * Returns for a given iterable collection of components the one closest to the specified component, or none if the collection is empty.
  */
  private def nearestComponent[T<: MTComponent](component: MTComponent, candidates: Iterable[T]): Option[T] = {  
    if (candidates.size > 0) {
      var closestCandidate = candidates.head
      val ccp = closestCandidate.getCenterPointGlobal()
      val p = component.getCenterPointGlobal()
      var minDist = Vector.euclideanDistance((ccp.getX, ccp.getY), (p.getX, p.getY))
      candidates.tail.foreach(candidate => {
        val cp = candidate.getCenterPointGlobal()
        val dist = Vector.euclideanDistance((cp.getX, cp.getY), (p.getX, p.getY))
        if (minDist > dist){ //compare its distance to the current min distance
          closestCandidate = candidate
          minDist = dist //and update if necessary
        }
      })	  
      Some(closestCandidate)
    }
    else None
  }
  
 
  
  private def evaluateFeedforward(targetNode: Node, virtualNode: Node) = {
    val virtualConnection = CursorProcessor.virtualConnectionMap(virtualNode)
    val sourceNode = virtualConnection.firstNode //the source node is always the start node of the virtual connection
    val feedforwardValue = this.feedforwardValue(targetNode, virtualNode.position) //(virtualNode, targetNode)
    
    val connectsToAnchorNode =                sourceNode.nodeType == AnchorNodeType                 || targetNode.nodeType == AnchorNodeType
    //val connectsToManipulableNode =           sourceNode.nodeType == ManipulableNodeType            || targetNode.nodeType == ManipulableNodeType
    val connectsToControlNode =               sourceNode.nodeType == ControlNodeType                || targetNode.nodeType == ControlNodeType
    val connectsToTimeNode =                  sourceNode.nodeType == TimeNodeType                   || targetNode.nodeType == TimeNodeType 
    val connectsToTimeConnectionNode =        sourceNode.nodeType == TimeConnectionNodeType         || targetNode.nodeType == TimeConnectionNodeType    
    //val connectsToDeleteManipulableNode =     sourceNode.nodeType == DeleteManipulableNodeType      || targetNode.nodeType == DeleteManipulableNodeType            
    
    if (connectsToAnchorNode || connectsToControlNode || connectsToTimeConnectionNode) {//|| connectsToDeleteManipulableNode) {
      if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))
    }
    else if (connectsToTimeNode) { //has to be placed before manipulable nodes... 
      this.processTimeConnection(sourceNode, targetNode, virtualNode, feedforwardValue)
    }   
    /*else if (connectsToManipulableNode) {
      if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))
    }*/
    else {
      this.processRegularConnection(sourceNode, targetNode, virtualNode, feedforwardValue)
    } 
  }    
 
 
  private def processTimeConnection(sourceNode: Node, targetNode: Node, virtualNode: Node, feedforwardValue: Float) = {
    (sourceNode, targetNode) match {
      case (timeNode: TimeNode, node: ManipulableNode) => {
        this.processTimeNodeToManipulableNodeConnection(timeNode, node, virtualNode, feedforwardValue)
      }
      case (node: ManipulableNode, timeNode: TimeNode) => {
        this.processTimeNodeToManipulableNodeConnection(timeNode, node, virtualNode, feedforwardValue)        
      }
      case (timeNode: TimeNode, node: Node) => {
        this.processTimeNodeToNodeConnection(timeNode, node, virtualNode, feedforwardValue)
      }
      case (node: Node, timeNode: TimeNode) => {
        this.processTimeNodeToNodeConnection(timeNode, node, virtualNode, feedforwardValue)
      }
      case (firstNode: Node, secondNode: Node) => {
        if (!this.feedforwardMap.contains(firstNode) || this.feedforwardMap(firstNode).value < feedforwardValue) this.feedforwardMap += ((firstNode, NodeTypeFeedforwardEvent(PlayNodeType, feedforwardValue)))
        if (!this.feedforwardMap.contains(secondNode) || this.feedforwardMap(secondNode).value < feedforwardValue) this.feedforwardMap += ((secondNode, NodeTypeFeedforwardEvent(StopNodeType, feedforwardValue)))
        if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))          
      }
    }
  }
 
  
  private def processTimeNodeToManipulableNodeConnection(timeNode: TimeNode, manipulableNode: ManipulableNode, virtualNode: Node, feedforwardValue: Float) = {
    if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))    
  }
  

  private def processTimeNodeToNodeConnection(timeNode: TimeNode, node: Node, virtualNode: Node, feedforwardValue: Float) = {
    node.nodeType match {
      case _: StartNodeType => {
        (timeNode.associatedPath, node.associatedPath) match {
          case (Some(timeNodePath), Some(nodePath)) => { //if the target node is part of a path
            if (timeNodePath != nodePath) {
              if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))               
            }
            else { //a path cannot trigger itself
              if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))               
            }
          }
          case somethingElse => {
            if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))                                 
          }
        }          
      }
      case otherNodeType => {
          if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))               
      }
    }    
  }  
  
  
  
  private def processRegularConnection(sourceNode: Node, targetNode: Node, virtualNode: Node, feedforwardValue: Float) = {
    if (sourceNode != targetNode) {
      (sourceNode.associatedPath, targetNode.associatedPath) match {
        case (None, None) => { //source and target are both isolated nodes
          if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(PlayNodeType, feedforwardValue)))
          if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(StopNodeType, feedforwardValue)))
          if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
        }
        case (None, Some(targetPath)) => { //source is an isolated node, target is a manipulable path
          targetNode.nodeType match {
            case _: StartNodeType => {
              if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(PlayNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
            }
            case _: EndNodeType => {
              if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(StopNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
            }
          }  
        }
        case (Some(sourcePath), None) => { //source is a manipulable path, target is an isolated node
          sourceNode.nodeType match {
            case _: StartNodeType => {
              if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(PlayNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
            }
            case _: EndNodeType => {
              if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(StopNodeType, feedforwardValue)))
              if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
            }
          }   
        }         
        case (Some(sourcePath), Some(targetPath)) => { //source and target are both manipulable paths
          if (sourcePath ne targetPath) { //do not connect start and end of same path
            (sourceNode.nodeType, targetNode.nodeType) match {
              case (_: StartNodeType, _: EndNodeType) => {
                if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
                if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
                if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
              }
              case (_: EndNodeType, _: StartNodeType) => {
                if (!this.feedforwardMap.contains(sourceNode) || this.feedforwardMap(sourceNode).value < feedforwardValue) this.feedforwardMap += ((sourceNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
                if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(AnchorNodeType, feedforwardValue)))
                if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
              }
              case somethingElse => {
                if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))
              }
            }
          }
          else {
            if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))
          }
        }
      }
    }
    else { //handle self reference sourceNode == targetNode
      /*(sourceNode.associatedPath, targetNode.associatedPath) match {
        case (None, None) => { //source and target are both isolated nodes
          if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("LEGAL ACTION", feedforwardValue)))
          if (!this.feedforwardMap.contains(targetNode) || this.feedforwardMap(targetNode).value < feedforwardValue) this.feedforwardMap += ((targetNode, NodeTypeFeedforwardEvent(ManipulableNodeType, feedforwardValue)))
        }
        case somethingElse => {
          if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))
        }
      }*/
      if (!this.feedforwardMap.contains(virtualNode) || this.feedforwardMap(virtualNode).value < feedforwardValue) this.feedforwardMap += ((virtualNode, FeedforwardEvent("ILLEGAL ACTION", feedforwardValue)))
    }    
  }
  
  
  /**
  * Returns a feedforward value between 0 and 1, with 0 implying no feedforward at all and 1 the strongest possible feedforward,
  * which is elicited if there is no distance between the two points given.
  */
  private def feedforwardValue(firstPosition: (Float, Float), secondPosition: (Float, Float)) = {
    import FeedforwardProcessor._
    val distance = Vector.euclideanDistance(firstPosition, secondPosition)
    if (distance <= MinDist) (MinDist - distance)/MinDist else 0
  }
  
  /**
  * Returns a feedforward value between 0 and 1, with 0 implying no feedforward at all and 1 the strongest possible feedforward,
  * which is elicited if the specified point is inside the given node.
  */
  private def feedforwardValue(node: Node, point: (Float, Float)) = {
    import FeedforwardProcessor._
    val distance = math.max(0, Vector.euclideanDistance(node.position, point) - node.radius * node.size)
    if (distance <= MinDist) (MinDist - distance)/MinDist else 0
  }  
  
}
