package ui.input

import org.mt4j.Application

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import scala.actors._

import ui.paths._
import ui.events._
import ui.audio._
import ui.properties.types._
import ui.menus.main._
import ui._
import org.mt4j.util.SessionLogger

class PlayTimbreDragAction(node: Node) extends NotifyingDragAction(node) {
  
  	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
  	  val returnValue = super.processGestureEvent(gestureEvent)
  	  //println("play timbre drag action")
      gestureEvent match {
        case dragEvent: DragEvent => {
          dragEvent.getId match {
            case MTGestureEvent.GESTURE_DETECTED => {println("detected"); this.sendPlayAudioEvent()}
            case MTGestureEvent.GESTURE_RESUMED => {println("resumed"); this.sendPlayAudioEvent()}
            case MTGestureEvent.GESTURE_UPDATED => {/*println("updated");*/ this.sendPlayAudioEvent()}
            case MTGestureEvent.GESTURE_CANCELED => {println("canceled"); this.sendPauseAudioEvent()}
            case MTGestureEvent.GESTURE_ENDED => {println("ended"); this.sendPauseAudioEvent()}
            case somethingElse => {println("some other gesture event type")}
          }
        }
      }  	  
  	  returnValue
  	}
  	
  	private def sendPlayAudioEvent() = {
      val channels = node match {
        case withChannels: AudioOutputChannels => withChannels.collectOpenOutputChannels
        case withoutChannels => Array(0,1,2,3)
      } 
      
      val (uiX, uiY) =  (node.position._1.toInt, node.position._2.toInt)
      val (x, y) = (uiX/Ui.width.toFloat, uiY/Ui.height.toFloat)
      
      node match {
        case singleNode: SingleNode => {
          SessionLogger.log("Move (TimbrePlay): Node",SessionLogger.SessionEvent.Event, this, singleNode, (singleNode.position));
          if (!singleNode.isPlaying) {
            Ui.audioInterface ! PlayAudioEvent(node.id, x, y, singleNode.getPropertyValue(PitchPropertyType), singleNode.getPropertyValue(VolumePropertyType), singleNode.activeInputChannel, channels)
          }
        }
        case otherNode => {
          otherNode.associatedPath.foreach(path => {
            SessionLogger.log("Move (TimbrePlay): Path",SessionLogger.SessionEvent.Event, this, otherNode, (otherNode.position, path));
            if (!path.isPlaying) {
              Ui.audioInterface ! PlayAudioEvent(path.id, x, y, PitchPropertyType.mean, VolumePropertyType.mean, path.activeInputChannel, channels)
            }
          })
        }
      }
    }
    
    
    private def sendStopAudioEvent() = {
      node match {
        case singleNode: SingleNode => {
          if (!singleNode.isPlaying) {
            Ui.audioInterface ! PauseAudioEvent(node.id)
          }
        }
        case otherNode => {
          otherNode.associatedPath.foreach(path => {
            if (!path.isPlaying) {
              Ui.audioInterface ! StopAudioEvent(path.id)
            }
          })
        }
      }         
    }
    
    
    private def sendPauseAudioEvent() = {
      node match {
        case singleNode: SingleNode => {
          if (!singleNode.isPlaying) {
            Ui.audioInterface ! PauseAudioEvent(node.id)
          }
        }
        case otherNode => {
          otherNode.associatedPath.foreach(path => {
            if (!path.isPlaying) {
              Ui.audioInterface ! PauseAudioEvent(path.id)
            }
          })
        }
      }      
    }
  	
}
