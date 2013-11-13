package ui.util

import org.mt4j.components.MTComponent
import org.mt4j.sceneManagement.IPreDrawAction

import ui._

/**
 *
 */
class RemoveChildrenActionThreadSafe(node: MTComponent) extends IPreDrawAction {

    def processAction() = {
        node.removeAllChildren()
    }

    def isLoop: Boolean = {
        false
    }

}

