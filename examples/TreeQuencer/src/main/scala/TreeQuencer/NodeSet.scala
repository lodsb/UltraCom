package main.scala.TreeQuencer

import collection.mutable
import collection.mutable.ArrayBuffer

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  10.05.13 :: 13:31
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
 
 
 class NodeSet[NodeType <: AnyRef] extends mutable.Set[NodeType] {

  private val nodes = new ArrayBuffer[NodeType]()

  override def +=(node: NodeType): NodeSet.this.type = {
    if (!this.contains(node)) {
      nodes += node
    }
    this
  }

  override def -=(node: NodeType): NodeSet.this.type = {
    for(i <- 0 to nodes.size-1) {
      if(nodes(i).eq(node)) {
        nodes.remove(i)
        return this
      }
    }
    this
  }

  override def contains(node: NodeType): Boolean = {
    foreach( tmpNode => {
      if (tmpNode.eq(node)){
        return true
      }
    })
    false
  }

  override def iterator: Iterator[NodeType] = {
    nodes.iterator
  }

  def copy: NodeSet[NodeType] = {
    val copy = new NodeSet[NodeType]()
    foreach( element => {
      copy += element
    })
    copy
  }

  override def empty: NodeSet.this.type = {
    foreach( element => {
      this -= element
    })
    this
  }
}
