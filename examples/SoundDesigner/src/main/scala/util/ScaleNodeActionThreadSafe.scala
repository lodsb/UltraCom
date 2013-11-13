package ui.util

import org.mt4j.components.MTComponent
import org.mt4j.sceneManagement.IPreDrawAction

import ui.paths._

/**
 * This class can be used to scale a node.
 * The action has to be registered with a scene to take effect the next time
 * the draw method is called on that scene.
 *
 * Adapted from class AddNodeActionThreadSafe.
 *
 */
class ScaleNodeActionThreadSafe(nodeToScale: Node, scaleFactor: Float) extends IPreDrawAction {

    def processAction() = {
        nodeToScale.setScale(scaleFactor)
    }

    def isLoop: Boolean = {
        false
    }

}

