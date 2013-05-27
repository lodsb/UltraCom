package main.scala.TreeQuencer

import java.io.File
import collection.mutable.ArrayBuffer
import scala.util.Random
import de.sciss.synth.SynthDef
import com.twitter.util.Eval
import collection.mutable
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh
import org.mt4j.util.modelImporter.ModelImporterFactory

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
  val formCache = new mutable.HashMap[File, Array[MTTriangleMesh]]()
  val synthiCache = new mutable.HashMap[File, SynthDef]()
  private val evaluateFile = new Eval()

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

  // build cache of SynthDefs and MTTriangleMeshes
  println("Building form cache...")
  formFiles.foreach(file => cacheMTTriangleMesh(file))
  println("Building form cache finished!")

  println("Building synthesizer cache...")
  synthiFiles.foreach(file => cacheSynthDef(file))
  println("Building synthesizer finished!")

  /**
   * Picks a form randomly from form-folder.
   * Not the sourceForm (i.e. the form from source node).
   * @return NodeForm The found form
   */
  def randomFormFile = {
    random = new Random().nextInt(formFiles.length)
    formFiles(random)
    //new File(System.getProperty("user.dir")+"/forms/tetrahedron.obj")
  }

  def randomSynthiFile = {
    synthiFiles(random)
    //new File(System.getProperty("user.dir")+"/forms/tetrahedron.scala")
  }

  def apply = this

  def cacheSynthDef(file: File): SynthDef = {
    if (synthiCache.get(file).isEmpty) {
      synthiCache += ((file,evaluateFile[SynthDef](file)))
    }
    synthiCache.get(file).get
  }

  def cacheMTTriangleMesh(file: File): Array[MTTriangleMesh] = {
    if (FileImporter.formCache.get(file).isEmpty) {
      FileImporter.formCache += ((file,ModelImporterFactory.loadModel(app, file.getAbsolutePath, 180, true, false)))
    }

    val cachedMeshes = formCache.get(file).get
    val meshes = new Array[MTTriangleMesh](cachedMeshes.size)
    for (i <- 0 to cachedMeshes.size-1) {
      meshes(i) = new MTTriangleMesh(app, formCache.get(file).get(i).getGeometryInfo)
    }
    meshes
  }

}

object Import {
  def form(formFile: File) = {
    new NodeForm(formFile)
  }
  def synthesizer(node: main.scala.TreeQuencer.Node, synthiFile: File) = {
    new NodeSynthesizer(node, synthiFile)
  }
}