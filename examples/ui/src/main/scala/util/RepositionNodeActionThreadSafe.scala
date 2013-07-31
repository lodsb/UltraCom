package ui.util

import org.mt4j.components.MTComponent
import org.mt4j.sceneManagement.IPreDrawAction

import org.mt4j.types.Vec3d
import org.mt4j.util.math.Vector3D

import ui.paths._

/**
 * This class can be used to reposition a node.
 * The action has to be registered with a scene to take effect the next time
 * the draw method is called on that scene.
 *
 * Adapted from class AddNodeActionThreadSafe.
 *
 */
class RepositionNodeActionThreadSafe(node: Node, position: Vector3D) extends IPreDrawAction {

    def processAction() = {
        node.setPositionGlobal(position)
    }

    def isLoop: Boolean = {
        false
    }

}
