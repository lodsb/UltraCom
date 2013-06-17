package main.scala.TreeQuencer

import collection.mutable
import collection.mutable.ArrayBuffer

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  29.04.13 :: 15:59
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


class NodeTreeElement[NodeType <: NodeTreeElement[NodeType]] extends NodeSet[NodeType] {

  // ---------- Set part START ---------- //

  /**
   * This function adds a node to the children from this
   * @param node the node to be added
   * @return
   */
  override def +=(node: NodeType): NodeTreeElement.this.type = {
    // only do something, if node isn't already within the Set
    if (!this.contains(node)){

      val oldAncestor = node.ancestor

      // remove node from the children of its old ancestor
      if(oldAncestor != null) {
        oldAncestor -= node
      }

      // add node to children from this
      super.+=(node)

      // set ancestor pointer within node to new ancestor=this
      try { node.ancestor = this.asInstanceOf[NodeType] }
      catch { case e: ClassCastException => {} } // happen because of globalNodeSet.. stupid stuff
    }

    this
  }

  // ---------- Set part END ---------- //


  // checks if node is a child somewhere in the subtree from this
  def hasChild(node: NodeType): Boolean = {
    allChildren.foreach(child => {
      if (child eq node) {
        return true
      }
    })
    false
  }

  /**
   * Checks if this or any of its children are within Metronome()
   * This would mean, that there is a running beat signal within this subtree
   * @return Boolean
   */
  def containsRunningSignal(): Boolean = {
    firstNodeInTree.allChildren.map(child => {
      if (NodeMetronome.contains(child.asInstanceOf[Node])) {
        return true
      }
    })
    false
  }

  /**
   * Returns a NodeSet[NodeType], that contains this node, all its children and their children to etc.
   * @return NodeSet[NodeType] with the children
   */
  def allChildren: NodeSet[NodeType] = {
    val childrenSet = new NodeSet[NodeType]()
    childrenSet += this.asInstanceOf[NodeType]
    foreach(child => childrenSet ++= child.allChildren)
    childrenSet
  }

  /**
   * Returns a NodeSet[NodeType], that contains only the children from node within the first hierarchy
   * @return NodeSet[NodeType] with the children
   */
  def children: NodeSet[NodeType] = {
    this
  }


  // ancestor: the ancestor of this
  private var _ancestor: NodeType = null.asInstanceOf[NodeType]
  def ancestor = _ancestor
  def ancestor_=(newAncestor: NodeType) {
    _ancestor = newAncestor
  }


  def firstNodeInTree: NodeType = {
    if (ancestor == null || ancestor.isSourceNode) {
      this.asInstanceOf[NodeType]
    } else {
      ancestor.firstNodeInTree
    }
  }
  def isLastInTree: Boolean = {
    treeLevel == treeDepth
  }
  def treeLevel: Int = {
    if (ancestor.isSourceNode) {
      1
    } else {
      1 + ancestor.treeLevel
    }
  }

  /**
   * Get the depth of the whole tree, in which this node actually is
   * @param upwards recursion parameter. Gets dissolved through treeDepth (see downwards)
   * @return Int The depth as number
   */
  def treeDepth(upwards: Boolean): Int = {
    if(!upwards) { // go downwards
      if (!isEmpty) {
        var depth = 0
        var tmp = 0
        foreach(child => { // search for the biggest tree depth
          tmp = child.treeDepth(upwards = false)
          if (depth < tmp) {
            depth = tmp
          }
        })
        depth + 1
      } else {
        1
      }
    } else { // go upwards
      if (ancestor.isSourceNode) { // at the beginning: start going downwards and return a nice value!
        treeDepth(upwards = false)
      } else {
        ancestor.treeDepth(upwards = true) // go further upwards
      }
    }
  }
  def treeDepth: Int = {
    treeDepth(upwards = true)
  }

  def isSourceNode: Boolean = {
    this.eq(SourceNode)
  }
}