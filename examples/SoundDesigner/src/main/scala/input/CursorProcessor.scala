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
import ui.properties.types._


object CursorProcessor {
  protected[input] var virtualNodeMap = Map[InputCursor, Node]()
  protected[input] var virtualConnectionMap = Map[Node, SimpleConnection]()
  
  def isCursorInUse(cursor: InputCursor) = {
    this.virtualNodeMap.contains(cursor)
  }
  
  def isCursorInUse(cursorId: Long) = {
    this.virtualNodeMap.exists(_._1.getId == cursorId)
  }  
  
}

/**
* This class represents a global input processor for connecting nodes.
* Nodes can be interconnected by moving one's finger in the vicinity of a node and then dragging the aquired handle on another node.
* Note, however, that only certain connections are allowed, namely:
* <ul>
*   <li> EndNodeType to StartNodeType and vice versa</li>
*   <li> IsolatedNodeType to StartNodeType and vice versa </li>
*   <li> IsolatedNodeType to EndNodeType and vice versa </li>
*   <li> IsolatedNodeType to IsolatedNodeType and vice versa </li>
*   <li> TimeNodeType to StartNodeType and vice versa </li>
* </ul>
* Most importantly, this implies that anchor nodes and control nodes cannot interactively be connected to other nodes.
*/
class CursorProcessor(app: Application) extends AbstractGlobalInputProcessor[MTFingerInputEvt] {
  
	def processInputEvtImpl(inputEvent: MTFingerInputEvt) = { //implementation on the basis of what CursorTracer does
	  if (inputEvent.getTarget == Ui.getCurrentScene.getCanvas) { //only process event if the user is not interacting with another component, i.e. only the canvas is touched
      inputEvent match {
        case cursorEvent: AbstractCursorInputEvt => {
          if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED || cursorEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED) { //DETECTED is officially deprecated and should read STARTED but local MT4J code does not correspond with online API, that is, there is no INPUT_STARTED         
            this.processMoveCursor(cursorEvent)  
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
            this.processReleaseCursor(cursorEvent)
          }
        }
        case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
        }
      }
    }
  }
  
  /**
  * Processes the movement of a cursor. This may entail creating a new virtual node and/or updating a virtual node's position.
  */
  private def processMoveCursor(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor
		val position = Vec3d(cursorEvent.getScreenX, cursorEvent.getScreenY) 
    val virtualNodeEntry = CursorProcessor.virtualNodeMap.get(inputCursor) //lookup cursor for already existing virtual node
    virtualNodeEntry match {
      case Some(virtualNode) => { //if there is one, update position
        //virtualNode.globalPosition := position
        Ui.getCurrentScene.registerPreDrawAction(new RepositionNodeActionThreadSafe(virtualNode, position))
      }
      case None => { //if there is none, it might need to be created
        val nearbyNodes = Ui.nodes.filter(node => node.distance(position) > 0 && node.isNearby(position)) //&& !node.isInstanceOf[ManipulableNode]) //get all nodes for which the coordinate (x,y) is in their vicinity, but not inside the component
        if (nearbyNodes.size > 0) {
          val nearestNode = nearbyNodes.minBy(_.distance(position)) //do not minBy on empty set
          if (!CursorProcessor.virtualConnectionMap.values.exists(_.firstNode == nearestNode)) { //and make sure the nearest node is not already interacted upon; note that virtual nodes are always the end node, not the start node
            println("new virtual node")
            val virtualNode = this.aquireVirtualNode(cursorEvent)
            this.aquireVirtualConnection(nearestNode, virtualNode) //aquire a virtual node and subsequently a virtual connection from the nearest node to the virtual node
          }    
        }
      }         
    }
  }

  
  /**
  * Processes the release of a cursor. This entails releasing the associated virtual connection and may lead to new connections.
  */
  private def processReleaseCursor(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
    val virtualNodeEntry = CursorProcessor.virtualNodeMap.get(inputCursor) //lookup cursor for virtual node
    virtualNodeEntry match {
      case Some(virtualNode) => {       
        this.processTarget(cursorEvent, virtualNode) /* evaluate if a target was hit by the virtual node handle */
        this.releaseVirtualNode(cursorEvent) /* then release the virtual node */
        this.releaseVirtualConnection(virtualNode) /* and the virtual connection */
      }   
      case None => {} //interactions unrelated to connecting
    }
  }
  
  private def processTarget(cursorEvent: AbstractCursorInputEvt, virtualNode: Node){
    val (x,y) = (cursorEvent.getScreenX, cursorEvent.getScreenY) 
    Ui.getCurrentScene.getCanvas.getComponentAt(x,y) match { //get target component; this works because a virtual node is NOT PICKABLE and thus ignored when asking for the component at position (x,y)
      case node: Node => { //if the target is a node
        this.processNodeTarget(node, virtualNode)
      }
      case tool: Tool => {
        println("tool hit")
        val virtualConnection = CursorProcessor.virtualConnectionMap(virtualNode)
        val sourceNode = virtualConnection.firstNode //the source node is always the start node of the virtual connection
        sourceNode.giveFeedback(FeedbackEvent("ILLEGAL ACTION"))
        tool.giveFeedback(FeedbackEvent("ILLEGAL ACTION"))
        virtualNode.giveFeedback(FeedbackEvent("ILLEGAL ACTION"))
      }
      case canvas: MTCanvas => {
        println("canvas hit")   
      }
      case somethingElse => {
        println("?") 
      }
    } 
  }
  
  
  private def processNodeTarget(targetNode: Node, virtualNode: Node) = {
    val virtualConnection = CursorProcessor.virtualConnectionMap(virtualNode)
    val sourceNode = virtualConnection.firstNode //the source node is always the start node of the virtual connection
    val connectsToAnchorNode =                  sourceNode.nodeType == AnchorNodeType                 || targetNode.nodeType == AnchorNodeType
    //val connectsToManipulableNode =             sourceNode.nodeType == ManipulableNodeType            || targetNode.nodeType == ManipulableNodeType
    val connectsToControlNode =                 sourceNode.nodeType == ControlNodeType                || targetNode.nodeType == ControlNodeType
    val connectsToTimeNode =                    sourceNode.nodeType == TimeNodeType                   || targetNode.nodeType == TimeNodeType      
    //val connectsToDeleteManipulableNodeType =   sourceNode.nodeType == DeleteManipulableNodeType      || targetNode.nodeType == DeleteManipulableNodeType  
    
    
    if (connectsToAnchorNode) { //we don't allow for branching of paths
      println("branching is not allowed")
      sourceNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
      targetNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
    }
    else if (connectsToControlNode) { //we don't allow for connections to control nodes
      println("connection to control node not allowed")
      sourceNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
      targetNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
    }   
    else if (connectsToTimeNode) { //this HAS to be placed before connectsToManipulableNode
      this.processTimeConnection(sourceNode, targetNode)
    }    
    /*else if (connectsToManipulableNode) { //we don't allow for connections to manipulable nodes with the exception of time nodes
      println("connection to manipulable node not allowed")
      sourceNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
      targetNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
    }*/
    /*else if (connectsToDeleteManipulableNodeType) {
      println("connection to delete manipulable node not allowed")
      sourceNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
      targetNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))      
    }*/
    else {
      this.processRegularConnection(sourceNode, targetNode)
    }     
  }
  
  
  
  private def processTimeConnection(sourceNode: Node, targetNode: Node) = {
    (sourceNode, targetNode) match {
      case (timeNode: TimeNode, node: ManipulableNode) => {
        this.processTimeNodeToManipulableNodeConnection(timeNode, node)
      }
      case (node: ManipulableNode, timeNode: TimeNode) => {
        this.processTimeNodeToManipulableNodeConnection(timeNode, node)        
      }
      case (timeNode: TimeNode, node: Node) => {
        this.processTimeNodeToNodeConnection(timeNode, node)
      }
      case (node: Node, timeNode: TimeNode) => {
        this.processTimeNodeToNodeConnection(timeNode, node)
      }
      case (firstNode: Node, secondNode: Node) => {
        firstNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
        secondNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))             
      }
    }
  }
  

  
  private def processTimeNodeToManipulableNodeConnection(timeNode: TimeNode, manipulableNode: ManipulableNode) = {
    val timeConnection = TimeConnection(Ui, timeNode, manipulableNode)
    timeNode.associatedPath.foreach(_ ! TimeConnectionAddEvent(timeConnection))
    manipulableNode ! TimeConnectionAddEvent(timeConnection)    
  }
  
  
  private def processTimeNodeToNodeConnection(timeNode: TimeNode, node: Node) = {
    node.nodeType match {
      case _: StartNodeType => {
        (timeNode.associatedPath, node.associatedPath) match {
          case (Some(timeNodePath), Some(nodePath)) => { //if the target node is part of a path
            if (timeNodePath != nodePath) {
              val timeConnection = TimeConnection(Ui, timeNode, node)
              nodePath ! TimeConnectionAddEvent(timeConnection)
              timeNodePath ! TimeConnectionAddEvent(timeConnection)
            }
            else { //a path cannot trigger itself
              println("a path cannot not trigger itself")
              timeNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
              node.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))              
            }
          }
          case somethingElse => {
            timeNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
            node.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))                   
          }
        }          
      }
      case otherNodeType => {
        timeNode.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))
        node.giveFeedback(FeedbackEvent("ILLEGAL NODE INVOLVEMENT"))               
      }
    }    
  }
  
  
  private def processRegularConnection(sourceNode: Node, targetNode: Node) = {
    if (sourceNode != targetNode) {
      (sourceNode.associatedPath, targetNode.associatedPath) match {
        case (None, None) => { //source and target are both isolated nodes
          println("(None, None)")
          Ui += Path(Ui, ManipulableBezierConnection.apply, sourceNode, targetNode)
        }
        case (None, Some(targetPath)) => { //source is an isolated node, target is a manipulable path
          println("(None, Some(targetPath))")
          targetNode.nodeType match {
            case _: StartNodeType => targetPath ! NodePrependEvent(sourceNode)//sourceNode +=: targetPath
            case _: EndNodeType => targetPath ! NodeAppendEvent(sourceNode) //targetPath += sourceNode 
          }  
          println(targetPath)
        }
        case (Some(sourcePath), None) => { //source is a manipulable path, target is an isolated node
          println("(Some(sourcePath), None)")
          sourceNode.nodeType match {
            case _: StartNodeType => sourcePath ! NodePrependEvent(targetNode) //targetNode +=: sourcePath
            case _: EndNodeType => sourcePath ! NodeAppendEvent(targetNode) //sourcePath += targetNode 
          }   
          println(sourcePath)
        }         
        case (Some(sourcePath), Some(targetPath)) => { //source and target are both manipulable paths
          println("(Some(sourcePath), Some(targetPath))")
          if (sourcePath ne targetPath) { //do not connect start and end of same path
            (sourceNode.nodeType, targetNode.nodeType) match {
              case (_: StartNodeType, _: EndNodeType) => {
                targetPath ! PathAppendEvent(sourcePath) //targetPath ++= sourcePath
              }
              case (_: EndNodeType, _: StartNodeType) => {
                sourcePath ! PathAppendEvent(targetPath) //sourcePath ++= targetPath
              }
              case somethingElse => {
                println("connecting two starts or two ends is not allowed") //either two ends or two starts are being connected, which is not allowed 
                sourceNode.giveFeedback(FeedbackEvent("ILLEGAL CLASHING PATH DIRECTIONS"))
                targetNode.giveFeedback(FeedbackEvent("ILLEGAL CLASHING PATH DIRECTIONS"))
              }
            }
          }
          else {
            println("connecting the start of a path with its end is not allowed") 
            sourceNode.giveFeedback(FeedbackEvent("ILLEGAL CYCLE"))
            targetNode.giveFeedback(FeedbackEvent("ILLEGAL CYCLE"))
          }
        }
      }
    }
    else { //handle self reference sourceNode == targetNode
      /*(sourceNode.associatedPath, targetNode.associatedPath) match {
        case (None, None) => { //source and target are both isolated nodes
          println("(None, None)")
          val position = sourceNode.position
          Ui -= sourceNode
          Ui += ManipulableNode(Ui, position)
        }
        case somethingElse => {
          println("a self-reference can only be applied to an isolated node")
          targetNode.giveFeedback(FeedbackEvent("ILLEGAL SELF-REFERENCE"))
        }
      }*/
      targetNode.giveFeedback(FeedbackEvent("ILLEGAL SELF-REFERENCE"))
    }
  }
  
  /**
  * Aquires a new virtual node at the cursor's current position and adds it to the canvas as well as the virtual node map.
  */
  private def aquireVirtualNode(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
		val position = Vec3d(cursorEvent.getScreenX, cursorEvent.getScreenY)    
    val virtualNode = Node(Ui, VirtualNodeType, None, position)
    CursorProcessor.virtualNodeMap += (inputCursor -> virtualNode)
    Ui += virtualNode
    Ui.getCurrentScene.registerPreDrawAction(new RepositionNodeActionThreadSafe(virtualNode, position))
    //virtualNode.globalPosition := position
    virtualNode    
  }

  /**
  * Aquires a new virtual connection between the specified nodes and adds it to the canvas as well as the virtual connection map.
  */
  private def aquireVirtualConnection(nearestNode: Node, virtualNode: Node) = {
    val virtualConnection = SimpleConnection(Ui, nearestNode, virtualNode)
    CursorProcessor.virtualConnectionMap += (virtualNode -> virtualConnection)
    Ui.getCurrentScene.getCanvas += virtualConnection //do not use Ui += ...
    virtualConnection
  }
  
  /**
  * Releases the virtual node associated with the input cursor obtainable from the specified cursorEvent by removing it from the virtual node map and the canvas.
  */
  private def releaseVirtualNode(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
    val virtualNode = CursorProcessor.virtualNodeMap(inputCursor)
    CursorProcessor.virtualNodeMap -= inputCursor
    Ui -= virtualNode
  }

  /**
  * Releases the virtual connection associated with the specified virtual node by removing it from the virtual connection map and the canvas.
  */  
  private def releaseVirtualConnection(virtualNode: Node) = {
    val virtualConnection = CursorProcessor.virtualConnectionMap(virtualNode)
    CursorProcessor.virtualConnectionMap -= virtualNode
    Ui -= virtualConnection
  }
  
}
