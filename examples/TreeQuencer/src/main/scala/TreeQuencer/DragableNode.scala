package main.scala.TreeQuencer

import org.mt4j.util.math.Vertex
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.MTGestureEvent._
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DProcessor
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor
import org.mt4j.util.camera.IFrustum._
import org.mt4j.util.camera.Frustum
import org.mt4j.util.SessionLogger
import org.mt4j.util.SessionLogger.SessionEvent

class DragableNode extends NodeTreeElement[DragableNode] with IGestureEventListener {

  // The line to the ancestor node
  var lineToAncestor: AnimatedLine = null

  override def ancestor_=(newAncestor: DragableNode) {
    super.ancestor_=(newAncestor)
    if(lineToAncestor != null) {
      lineToAncestor.update()
    }
  }

  // form: if circle, square or whatever. has a touch listener.
  private var _form: NodeForm = null
  def form: NodeForm = _form
  def form_=(newForm: NodeForm) {
    _form = newForm
    app.scene.canvas.addChild(form)

    // remove all drag event handlers. We want only our own
    form.unregisterAllInputProcessors()
    form.removeAllGestureEventListeners()

    // don't bother the SourceNode. It's not movable/dragable and has no connection line
    if (isSourceNode) return

    // instantiate the connection line to the ancestor, since this.form now exists
    lineToAncestor = new AnimatedLine(this)

    // SillNodes aren't movable
    if (isInstanceOf[StillNode]) return

    // make the form movable
    form.registerInputProcessor(new Rotate3DProcessor(app, form))
    form.addGestureListener(classOf[Rotate3DProcessor], this)
    form.registerInputProcessor(new RotateProcessor(app))
    form.addGestureListener(classOf[RotateProcessor], this)
    form.registerInputProcessor(new ScaleProcessor(app))
    form.addGestureListener(classOf[ScaleProcessor], this)
    form.registerInputProcessor(new DragProcessor(app))
    form.addGestureListener(classOf[DragProcessor], this)
    form.makeGrey(makeItGrey = true)
  }

  def update(childrenAlso: Boolean) {
    if (childrenAlso) {

      // children also have to be updated, but not their own, too! -> false parameter
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

  def position = new Vertex(form.position)


  def getNearestPossibleAncestor: DragableNode = {
    var lowestDistance = Float.MaxValue
    var nearestNode = null.asInstanceOf[main.scala.TreeQuencer.Node]
    var distance = Float.MaxValue
    app.globalNodeSet.foreach( node => {
      //don't connect to own children!
      if((!node.isNearToCenter || app.game == app.SEQUENCE_GAME) && !this.hasChild(node) || node.isSourceNode) {
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
    // Logging
    if(app.loggingEnabled) {
      SessionLogger.log("Removed Node", SessionEvent.Event, _form, null, null)
    }
  }

  private def removeFromField(firstOne: Boolean) {

    var oldAncestor = null.asInstanceOf[DragableNode]
    if (ancestor != null) {
      oldAncestor = ancestor
    }

    // first remove all visible MTComponents from canvas
    app.scene.canvas().removeChild(form)
    app.scene.canvas().removeChild(form.xCircle)
    app.scene.canvas().removeChild(form.yCircle)
    app.scene.canvas().removeChild(form.zCircle)
    lineToAncestor.remove

    // remove other pointers to this
    app.globalNodeSet -= this.asInstanceOf[Node]
    ancestor -= this
    Metronome().removeNode(this.asInstanceOf[Node])
    this.asInstanceOf[Node].synthesizer.free

    // remove children (need to do this from a copy via clone())
    copy.foreach( child => {
      child.removeFromField(firstOne = false)
    })

    if (app.game == app.RANDOM_GAME && firstOne && oldAncestor != null) {
      // if subtree of ancestor contains running signal
      // incubate new signal to father's subtree

      if (ancestor != null && !oldAncestor.firstNodeInTree.containsRunningSignal) {
        NodeMetronome += oldAncestor.firstNodeInTree.asInstanceOf[Node]
      }
    }
  }

  def isWithinField: Boolean = {
    if (!app.scene.canvas.containsChild(form)) {
      return false
    }
    val borderWidth = 150
    val zero = borderWidth
    val width = app.width-borderWidth
    val height = app.height-borderWidth

    val posX = this.position.getX
    val posY = this.position.getY

    if (posX>zero && posX<width && posY>zero && posY<height) {
      return true
    }
    return false

    val position = app.scene.getSceneCam.getFrustum.isPointInFrustum(this.position)
    position match {
      case INSIDE => true
      case INTERSECT => false
      case OUTSIDE => false
      case _ => false
    }
  }

  def processGestureEvent(e: MTGestureEvent): Boolean = {
    e.getId match {
      case GESTURE_UPDATED => {
        if (isWithinField) {
          // redraw line and update ancestor
          update(childrenAlso = true)
        } else if (app.scene.canvas.containsChild(form)) {
          // if dragged beyound to the edge of the field => remove from field entirely
          removeFromField()
        }
        if (isNearToCenter) {
          form.makeGrey(makeItGrey = true)
        } else {
          form.makeGrey(makeItGrey = false)
        }
      }
      case GESTURE_ENDED => {
        // if SourceNode() isn't occupied by another node
        // add new RandomNode() to field center
        if(app.game != app.SEQUENCE_GAME && !SourceNode.isOccupied) {
          SourceNode += RandomNode()
        }
      }
      case _ =>
    }

    form.handleEvent(e)

    false
  }

  override def +=(node: DragableNode): DragableNode.this.type = {
    val oldAncestor = node.ancestor

    super.+=(node)

    if (app.game == app.RANDOM_GAME) {

      if (oldAncestor != null && !oldAncestor.firstNodeInTree.containsRunningSignal) {
        NodeMetronome += node.ancestor.firstNodeInTree.asInstanceOf[Node]
      }

      if (!node.firstNodeInTree.containsRunningSignal) {
        NodeMetronome += node.firstNodeInTree.asInstanceOf[Node]
      }

    }

    if(app.loggingEnabled) {
      SessionLogger.log("Added node to set", SessionEvent.Event, _form, null, node.form)
    }

    this

  }

  override def -=(node: DragableNode): DragableNode.this.type = {
    super.-=(node)

    if(app.loggingEnabled) {
      SessionLogger.log("Removed node from set", SessionEvent.Event, _form, null, node.form)
    }

    this
  }


  def isNearToCenter: Boolean = {
    position.distance2D(app.center) < app.innerCircleRadius
  }


}
