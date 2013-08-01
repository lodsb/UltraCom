package ui.paths

import org.mt4j.Application

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import ui._
import ui.util._
import ui.paths.types._
import ui.events._
import ui.usability._
import ui.audio._


object TimeNode {
  
  def apply(app: Application, pathPosition: (Path, Connection, Float)) = {
      new TimeNode(app, Some(pathPosition._1), pathPosition._2, pathPosition._3)
  }

}

/**
* This class represents a time node.
* A time node is bound to a playback position on a path and can be used to create time connections to other paths.
* The latter will then start their playback every time the playback position corresponding to this node is reached (if they are not already playing).
*
*/
class TimeNode(app: Application, associatedPath: Option[Path], var connection: Connection, var parameter: Float) extends Node(app, TimeNodeType, associatedPath, Vec3d(connection(parameter)._1, connection(parameter)._2)) {
//adds connection and parameter as attributes and can therefore not be realized by a node type
}
