package ui.audio

import org.mt4j.util.MTColor

import ui._

object AudioChannels {
    private val ChannelColor = Array(new MTColor(255, 0, 0), new MTColor(0, 255, 0), new MTColor(0, 0, 255), new MTColor(255, 255, 0))
    
    /**
    * Returns for a given index the associated color, or white if the index is out of range.
    */
    def colorFromIndex(index: Int) = {
      if (index >= 0 && index < ChannelColor.size) {
        ChannelColor(index)
      }
      else new MTColor(255,255,255)
    }
}


trait AudioChannels {

  private var audioChannel = new Array[Boolean](Ui.audioInterface.Channels)
  for (index <- 0 until this.channelNumber) this.setChannel(index, true) //set channels to true 
   
  def toggleChannel(index: Int) = {
    this.audioChannel(index) = !this.audioChannel(index)
  }
  
  def setChannel(index: Int, isOpen: Boolean) = {
    this.audioChannel(index) = isOpen
  }
  
  def isChannelOpen(index: Int) = {
    this.audioChannel(index)
  }
  
  def channelNumber = {
    this.audioChannel.size
  }
  
  /**
  * Returns an array containing the indices of all open channels.
  */
  def collectOpenChannels = {
    this.audioChannel.zipWithIndex.collect({case (element, index) if (element) => index})    
  }
  
}
  
