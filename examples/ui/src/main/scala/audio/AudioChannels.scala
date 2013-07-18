package ui.audio

trait AudioChannels {

  private var audioChannel = new Array[Boolean](Synthesizer.Channels)
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
  
