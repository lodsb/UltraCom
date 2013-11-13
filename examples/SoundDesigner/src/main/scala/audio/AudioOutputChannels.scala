package ui.audio

import org.mt4j.util.MTColor

import ui._

object AudioOutputChannels {
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


trait AudioOutputChannels {

  protected var audioChannel = new Array[Boolean](Ui.audioInterface.Channels)
  for (index <- 0 until this.outputChannelNumber) this.setOutputChannel(index, true) //set channels to true 
   
  def toggleOutputChannel(index: Int) = {
    this.audioChannel(index) = !this.audioChannel(index)
  }
  
  def setOutputChannel(index: Int, isOpen: Boolean) = {
    this.audioChannel(index) = isOpen
  }
  
  def isOutputChannelOpen(index: Int) = {
    this.audioChannel(index)
  }
  
  def outputChannelNumber = {
    this.audioChannel.size
  }
  
  /**
  * Returns an array containing the indices of all open channels.
  */
  def collectOpenOutputChannels = {
    this.audioChannel.zipWithIndex.collect({case (element, index) if (element) => index})    
  }
  
}
  
