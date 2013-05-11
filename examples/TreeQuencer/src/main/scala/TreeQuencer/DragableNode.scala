package TreeQuencer

import org.mt4j.util.math.Vertex
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragEvent, DragProcessor}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DProcessor
import org.mt4j.input.gestureAction.Rotate3DAction
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.{RotateEvent, RotateProcessor}
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.{ScaleEvent, ScaleProcessor}
import org.mt4j.components.MTComponent
import org.mt4j.util.camera.IFrustum

class DragableNode extends NodeTreeElement[DragableNode] with IGestureEventListener {

  // all nodes are globally stored here, this also
  app.globalNodeSet += this.asInstanceOf[Node]

  // The line to the ancestor node
  var lineToAncestor: AnimatedLine = null

  override def ancestor_=(newAncestor: DragableNode) {
    super.ancestor_=(newAncestor)
    if(lineToAncestor != null) {
      lineToAncestor.update()
    }
  }

  // form: if circle, square or whatever. has a touch listener.
  private var _form: MTComponent = null
  def form: MTComponent = _form
  def form_=(newForm: MTComponent) {
    _form = newForm
    app.scene.canvas.addChild(form)

    // remove all drag event handlers. we want only our own
    form.unregisterAllInputProcessors()
    form.removeAllGestureEventListeners()

    // don't bother the SourceNode. It's not movable/dragable
    if (isSourceNode) return

    // instantiate the connection line to the ancestor, since this.form now exists
    lineToAncestor = new AnimatedLine(this)

    // make the form movable
    form.registerInputProcessor(new Rotate3DProcessor(app, form))
    form.addGestureListener(classOf[Rotate3DProcessor], new Rotate3DAction(app, form))
    form.registerInputProcessor(new RotateProcessor(app))
    form.addGestureListener(classOf[RotateProcessor], this)
    form.registerInputProcessor(new ScaleProcessor(app))
    form.addGestureListener(classOf[ScaleProcessor], this)
    form.registerInputProcessor(new DragProcessor(app))
    form.addGestureListener(classOf[DragProcessor], this)
  }

  def update(childrenAlso: Boolean) {
    if (childrenAlso) {

      // children also have to be updated, bot not their own, too! -> false parameter
      foreach( child => {
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
    new Vertex(form.getCenterPointGlobal)
  }


  def getNearestPossibleAncestor: DragableNode = {
    var lowestDistance = Float.MaxValue
    var nearestNode: DragableNode = null
    var distance = Float.MaxValue
    app.globalNodeSet.foreach((node) => {
      //don't connect to own children!
      if(!this.hasChild(node)) {
        distance = position.distance(node.position)
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
  def removeFromField() {
    if (app.scene.canvas.containsChild(form)) {
      removeFromField(firstOne = true)
    }
  }

  private def removeFromField(firstOne: Boolean) {

    var oldAncestor = null.asInstanceOf[DragableNode]
    if (ancestor != null) {
      oldAncestor = ancestor
    }

    // first remove all visible MTComponents from canvas
    app.scene.canvas().removeChild(form)
    app.scene.canvas().removeChild(lineToAncestor.movingCircle)
    app.scene.canvas().removeChild(lineToAncestor)

    // remove other pointers to this
    app.globalNodeSet -= this.asInstanceOf[Node]
    ancestor -= this
    NodeMetronome -= this.asInstanceOf[Node]

    // remove children (need to do this from a copy via clone())
    copy().foreach( child => {
      child.removeFromField(firstOne = false)
    })

    if (firstOne && oldAncestor != null) {
      // if subtree of ancestor contains running signal
      // incubate new signal to father's subtree

      if (ancestor != null && !oldAncestor.firstNodeInTree.containsRunningSignal) {
        NodeMetronome() += oldAncestor.firstNodeInTree.asInstanceOf[Node]
      }
    }
  }

  def isWithinField: Boolean = {
    val position = app.scene.getSceneCam.getFrustum.isSphereInFrustum(this.position,60)
    position match {
      case IFrustum.INSIDE => true
      case IFrustum.INTERSECT => false
      case IFrustum.OUTSIDE => false
      case _ => false
    }
  }

  def processGestureEvent(ge: MTGestureEvent): Boolean = {
    ge.getId match {
      case MTGestureEvent.GESTURE_UPDATED => {
        if (isWithinField) {
          // redraw line and update ancestor
          update(childrenAlso = true)
        } else if (app.scene.canvas.containsChild(form)) {
          // if dragged beyound to the edge of the field => remove from field entirely
          removeFromField()
        }
      }
      case MTGestureEvent.GESTURE_ENDED => {
        // if SourceNode() isn't occupied by another node
        // add new RandomNode() to field center
        if(!SourceNode.isOccupied) {
          SourceNode += NewRandomNode()
        }
      }
      case _ =>
    }

    if(ge.isInstanceOf[ScaleEvent]) {
      val se = ge.asInstanceOf[ScaleEvent]
      val max = List(se.getScaleFactorX,se.getScaleFactorY).max
      form.scaleGlobal(max, max, max, form.getCenterPointGlobal)
    } else if(ge.isInstanceOf[RotateEvent]) {
      val re = ge.asInstanceOf[RotateEvent]
      form.rotateZGlobal(form.getCenterPointGlobal, re.getRotationDegrees)
    } else if(ge.isInstanceOf[DragEvent]) {
      val de = ge.asInstanceOf[DragEvent]
      form.translateGlobal(de.getTranslationVect)
    }

    false
  }

  override def +=(node: DragableNode): DragableNode.this.type = {
    val oldAncestor = node.ancestor
    // only do something, if node isn't already within the Set
    if (!this.contains(node)){

      // if a subtree gets added to SourceNode(),
      // which doesn't contain a running beat signal,
      // then the first node of this subtree has to be added to Metronome()
      //if (!node.containsRunningSignal) {
      //  NodeMetronome() += node.asInstanceOf[Node]
      //}

    }

    super.+=(node)

    if (oldAncestor != null && !oldAncestor.firstNodeInTree.containsRunningSignal) {
      NodeMetronome() += node.ancestor.firstNodeInTree.asInstanceOf[Node]
    }

    if (!node.firstNodeInTree.containsRunningSignal) {
      NodeMetronome() += node.firstNodeInTree.asInstanceOf[Node]
    }

    this

  }

}