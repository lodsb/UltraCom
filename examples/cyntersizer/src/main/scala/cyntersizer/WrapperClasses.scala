package cyntersizer

import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.util.math.Vertex
import org.mt4j.Scene
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragEvent, DragProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import scala.collection.mutable.ArrayBuffer
import collection.mutable




class NodeScene extends Scene(app,"Cyntersizer") {
  app.scene = this
}


trait NodeSet[NodeType <: NodeSet[NodeType]] extends mutable.Set[NodeType] {

  // ---------- Set part START ---------- //
  private val children = ArrayBuffer[NodeType]()

  /**
   * This function adds a node to the children from this
   * @param node the node to be added
   * @return
   */
  override def +=(node: NodeType): NodeSet.this.type = {
    // only do something, if node isn't already within the Set
    if (!this.contains(node)){

      val oldAncestor = node.ancestor

      // remove node from the children of its old ancestor
      if(oldAncestor != null) {
        oldAncestor -= node
      }

      // add node to children from this
      children += node

      // set ancestor pointer within node to new ancestor=this
      node.ancestor = this.asInstanceOf[NodeType]

      // if a subtree gets added to SourceNode(),
      // which doesn't contain a running beat signal,
      // then the first node of this subtree has to be added to Metronome()
      if (!node.containsRunningSignal) {//println("!node.containsRunningSignal")
        Metronome() += node.asInstanceOf[Node]
      }
      if (oldAncestor != null && !oldAncestor.containsRunningSignal) {//println("!oldAncestor.containsRunningSignal")
        Metronome() += oldAncestor.firstNodeInTree.asInstanceOf[Node] //TODO: maybe 1 beat latency?!? a bit pause!
      }
    }

    this
  }


  override def -=(node: NodeType): NodeSet.this.type = {
    // nothing special, just an ordinary removal from the Set
    var i = 0
    for (child <- children) {
      if(child eq node) {
        children.remove(i)
        return this
      }
      i = i + 1
    }
    this
  }

  override def contains(node: NodeType): Boolean = {
    for (child <- children) {
      if(child eq node) {
        return true
      }
    }
    false
  }

  override def iterator: Iterator[NodeType] = {
    children.iterator
  }
  // ---------- Set part END ---------- //


  // checks if node is a child somewhere in the subtree from this
  def hasChild(node: NodeType): Boolean = {
    completeChildList.foreach(child => {
      if (child eq node) {
        return true
      }
    })
    false
  }

  /**
   * Checks if this or any of its children are within Metronome()
   * This would mean, that there is a running beat signal within this subtree
   * @return Boolean
   */
  def containsRunningSignal(): Boolean = {
    firstNodeInTree.completeChildList.map(child => {
      if (Metronome().contains(child.asInstanceOf[Node])) {
        return true
      }
    })
    false
  }

  /**
   * Returns a list, that contains this node and all children
   * @return
   */
  def completeChildList: mutable.ArrayBuffer[NodeType] = {
    val list = new ArrayBuffer[NodeType]()
    list += this.asInstanceOf[NodeType]
    foreach(child => list ++= child.completeChildList)
    list
  }


  // ancestor: the ancestor of this
  private var _ancestor: NodeType = null.asInstanceOf[NodeType]
  def ancestor = _ancestor
  def ancestor_=(newAncestor: NodeType) {
    _ancestor = newAncestor
  }


  def firstNodeInTree: NodeType = {
    if (ancestor == null || ancestor.isSourceNode) {
      this.asInstanceOf[NodeType]
    } else {
      ancestor.firstNodeInTree
    }
  }
  def isLastInTree: Boolean = { //println("isLastInTree. treeLevel="+treeLevel+"treeDepth="+treeDepth)
    treeLevel == treeDepth
  }
  def treeLevel: Int = {
    if (ancestor.isSourceNode) {
      1
    } else {
      1 + ancestor.treeLevel
    }
  }

  /**
   * Get the depth of the whole tree, in which this node actually is
   * @param upwards recursion parameter. Gets dissolved through treeDepth (see downwards)
   * @return Int The depth as number
   */
  def treeDepth(upwards: Boolean): Int = {
    if(!upwards) { // go downwards
      if (!isEmpty) {
        var depth = 0
        var tmp = 0
        foreach(child => { // search for the biggest tree depth
          tmp = child.treeDepth(upwards = false)
          if (depth < tmp) {
            depth = tmp
          }
        })
        depth + 1
      } else {
        1
      }
    } else { // go upwards
      if (ancestor.isSourceNode) { // at the beginning: start going downwards and return a nice value!
        treeDepth(upwards = false)
      } else {
        ancestor.treeDepth(upwards = true) // go further upwards
      }
    }
  }
  def treeDepth: Int = treeDepth(upwards = true)

  def isSourceNode:Boolean = {
    this.eq(ancestor) || ancestor == null
  }
}

trait DragableNode extends NodeSet[DragableNode] {

  var radius: Float = 10f
  var rotationAngle: Float = 0f

  //all nodes are globally stored here
  app.globalNodeSet += this

  // ancestor. Standard: SourceNode()
  ancestor = SourceNode()

  // The line to the ancestor node
  var lineToAncestor: AnimatedLine = null

  // form: if circle, square or whatever. has a touch listener.
  private var _form: MTPolygon = null
  def form: MTPolygon = _form
  def form_=(newForm: MTPolygon) {
    _form = newForm
    app.scene.canvas.addChild(form)

    // remove all drag event handlers. we want only our own
    form.getInputProcessors.foreach (dp => {
      if (dp.isInstanceOf[DragProcessor]) {
        form.unregisterInputProcessor(dp)
      }
    })
    form.removeAllGestureEventListeners(classOf[DragProcessor])

    // don't bother the SourceNode. It's not movable/dragable
    if (this.isSourceNode) return

    // instantiate the connection line to the ancestor, since this.form now exists
    lineToAncestor = new AnimatedLine(this)

    // make the form movable
    form.registerInputProcessor(new DragProcessor(app))
    form.addGestureListener(classOf[DragProcessor], new IGestureEventListener() {
      def processGestureEvent(ge: MTGestureEvent): Boolean = {
        val de = ge.asInstanceOf[DragEvent]
        de.getTarget.translateGlobal(de.getTranslationVect); //Moves the component
        de.getId match {
          case MTGestureEvent.GESTURE_UPDATED => {
            if (isWithinField) {
              // redraw line and update ancestor
              update(childrenAlso = true)
            } else {
              // if dragged beyound to the edge of the field => remove from field entirely
              removeFromField()
            }
          }
          case MTGestureEvent.GESTURE_ENDED => {
            // if SourceNode() isn't occupied by another node
            // add new RandomNode() to field center
            if(!SourceNode.isOccupied) {
              SourceNode() += NewRandomNode()
            }
          }
          case _ => {}
        }
        false
      }
    })
  }

  def update(childrenAlso: Boolean) {
    if (childrenAlso) {

      // children also have to be updated, bot not their own, too! -> false parameter
      foreach((child: DragableNode) => {
        child.update(childrenAlso = false)
      })

      // set nearest node to new ancestor if necessary
      var nearestNode = getNearestPossibleAncestor
      if (nearestNode.ne(null)) {
        nearestNode += this
      }
    }

    // redraw line to ancestor
    lineToAncestor.update()
  }

  def position: Vertex = {
    val pos = form.getCenterPointGlobal
    new Vertex(pos.getX, pos.getY, -1f)
  }


  def getNearestPossibleAncestor: DragableNode = {
    var lowestDistance = Float.MaxValue
    var nearestNode: DragableNode = null
    var distance = Float.MaxValue
    app.globalNodeSet.foreach((node: DragableNode) => {
      //don't connect to own children!
      if(!this.hasChild(node)) {
        distance = this.position.distance(node.position)
        if(distance < lowestDistance){
          lowestDistance = distance
          nearestNode = node
        }
      }
    })
    if (nearestNode ne ancestor) {
      return nearestNode
    }
    null
  }

  /**
   * Removes this node from the field.
   * Children also get deleted.
   * Should happen, whenn dragged to the edge of the field.
   */
  def removeFromField():Unit = removeFromField(firstOne = true)

  private def removeFromField(firstOne:Boolean) {
    // first remove all visible MTComponents from canvas
    app.scene.canvas().removeChild(form)
    app.scene.canvas().removeChild(lineToAncestor.line)
    app.scene.canvas().removeChild(lineToAncestor.movingCircle)

    // remove other pointers to this
    app.globalNodeSet -= this
    ancestor -= this

    // remove children (need to do this from a copy via clone())
    clone().foreach(child => child.removeFromField(false))

    // inject a new signal into the tree if necessary
    if (firstOne) {
      if(containsRunningSignal() || Metronome().contains(this.asInstanceOf[Node])) {//println("wannatoaddsomething")
        //Metronome() -= this.asInstanceOf[Node]
        //Metronome() += firstNodeInTree.asInstanceOf[Node]
      }
    }
  }

  def isWithinField:Boolean = {

    if(
      radius < position.getX &&
      position.getX < app.width - radius &&
      radius < position.getY &&
      position.getY < app.height - radius
    ) {
      return true
    }
    false
  }

}