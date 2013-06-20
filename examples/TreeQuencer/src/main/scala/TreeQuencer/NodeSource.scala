package main.scala.TreeQuencer

import java.io.File
import org.mt4j.util.math.Vector3D
import collection.mutable.ArrayBuffer
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.input.inputProcessors.MTGestureEvent._
import org.mt4j.types.Vec3d

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  07.06.13 :: 23:09
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 * 
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 * 
 *  ^     ^
 *   ^   ^
 *   (o o)
 *  {  |  }                  (Wong)
 *     "
 * 
 * Don't eat the pills!
 */

/**
 * classes for creating the nodes within sequence game
 */
object NodeSource {
  val sourcesList = new Array[NodeSource](FileImporter.formFiles.size)
  def buildSources {
    var x = 100
    for (i <- 0 to FileImporter.formFiles.size-1) {
      sourcesList(i) = new NodeSource(i,Vec3d(x,90))
      x += 110
    }
  }
}

class NodeSource(val nodeType: Int, val pos: Vector3D) {

  createNewNode

  def createNewNode {
    new Node {
      form = Import.form(FileImporter.formFiles(nodeType))
      synthesizer = Import.synthesizer(this,FileImporter.synthiFiles(nodeType))
      form.setPositionGlobal(pos)
      form.addGestureListener(classOf[DragProcessor], new IGestureEventListener {
        var madeOne = false
        def processGestureEvent(e: MTGestureEvent): Boolean = {
          if (e.getId == GESTURE_ENDED && !madeOne) {
            createNewNode
            madeOne = true
          }
          false
        }
      })
    }
  }
}


