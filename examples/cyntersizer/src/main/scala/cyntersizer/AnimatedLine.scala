package cyntersizer

import org.mt4j.components.visibleComponents.shapes.{MTEllipse, MTLine}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.util.MTColor

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


class AnimatedLine(val node: DragableNode) {
  val line = new MTLine(app, node.ancestor.position, node.position)
  line.unregisterAllInputProcessors()
  line.removeAllGestureEventListeners(classOf[DragProcessor])
  update()
  app.scene.canvas.addChild(line)

  val movingCircle = new MTEllipse(app, node.ancestor.position, 5f, 5f)
  movingCircle.setVisible(false)
  movingCircle.setFillColor(new MTColor(255,255,255))
  app.scene.canvas.addChild(movingCircle)

  // for the silver line
  def update() {
    if (node.ancestor != null) {
      line.setVertices(Array(node.ancestor.position, node.position))
      if(!line.isVisible) {
        line.setVisible(true)
      }
    } else if(line.isVisible) {
      line.setVisible(false)
    }
  }
}