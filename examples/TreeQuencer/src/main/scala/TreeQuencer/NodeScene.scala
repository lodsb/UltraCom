package TreeQuencer

import org.mt4j.{Scene, Application}
import org.mt4j.util.math.Vector3D
import org.mt4j.components.MTLight
import javax.media.opengl.GL
import com.twitter.util.Eval
import java.io.File
import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.AudioServer
import org.mt4j.output.audio.AudioServer._
import de.sciss.synth._
import collection.mutable.ArrayBuffer

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
  // all nodes are globally stored here
  var globalNodeSet = new NodeSetAternative[TreeQuencer.Node]()
  var audioServerBooted = false

  // start scene
  def main(args: Array[String]) {
    execute(false)
  }

  override def startUp() {
    addScene(new NodeScene())
  }

}


class NodeScene() extends Scene(app,"Cyntersizer") {
  AudioServer.start(true)
  app.scene = this
  app.light = new MTLight(app, GL.GL_LIGHT3, app.center.getAdded(app.scene.getSceneCam.getPosition))
  MTLight.enableLightningAndAmbient(app, 150, 150, 150, 255)
  showTracer(show = true)

  SourceNode() += NewRandomNode()
  NodeMetronome().start()

}
