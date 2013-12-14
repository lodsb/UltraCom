package org.mt4j.components.visibleComponents.widgets

import org.mt4j.MTApplication
import org.mt4j.components.visibleComponents.shapes.{MTRectangle, MTRoundRectangle}
import org.mt4j.components.{MTComponent, TransformSpace, StateChangeEvent, StateChangeListener}
import org.lodsb.reakt.property.Property
import org.lodsb.reakt.async.VarA
import org.mt4j.types.Vec3d

/**
 * Created by lodsb on 12/9/13.
 */
class Group(app: MTApplication, minWidth: Float=10, minHeight: Float=10, vertical: Boolean = true) extends MTRectangle(app, 0f, 0f, minWidth, minHeight)
with StateChangeListener {

  // todo justification & properties
  val xPadding = new VarA[Float](10)
  val yPadding = new VarA[Float](10)

  this.addStateChangeListener(org.mt4j.components.StateChange.CHILD_ADDED, this)
  this.addStateChangeListener(org.mt4j.components.StateChange.CHILD_REMOVED, this)

  def updateContents() = {
    this.updateBounds()
    this.layoutChildren()
  }

  def stateChanged(evt: StateChangeEvent): Unit = {
    updateContents();
  }

  private def updateBounds() = {
    val xoff = xPadding()
    val yoff = yPadding()

    val children = this.getChildren

    var currentMaxWidth = minWidth
    var currentMaxHeight = minHeight

    var currentHeight = minHeight
    var currentWidth = minHeight

    children.foreach {
      child =>
        val cbounds = child.getBounds

        val cwidth = cbounds.getWidthXY(TransformSpace.RELATIVE_TO_PARENT)
        val cheight = cbounds.getHeightXY(TransformSpace.RELATIVE_TO_PARENT)

        currentMaxWidth = scala.math.max(currentMaxWidth, cwidth)
        currentMaxHeight = scala.math.max(currentMaxHeight, cheight)
        currentHeight = currentHeight + cheight + yoff
        currentWidth = currentWidth + cwidth + xoff

    }

    if (vertical) {
      this.width := currentMaxWidth + 2 * xoff
      this.height:= currentHeight + 2 * yoff
    } else {
      this.width := currentWidth + 2 * xoff
      this.height:= currentMaxHeight + 2 * yoff
    }

  }

  private def layoutChildren() = {
    val children = this.getChildren

    var currentPosition = Vec3d(0, 0, 10)

    var yInc = Vec3d(0, yPadding(), 0)
    var xInc = Vec3d(xPadding(), 0, 0)
    val justify = Vec3d((this.width()/2f).toFloat,0f,0f,0f)

    if (vertical) {
      children.foreach {
        child =>
          child.setPickable(false)

          val cbounds = child.getBounds
          val cheight = Vec3d(0, cbounds.getHeightXY(TransformSpace.RELATIVE_TO_PARENT))

          currentPosition = currentPosition.getAdded(yInc).getAdded(cheight)
          child.relativePositionToParent() = currentPosition

          //val centerpos = child.getCenterPointRelativeToParent
          //val xpos = Vec3d(cbounds.getWidthXY(TransformSpace.RELATIVE_TO_PARENT),0,0)
          // todo proper justification, not only centering
          child.relativePositionToParent() = currentPosition.getAdded(justify)
      }
    } else {
      children.foreach {
        child =>
          child.setPickable(false)

          val cbounds = child.getBounds
          val cwidth = Vec3d(cbounds.getWidthXY(TransformSpace.RELATIVE_TO_PARENT), 0)

          currentPosition = currentPosition.getAdded(xInc).getAdded(cwidth)
          child.relativePositionToParent() = currentPosition

          val justify = (this.height())
          child.relativePositionToParent() = currentPosition.getAdded(Vec3d(0f, justify.toFloat + yInc.getY, 0f))
      }
    }
  }

  /*
  override def addChild(component: MTComponent) = {
    super.addChild(component)
    component.setPickable(false)
    this.layoutChildren()
    this.updateBounds()
  }

  override def removeChild(component: MTComponent) = {
    super.removeChild(component)
    component.setPickable(true)
  } */


}
