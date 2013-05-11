package TreeQuencer

import java.io.File
import collection.mutable.ArrayBuffer
import scala.util.Random
import de.sciss.synth.SynthDef
import com.twitter.util.Eval

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  02.05.13 :: 12:25
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
 
 
 object FileImporter {
  // load all *.obj files from form-directory
  // make 3d objects out of it, that only need to be copied late

  val formsDirectory = new File(System.getProperty("user.dir")+"/forms/")
  val formFiles = new ArrayBuffer[File]()
  val synthiFiles = new ArrayBuffer[File]()
  var sourceNodeFormFile: File = null

  private var random: Int = null.asInstanceOf[Int]


  formsDirectory.listFiles.foreach( formFile => {
    val synthiFile = new File(formFile.getAbsolutePath.replace(".obj", ".scala"))
    if(formFile.getName.endsWith(".obj")) {
      if (formFile.getName.startsWith("center.obj")) {
        sourceNodeFormFile = formFile
      } else if(synthiFile.exists()) {
        formFiles += formFile
        synthiFiles += synthiFile
      }
    }
  })

  /**
   * Picks a form randomly from form-folder.
   * Not the sourceForm (i.e. the form from source node).
   * @return ImportedForm The found form
   */
  def randomFormFile = {
    random = new Random().nextInt(formFiles.length)
    formFiles(random)
  }

  def randomSynthiFile = {
    synthiFiles(random)
  }

  def apply = {
    this
  }

}

object Import {
  private val evaluateFile = new Eval()
  def form(formFile: File) = {
    new ImportedForm(formFile)
  }
  def synthesizer(synthiFile: File) = {
    evaluateFile[SynthDef](synthiFile)
  }
}