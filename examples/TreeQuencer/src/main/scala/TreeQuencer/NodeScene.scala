package main.scala.TreeQuencer

import org.mt4j.{Scene, Application}
import org.mt4j.util.math.Vector3D
import org.mt4j.components.MTLight
import javax.media.opengl.GL
import org.mt4j.output.audio.AudioServer
import org.mt4j.util.MTColor
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  17.03.13 :: 17:11
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 * ^     ^
 *  ^   ^
 *  (o o)
 * {  |  }                  (Wong)
 *    "
 *
 * Don't eat the pills!
 */
object app extends Application {

  // some global values
  def apply = this
  def center = new Vector3D(width/2f, height/2f)
  var scene: NodeScene = null
  var light: MTLight = null
  val globalNodeSet = new NodeSet[Node]() // all nodes are globally stored here

  // start scene
  def main(args: Array[String]) {
    execute(false)
  }

  override def startUp() {
    addScene(new NodeScene())
  }

  override protected def handleKeyEvent(e: KeyEvent) {
    if (keyPressed && keyCode == VK_ESCAPE) {
      Runtime.getRuntime.halt(0)
    }
    super.handleKeyEvent(e)
  }

}

class NodeScene() extends Scene(app,"TreeQuencer") {
  AudioServer.start(true)
  app.scene = this
  app.scene.setClearColor(new MTColor(255,255,255))
  app.light = new MTLight(app, GL.GL_LIGHT3, new Vector3D(0,0,200))//app.center.getAdded(app.scene.getSceneCam.getPosition))
  MTLight.enableLightningAndAmbient(app, 150, 150, 150, 255)
  showTracer(show = true)

  SourceNode() += NewRandomNode()
  NodeMetronome().start()

}
