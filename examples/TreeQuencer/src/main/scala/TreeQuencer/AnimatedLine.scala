package main.scala.TreeQuencer

import org.mt4j.components.visibleComponents.shapes.{MTEllipse, MTLine}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.util.MTColor
import org.mt4j.util.math.{Vertex, Vector3D}
import actors.Actor
import collection.mutable

/*
 This source code is licensed as GPLv3 if not stated otherwise.
 NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.

 Last modified:  2013 - 3 - 12 :: 10 : 42
 Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)

 Made in Bavaria by tons of eager fast pixies - since 1986.

  ^     ^
   ^   ^
   (o o)
  {  |  }                  (Wong)
     "
 Don't eat the pills!
 */


class AnimatedLine(val node: DragableNode)
  extends MTLine(app, if(node.ancestor==null) new Vertex() else node.ancestor.position, node.position) {

  val movingCircles = new mutable.HashMap[Actor, MTEllipse]()

  unregisterAllInputProcessors()
  removeAllGestureEventListeners(classOf[DragProcessor])
  update()
  app.scene.canvas.addChild(this)

  setStrokeColor(new MTColor(255,255,255))


  def moveCircle(actor: Actor, newPosition: Vector3D) {
    if (movingCircles.contains(actor)) {
      movingCircles(actor).setPositionGlobal(newPosition)
    }
  }

  def remove {
    app.scene.canvas().removeChild(this)
    movingCircles.foreach( tuple => { val actor = tuple._1
      movingCircles(actor).setVisible(false)
      app.scene.canvas().removeChild(movingCircles(actor))
      movingCircles.remove(actor)
    })
  }
  def createAnimationCircle(actor: Actor) {
    if (app.scene.canvas().containsChild(this) && !movingCircles.contains(actor)) {
      movingCircles(actor) = new MTEllipse(app, new Vertex(), 5f, 5f)
      movingCircles(actor).setVisible(true)
      movingCircles(actor).setFillColor(new MTColor(255,255,255))
      app.scene.canvas.addChild(movingCircles(actor))
    }
  }
  def destructAnimationCircle(actor: Actor) {
    if (movingCircles.contains(actor)) {
      movingCircles(actor).setVisible(false)
      app.scene.canvas.removeChild(movingCircles(actor))
      movingCircles.remove(actor)
    }
  }

  /**
   * If this.node moves, the line needs a graphical update()
   */
  def update() {
    if (node.ancestor != null) {
      setVertices(Array(node.ancestor.position, node.position))
      if(!isVisible) {
        setVisible(true)
      }
    } else if(isVisible) {
      setVisible(false)
    }
  }
}