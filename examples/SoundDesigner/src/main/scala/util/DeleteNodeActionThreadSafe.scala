package ui.util

import org.mt4j.components.MTComponent
import org.mt4j.sceneManagement.IPreDrawAction

/**
 * This class can be used to delete a component from another one from a
 * different thread than the main drawing thread.
 * The action has to be registered with a scene to take effect the next time
 * the draw method is called on that scene.
 *
 * Adapted from class AddNodeActionThreadSafe.
 *
 */
class DeleteNodeActionThreadSafe(objectToDelete: MTComponent) extends IPreDrawAction {

    def processAction() = {
        objectToDelete.destroy
    }

    def isLoop: Boolean = {
        false
    }

}

