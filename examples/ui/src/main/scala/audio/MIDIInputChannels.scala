package ui.audio

import org.mt4j.util.MTColor

import ui._

object MIDIInputChannels {
}


trait MIDIInputChannels {

  protected var midiChannel = new Array[Boolean](4) //initializing 4 channels for midi input    
  this.activateInputChannel(0) //set first channel to true
   
  def activateInputChannel(index: Int) = {
    for (index <- 0 until this.inputChannelNumber) this.setInputChannel(index, false) //set all channels to false
    this.setInputChannel(index, true) //then activate specified channel
  }
  
  def setInputChannel(index: Int, isActive: Boolean) = {
    this.midiChannel(index) = isActive
  }
  
  def isInputChannelActive(index: Int) = {
    this.midiChannel(index)
  }
  
  def inputChannelNumber = {
    this.midiChannel.size
  }
  
  def activeInputChannel: Int = {
    (0 until midiChannel.size).foreach(index => if (this.midiChannel(index)) return index)
    return -1
  }
  
  /**
  * Returns an array containing the indices of all open channels.
  */
  def collectOpenInputChannels = {
    this.midiChannel.zipWithIndex.collect({case (element, index) if (element) => index})    
  }
  
}
  
