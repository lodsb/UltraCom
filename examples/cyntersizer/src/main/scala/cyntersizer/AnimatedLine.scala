package cyntersizer

import org.mt4j.components.visibleComponents.shapes.{Line, MTEllipse, MTLine}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import scala.actors.Actor
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
  //val line = new MTLine(app, node.ancestor.position, node.position)
  val line = Line()
  line.startPosition <~ node.ancestor.form.globalPosition
  line.endPosition <~ node.form.globalPosition

  line.unregisterAllInputProcessors()
  line.removeAllGestureEventListeners()
  update()
  app.scene.canvas.addChild(line)

  val animationActor = new Actor() {
    var duration = Metronome().bpmInMillisecs
    var startTime = 0l
    var movingCircle = new MTEllipse(app, node.ancestor.position, 5f, 5f)
    movingCircle.setFillColor(new MTColor(255,255,255))
    movingCircle.setVisible(false)
    app.scene.canvas.addChild(movingCircle)
    def circlePosition = node.ancestor.position.getAdded(connectionLine.getScaled(expiredTimeFactor))
    def connectionLine = node.position.getSubtracted(node.ancestor.position)
    def expiredTime =  System.currentTimeMillis() - startTime
    def expiredTimeFactor =  expiredTime/duration.toFloat

    def act() {
      startTime = System.currentTimeMillis()
      movingCircle.setVisible(true)
      while (expiredTime < duration) {
        movingCircle.setPositionGlobal(circlePosition)
      }
      movingCircle.setVisible(false)
    }
  }

  def animate() {
    // the dot should run from the beginning to the end within milliSecs
    if (animationActor.getState == Actor.State.Terminated) {
      animationActor.restart()
    } else {
      animationActor.start()
    }
  }

  // for the silver line
  def update() {/*
    if (node.ancestor != null) {
      line.setVertices(Array(node.ancestor.position, node.position))
      line.setVisible(true)
    } else {
      line.setVisible(false)
    }
  */}
}