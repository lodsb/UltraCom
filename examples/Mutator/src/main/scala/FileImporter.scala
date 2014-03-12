package mutator

import java.io.File
import collection.mutable.ArrayBuffer
import scala.util.Random
import de.sciss.synth.{Synth, SynthDef}
import com.twitter.util.Eval
import collection.mutable
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh
import org.mt4j.util.modelImporter.ModelImporterFactory
import org.mt4j.util.opengl.GLMaterial
import org.mt4j.util.math.Tools3D

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

  val formsDirectory = new File(System.getProperty("user.dir")+"/nodes/")
  val formFiles = new ArrayBuffer[File]()
  val materialFiles = new ArrayBuffer[File]()
  val synthiFiles = new ArrayBuffer[File]()
  var sourceNodeFormFile: File = null
  val formCache = new mutable.HashMap[File, Array[MTTriangleMesh]]()
  val materialCache = new mutable.HashMap[File, NodeMaterial]()
  val synthiCache = new mutable.HashMap[File, SynthDef]()
  private val evaluateFile = new Eval()
  private var random: Int = null.asInstanceOf[Int]

  private var i = -1
  private def iterator = {i+=1;i%=formFiles.size;i}


  formsDirectory.listFiles.foreach( formFile => {
    val synthiFile = new File(formFile.getAbsolutePath.replace(".obj", ".scala"))
    val materialFile = new File(formFile.getAbsolutePath.replace(".obj", "_material.scala"))
    if(formFile.getName.endsWith(".obj")) {
      if (formFile.getName.startsWith("center.obj")) {
        sourceNodeFormFile = formFile
      } else if(synthiFile.exists && materialFile.exists) {
        formFiles += formFile
        synthiFiles += synthiFile
        materialFiles += materialFile
      }
    }
  })

  formsDirectory.listFiles.foreach( formFile => {
    val synthiFile = new File(formFile.getAbsolutePath.replace(".3ds", ".scala"))
    val materialFile = new File(formFile.getAbsolutePath.replace(".3ds", "_material.scala"))
    if(formFile.getName.endsWith(".3ds")) {
      if (formFile.getName.startsWith("center.3ds")) {
        sourceNodeFormFile = formFile
      } else if(synthiFile.exists && materialFile.exists) {
        formFiles += formFile
        synthiFiles += synthiFile
        materialFiles += materialFile
      }
    }
  })

  // build cache of SynthDefs and MTTriangleMeshes
  println("Building form cache...")
  formFiles.foreach(file => cacheMTTriangleMesh(file))
  println("Building form cache finished!")

  println("Building material cache...")
  materialFiles.foreach(file => cacheGLMaterial(file))
  println("Building material cache finished!")

  /**
   * Picks a form randomly from form-folder.
   * Not the sourceForm (i.e. the form from source node).
   * @return NodeForm The found form
   */
  def randomFormFile = {
    random = iterator
    formFiles(random)
  }

  def numFormFile(no: Int) : File = {
    val num = no % formFiles.size

    formFiles(num)
  }

  def randomSynthiFile = {
    synthiFiles(random)
  }

  def randomMaterialFile = {
    materialFiles(random)
  }

  def apply = this

  def cacheGLMaterial(file: File): NodeMaterial = {
    println("FILENAME: " + file.getName)
    if (!materialCache.contains(file)) {
      // god damned twitter eval lib issues related to 2.10.3 ?
      //materialCache(file) = evaluateFile[NodeMaterial](file)

      println(file.getName)


      val mat: NodeMaterial = file.getName match {
        case "angular_something_material.scala" => new NodeMaterial(Tools3D.getGL()) {
          override val name = "angular_something_material.scala"
                                              setAmbient(Array( 1.00f, 0.39f, 0.43f, 0.95f ));
                                              setDiffuse(Array( 1.00f, 0.39f, 0.43f, 0.95f ));
                                              setSpecular(Array( 2*0.33f, 2*0.39f, 2*0.43f, 0.95f ))
                                              setEmission(Array( .8f, 0.29f, 0.33f, 0.95f ));
                                              setShininess(80);
                                            }


        case  "center_material.scala" => new NodeMaterial(Tools3D.getGL()) {
                                            override val name = "center_material.scala"
                                            setAmbient(Array( 0.8f, 0.8f, 0.8f, 1f ));
                                            setDiffuse(Array( 0.8f, 0.8f, 0.8f, 1f ));
                                            setSpecular(Array( 0.8f, 0.8f, 0.8f, 1f ))
                                            setEmission(Array( 0.5f, 0.5f, 0.5f, 1f ));;
                                            setShininess(80);
                                          }
       case  "cube_material.scala" => new NodeMaterial(Tools3D.getGL()) {
         override val name = "cube_material.scala"
                                             setAmbient(Array( 78f/255f,205f/255f,196f/255f, 0.95f ));
                                             setDiffuse(Array( 78f/255f,205f/255f,196f/255f, 0.95f ));
                                             setSpecular(Array( 78f/255f,205f/255f,196f/255f, 0.9f ));
                                             setEmission(Array( 78f/355f,205f/355f,196f/355f, 0.95f ));
                                             setShininess(80);
                                           }
        case  "decimate_material.scala" => new NodeMaterial(Tools3D.getGL()) {
          override val name = "decimate_material.scala"
                                                setAmbient(Array( 199f/255f,244f/255f,100f/255f, 0.95f ));
                                                setDiffuse(Array( 199f/255f,244f/255f,100f/255f, 0.95f ));
                                                setSpecular(Array( 199f/255f,244f/255f,100f/255f, 0.95f ))
                                                setEmission(Array( 199f/355f,244f/355f,100f/355f, 0.95f ));;
                                                setShininess(80);
                                              }

        case  "iconosphere_material.scala" => new NodeMaterial(Tools3D.getGL()) {
          override val name = "iconosphere_material.scala"
                                                setAmbient(Array( 0f, 0f, 1f, 0.95f));
                                                setDiffuse(Array( 0f, 0f, 1f, 0.95f ));
                                                setSpecular(Array( 0f, 0f, 1f, 0.95f ))
                                                setEmission(Array( 0f, 0f, 1f, 0.95f ));;
                                                setShininess(80);
                                              }
        case  "pentagon_material.scala"   => new NodeMaterial(Tools3D.getGL()) {
          override val name = "pentagon_material.scala"
                                            setAmbient(Array( 255f/255f,107f/255f,107f/255f, 0.95f ));
                                            setDiffuse(Array( 255f/255f,107f/255f,107f/255f, 0.95f ));
                                            setSpecular(Array( 255f/255f,107f/255f,107f/255f, 0.95f ))
                                            setEmission(Array( 255f/355f,107f/355f,107f/355f, 0.95f ));;
                                            setShininess(80);
                                          }
        case  "tetrahedron_material.scala" => new NodeMaterial(Tools3D.getGL()) {
          override val name = "tetrahedron_material.scala"
                                    setAmbient(Array( 5/255f,77f/255f,88f/255f, 0.9f ));
                                    setDiffuse(Array( 50/255f,77f/255f,88f/255f, 0.9f ));
                                    setSpecular(Array( 100/255f,77f/255f,88f/255f, 0.9f ));
                                    setEmission(Array( 50/355f,77f/355f,88f/355f, 0.9f ));
                                    setShininess(80);
                                  }
      }

      /*
      mat = new NodeMaterial(Tools3D.getGL(Mutator)) {
        override val name = "test"
        setAmbient(Array( 1f, 0f, 0f, 0.9f ));
        setDiffuse(Array( 1f, 0f, 0f, 0.9f ));
        setSpecular(Array( 1f, 0f, 0f, 0.9f ));
        setEmission(Array( .15f, .15f, .15f, 0.9f ));
        setShininess(10);
      } */

     // mat.setDefaults
      println("mat " + mat.name)
      materialCache(file) = mat
    }

    materialCache(file)
  }

  def getMaterialFromNo(id: Int) : NodeMaterial = {
    materialCache.toList(id)._2
  }

  def cacheMTTriangleMesh(file: File): Array[MTTriangleMesh] = {
    if (FileImporter.formCache.get(file).isEmpty) {
      FileImporter.formCache += ((file,ModelImporterFactory.loadModel(Mutator, file.getAbsolutePath, 180, true, false)))
    }

    val cachedMeshes = formCache.get(file).get
    val meshes = new Array[MTTriangleMesh](cachedMeshes.size)
    for (i <- 0 to cachedMeshes.size-1) {
      meshes(i) = new MTTriangleMesh(Mutator, formCache.get(file).get(i).getGeometryInfo)
    }

    formCache.clear

    meshes
  }

  def triangleMesh(file: File): Array[MTTriangleMesh] = {
    val model = ModelImporterFactory.loadModel(Mutator, file.getAbsolutePath, 180, true, false)

    val meshes = new Array[MTTriangleMesh](model.size)
    for (i <- 0 to model.size-1) {
      meshes(i) = new MTTriangleMesh(Mutator, model(i).getGeometryInfo)
    }


    meshes
  }

}

object Import {
  def form(formFile: File) = {
    new NodeForm( formFile, Mutator)
  }
}