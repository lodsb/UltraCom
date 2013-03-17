package cyntersizer

import org.mt4j.components.visibleComponents.shapes.{MTPolygon, MTRectangle}
import org.mt4j.util.math.{Vector3D, Vertex}
import org.mt4j.util.MTColor
import org.mt4j.Scene
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragEvent, DragProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import scala.collection.mutable.ArrayBuffer
import collection.mutable





class TriangleForm(width: Float, color: MTColor)
  extends MTRectangle(app, new Vertex(app.center), width, width) {

  this.setFillColor(color)

}

class NodeScene extends Scene(app,"Cyntersizer") {
  app.scene = this
}


trait NodeSet[NodeType <: NodeSet[NodeType]] extends mutable.Set[NodeType] {

  // ---------- Children part START ---------- //
  private val children = ArrayBuffer[NodeType]()

  override def +=(node: NodeType): NodeSet.this.type = {
    // only do something, if node isn't already within the Set
    if (!this.contains(node)){

      // remove node from the children of its old ancestor
      if(node.ancestor != null) {
        node.ancestor -= node
      }

      // add node to children from this
      children += node

      // set ancestor pointer within node to new anestor
      node.ancestor = this.asInstanceOf[NodeType]
    }

    this
  }

  override def -=(node: NodeType): NodeSet.this.type = {
    // nothing special, just a removal from the Set
    var i = 0
    for (child <- children) {
      if(child eq node) {
        children.remove(i)
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

  // checks if node is a child somewhere in the subtree from this
  def hasChild(node: NodeType): Boolean = {
    if (this.eq(node)) {
      // node is "child" from itself.... more practicable
      return true
    }
    foreach((child: NodeType) => {
      if (child.hasChild(node)) {
        // if node is a child from the children of this -> true
        return true
      }
    })
    false
  }
  // ---------- Children part END ---------- //

  // ancestor: the ancestor of this
  private var _ancestor: NodeType = null.asInstanceOf[NodeType]
  def ancestor = _ancestor
  def ancestor_=(newAncestor: NodeType) {
    _ancestor = newAncestor
  }
}

trait DragableNode extends NodeSet[DragableNode] {

  //all nodes are stored here
  app.globalNodeSet += this

  // standard ancestor
  ancestor = SourceNode()

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
        if(de.getId == MTGestureEvent.GESTURE_UPDATED) {
          update(childrenAlso = true)
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

      // set nearest node to ancestor if necessary
      var nearestNode = getNearestPossibleAncestor
      if (nearestNode.ne(null)) {
        nearestNode += this
      }
    }

    // redraw line to ancestor
    lineToAncestor.update()
  }

  def position(): Vector3D = {
    val vec = form.getCenterPointGlobal
    vec.setZ(-1)
    vec
  }

  def isSourceNode:Boolean = {
    this.eq(ancestor) || ancestor == null
  }

  def getNearestPossibleAncestor: DragableNode = {
    var lowestDistance = Float.MaxValue
    var nearestNode: DragableNode = null
    var distance = Float.MaxValue
    app.globalNodeSet.foreach((node: DragableNode) => {
      //don't connect to own children!
      if(!this.hasChild(node)) {
        distance = this.position().distance(node.position())
        if(distance < lowestDistance){
          lowestDistance = distance
          nearestNode = node
        }
      }
    })
    nearestNode
  }

}