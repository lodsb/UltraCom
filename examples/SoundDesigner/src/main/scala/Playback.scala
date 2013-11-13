package ui

import scala.actors._

import scala.collection.mutable._

import ui.paths._
import ui.events._


/**
* This object keeps track of overall playback of paths.
*/
object Playback extends Actor {
  
  private val playingPaths = new HashSet[Path] with SynchronizedSet[Path]
  this.start()
  
  def act = {
    while (true) {
      receive {
        case event: PathPlaybackEvent => {
          if (event.isPlaying) {
            this.playingPaths += event.path
          }
          else {
            this.playingPaths -= event.path
          }
        } 
      }
    }
  }
  
  def isPlaying = {
    this.playingPaths.size > 0
  }
  
  def relativeTime = {
    val maxTime = playingPaths.foldLeft(Float.NegativeInfinity)((maxValue, path) => maxValue max path.time)
    val maxTimeLeft = playingPaths.foldLeft(Float.NegativeInfinity)((maxValue, path) => maxValue max path.timeLeft)
    maxTimeLeft/maxTime
  }
  
}
