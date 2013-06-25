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

/**
* This class represents a global input processor for connecting nodes.
* Nodes can be interconnected by moving one's finger in the vicinity of a node and then dragging the aquired handle on another node.
* Note, however, that only certain connections are allowed, namely:
* <ul>
*   <li> EndNodeType to StartNodeType and vice versa</li>
*   <li> IsolatedNodeType to StartNodeType and vice versa </li>
*   <li> IsolatedNodeType to EndNodeType and vice versa </li>
*   <li> IsolatedNodeType to IsolatedNodeType and vice versa </li>
* </ul>
* Most importantly, this implies that anchor nodes and control nodes cannot interactively be connected to other nodes.
*/
class NodeConnectionProcessor(app: Application) extends AbstractGlobalInputProcessor[MTFingerInputEvt] {

  private var virtualNodeMap = Map[InputCursor, Node]()
  private var virtualConnectionMap = Map[Node, SimpleConnection]()
  private val FeedforwardMinDist = 100
 

	def processInputEvtImpl(inputEvent: MTFingerInputEvt) { //implementation on the basis of what CursorTracer does
	  if (inputEvent.getTarget == Ui.getCurrentScene.getCanvas){ //only process event if the user is not interacting with another component, i.e. only the canvas is touched
      inputEvent match {
        case cursorEvent: AbstractCursorInputEvt => {
          if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_DETECTED || cursorEvent.getId == AbstractCursorInputEvt.INPUT_UPDATED) { //DETECTED is officially deprecated and should read STARTED but local MT4J code does not correspond with online API, that is, there is no INPUT_STARTED         
            val nodeOption = this.processMoveHandle(cursorEvent)  
            nodeOption match {
              case Some(virtualNode) => {
                this.nearestComponent(virtualNode) match {
                  case Some(component) => {
                    component match {
                      case tool: Tool => {
                        println("tool")
                        //virtualNode.
                      }
                      case node: Node => {
                        println("node")
                        //virtualNode.
                      }
                      case somethingElse => {
                        println("somethingElse")
                      }
                    }
                  }
                  case None => {}
                }
              }
              case None => {}          
            }
            //TODO feedforward here also
          }
          else if (cursorEvent.getId == AbstractCursorInputEvt.INPUT_ENDED) {
            this.processReleaseHandle(cursorEvent)
          }
          true
        }
        case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
          false
        }
      }
    }
  }
  
  
  private def processMoveHandle(cursorEvent: AbstractCursorInputEvt): Option[Node] = { //TODO add feedforward for wrong/right action
    val inputCursor = cursorEvent.getCursor
		val position = Vec3d(cursorEvent.getScreenX, cursorEvent.getScreenY) 
    val virtualNodeEntry = this.virtualNodeMap.get(inputCursor) //lookup cursor for already existing virtual node
    val nodeOption = (virtualNodeEntry match {
      case Some(virtualNode) => { //if there is one, update position
        virtualNode.setPositionGlobal(position)
        virtualNodeEntry
      }
      case None => { //if there is none, it might need to be created
        val nearbyNodes = Ui.nodes.filter(node => node.distance(position) > 0 && node.isNearby(position)) //get all nodes for which the coordinate (x,y) is in their vicinity, but not inside the component
        if (nearbyNodes.size > 0) { //do not minBy on empty set (!)
          val nearestNode = nearbyNodes.minBy(_.distance(position)) //get nearest node
          if (!this.virtualConnectionMap.values.exists(_.firstNode == nearestNode)) { //and make sure the nearest node is not already interacted upon; note that virtual nodes are always the end node, not the start node
            val virtualNode = this.aquireVirtualNode(cursorEvent)
            this.aquireVirtualConnection(nearestNode, virtualNode) //aquire a virtual node and subsequently a virtual connection from the nearest node to the virtual node
            Some(virtualNode)
          }
          else None
        }
        else None
      }         
    })   
    nodeOption
  }

  
  private def processReleaseHandle(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
    val virtualNodeEntry = this.virtualNodeMap.get(inputCursor) //lookup cursor for virtual node
    virtualNodeEntry match {
      case Some(virtualNode) => {       
        this.processTarget(cursorEvent, virtualNode) /* evaluate if a target was hit by the virtual node handle */
        this.releaseVirtualNode(cursorEvent) /* then release the virtual node */
        this.releaseVirtualConnection(virtualNode) /* and the virtual connection */
      }   
      case None => {println("No virtual node for this cursor.")} //interactions unrelated to connecting
    }
  }
  
  private def processTarget(cursorEvent: AbstractCursorInputEvt, virtualNode: Node){
    val (x,y) = (cursorEvent.getScreenX, cursorEvent.getScreenY) 
    Ui.getCurrentScene.getCanvas.getComponentAt(x,y) match { //get target component; this works because a virtual node is NOT PICKABLE and thus ignored when asking for the component at position (x,y)
      case node: Node => { //if the target is a node
        this.processNodeTarget(node, virtualNode)
      }
      case tool: Tool => {
        println("tool hit")  //TODO visually show that the wrong object was selected
        //TODO: tool.alertWrongAction() or something
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
    val virtualConnection = this.virtualConnectionMap(virtualNode)
    val sourceNode = virtualConnection.firstNode //the source node is always the start node of the virtual connection
    val createsBranching = sourceNode.nodeType == AnchorNodeType || targetNode.nodeType == AnchorNodeType
    val connectsToManipulableNode = sourceNode.nodeType == PlayTimbreNodeType || targetNode.nodeType == PlayTimbreNodeType
    if (!createsBranching && !connectsToManipulableNode) { //we don't allow for branching of paths or connections to manipulable nodes
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
                  println(targetPath)
                }
                case (_: EndNodeType, _: StartNodeType) => {
                  sourcePath ! PathAppendEvent(targetPath) //sourcePath ++= targetPath
                  println(sourcePath)
                }
                case somethingElse => println("connecting two starts or two ends is not allowed") //either two ends or two starts are being connected, which is not allowed //TODO visually show that this is not possible
              }
            }
            else {
              println("connecting the start of a node with its end is not allowed") //TODO visually show that this is not possible
            }
          }
        }
      }
      else { //handle self reference sourceNode == targetNode
        (sourceNode.associatedPath, targetNode.associatedPath) match {
          case (None, None) => { //source and target are both isolated nodes
            println("(None, None)")
            val position = sourceNode.position
            Ui -= sourceNode
            Ui += ManipulableNode(Ui, position)
          }
          case somethingElse => {
            println("a self-reference can only be applied to an isolated node")
          }
        }
      }
    } 
    else {  
      println("anchor node involved or node connected to itself") //TODO visually show that this action is illegal
    }
    
  }
  
  
  /**
  * Aquires a new virtual node at the cursor's current position and adds it to the canvas as well as the virtual node map.
  */
  private def aquireVirtualNode(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
		val position = Vec3d(cursorEvent.getScreenX, cursorEvent.getScreenY)    
    val virtualNode = Node(Ui, VirtualNodeType, position)
    this.virtualNodeMap += (inputCursor -> virtualNode)
    Ui.getCurrentScene.getCanvas += virtualNode //do not use Ui += ...
    virtualNode.setPositionGlobal(position)
    virtualNode    
  }

  /**
  * Aquires a new virtual connection between the specified nodes and adds it to the canvas as well as the virtual connection map.
  */
  private def aquireVirtualConnection(nearestNode: Node, virtualNode: Node) = {
    val virtualConnection = SimpleConnection(Ui, nearestNode, virtualNode)
    this.virtualConnectionMap += (virtualNode -> virtualConnection)
    Ui.getCurrentScene.getCanvas += virtualConnection //do not use Ui += ...
    virtualConnection
  }
  
  /**
  * Releases the virtual node associated with the input cursor obtainable from the specified cursorEvent by removing it from the virtual node map and the canvas.
  */
  private def releaseVirtualNode(cursorEvent: AbstractCursorInputEvt) = {
    val inputCursor = cursorEvent.getCursor()
    val virtualNode = this.virtualNodeMap(inputCursor)
    this.virtualNodeMap -= inputCursor
    Ui -= virtualNode
  }

  /**
  * Releases the virtual connection associated with the specified virtual node by removing it from the virtual connection map and the canvas.
  */  
  private def releaseVirtualConnection(virtualNode: Node) = {
    val virtualConnection = this.virtualConnectionMap(virtualNode)
    this.virtualConnectionMap -= virtualNode
    Ui -= virtualConnection
  }
  
  
  private def nearestComponent(virtualNode: Node): Option[MTComponent] = {
    val candidates = Ui.getCurrentScene.getCanvas.getChildren.filter(_ match {
      case node: Node => {
        node.nodeType != VirtualNodeType
      } 
      case tool: Tool => {
        true
      } 
      case somethingELse => {
        false
      }
    })
      
    if (candidates.size > 0) {
      println("size = " + candidates.size)
      var closestCandidate = candidates.head
      val ccp = closestCandidate.getCenterPointGlobal()
      var minDist = Vector.euclideanDistance((ccp.getX, ccp.getY), virtualNode.position)
      candidates.tail.foreach(candidate => {
        val cp = candidate.getCenterPointGlobal()
        val dist = Vector.euclideanDistance((cp.getX, cp.getY), virtualNode.position)
        if (minDist > dist){ //compare its distance to the current min distance
          closestCandidate = candidate
          minDist = dist //and update if necessary
        }
      })	  
      Some(closestCandidate)
    }
    else None
  }
  
}
