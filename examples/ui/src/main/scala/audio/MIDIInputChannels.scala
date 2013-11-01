package ui.audio

import org.mt4j.util.MTColor

import ui._

object MIDIInputChannels {
}


trait MIDIInputChannels {

  protected var midiChannel = new Array[Boolean](4) //initializing 4 channels for midi input
  for (index <- 0 until this.inputChannelNumber) this.setInputChannel(index, true) //set channels to true 
   
  def toggleInputChannel(index: Int) = {
    this.midiChannel(index) = !this.midiChannel(index)
  }
  
  def setInputChannel(index: Int, isOpen: Boolean) = {
    this.midiChannel(index) = isOpen
  }
  
  def isInputChannelOpen(index: Int) = {
    this.midiChannel(index)
  }
  
  def inputChannelNumber = {
    this.midiChannel.size
  }
  
  /**
  * Returns an array containing the indices of all open channels.
  */
  def collectOpenInputChannels = {
    this.midiChannel.zipWithIndex.collect({case (element, index) if (element) => index})    
  }
  
}
  
