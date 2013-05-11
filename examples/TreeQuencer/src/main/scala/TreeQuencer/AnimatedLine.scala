package TreeQuencer

import org.mt4j.components.visibleComponents.shapes.{MTEllipse, MTLine}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.util.MTColor
import org.mt4j.util.math.{Vertex, Vector3D}

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

  unregisterAllInputProcessors()
  removeAllGestureEventListeners(classOf[DragProcessor])
  update()
  app.scene.canvas.addChild(this)

  val movingCircle = new MTEllipse(app, new Vertex(), 5f, 5f)
  movingCircle.setVisible(false)
  movingCircle.setFillColor(new MTColor(255,255,255))
  app.scene.canvas.addChild(movingCircle)

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