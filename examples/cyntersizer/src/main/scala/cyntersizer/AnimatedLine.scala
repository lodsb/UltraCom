package cyntersizer

import org.mt4j.components.visibleComponents.shapes.MTLine
import org.mt4j.util.math.Vertex
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import processing.core.PApplet

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
  val startVertex: Vertex = new Vertex() //it starts from the ancestor!!!
  val endVertex: Vertex = new Vertex()
  val vertices = Array(startVertex, endVertex)
  val line = new MTLine(app.asInstanceOf[PApplet], startVertex, endVertex)
  line.unregisterAllInputProcessors()
  line.removeAllGestureEventListeners(classOf[DragProcessor])

  update()

  app.scene.canvas.addChild(line)

  def animate(milliSecs : Int) {
    // the dot should run from the beginning to the end within milliSecs
  }

  // for the silver line
  def update() {
    if (node.ancestor != null) {
      startVertex.setValues(node.ancestor.position())
      endVertex.setValues(node.position())
      line.setVertices(vertices)
      line.setVisible(true)
    } else {
      line.setVisible(false)
    }
  }
}