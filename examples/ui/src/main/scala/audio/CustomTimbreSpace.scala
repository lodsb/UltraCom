package ui.audio

import org.mt4j.util.MT4jSettings

import ui._

/**
* This object realizes a custom timbre space.
*/
object CustomTimbreSpace extends TimbreSpace {
  
  override def process(event: AudioEvent) = {
        
  }
  
  override def visualization = {
    Some(Ui.loadImage(MT4jSettings.getInstance.getDefaultImagesPath + "customTimbreSpace.jpg"))  
  }
  
}
