
package org.lodsb.VPDSynth
import com.thesamet.spatial.KDTreeMap
import io.Source
import org.mt4j.components.visibleComponents.widgets.MTImage
import org.mt4j.MTApplication
import org.mt4j.util.math.Vector3D
import processing.core.PImage
import scala.util.Random


class PresetBank(csvFilename: String, mappingJitter:Float = 0.0f, private val loadPercussive: Boolean = true, private val loadInvEnv: Boolean = true) {
  private var minX = Float.MaxValue
  private var minY = Float.MaxValue

  private var maxX = Float.MinValue
  private var maxY = Float.MinValue

  private val random = new Random()

  private val data = parseCSVMap(csvFilename, mappingJitter);
  private val kdmap : KDTreeMap[(Float, Float), (Array[Float],Int)] = KDTreeMap.fromSeq( data )

  private def generateJitter(variance: Float) = {
    val r = random.nextGaussian().toFloat - 0.5f;

    r*variance;
  }

  private def parseCSVMap(filename: String, jitter:Float = 0.0f) : Seq[((Float,Float),(Array[Float],Int))] = {

    val src = Source.fromFile(filename)

    val lines = src.getLines()

    var seq:Iterator[((Float, Float), (Array[Float], Int))] = lines.map({
      l =>
        val values = l.split(",").map(_.toFloat)

        val xy = ( scala.math.max(values(0)+generateJitter(jitter),-1.0f)
          , scala.math.max(values(1) + generateJitter(jitter),-1.0f) )

        updateMinMax(values(0), values(1))

        val cluster = values(2).toInt

        val parameters = values.slice(3, values.size)

        //println("PARSED "+unwrapParameterString(parameters)+" @ "+values(0)+","+values(1)+" cl "+values(2))

        (xy, (parameters, cluster))
    })

    if(!loadPercussive) {
	    seq = seq.filter({x => 
		val p = x._2._1
	
		p(p.size-1) < 1f
	    })
    }

    if(!loadInvEnv) {
    	seq = seq.filter{x=>
		val p =x._2._1
		p(3) <= 0.75
	}
    }

    seq.toSeq
  }

  private def unwrapParameterString(parms: Array[Float]) : String = {
     val s: String = parms.foldLeft("")((x,y) => x+" , "+(y+""))

     s
   }


  private def updateMinMax(x:Float, y:Float) = {
    if (x < minX) {
      minX = x
    }

    if (y < minY) {
      minY = y
    }

    if (x > maxX) {
      maxX = x
    }

    if (y > maxY) {
      maxY = y
    }
  }


  def minmax = {
    ((minX, minY), (maxX, maxY))
  }

  def generateMappingImage(app: MTApplication): MTImage = {
    new MTImage(app, generateMappingPImage(app.width,app.height, 0xff222222))
  }

  def generateMappingPImage(width: Int, height: Int, fillColor: Int) : PImage = {
    val pimage = new PImage(width, height)


    pimage.loadPixels()

    val pixels = pimage.pixels

    // black bg
    (0 to pixels.size-1).foreach({x => pixels(x) = fillColor;})

    data.foreach({
      x =>
        val xyAbs = relToAbs(x._1._1, x._1._2, width, height);

        val r = ((x._2._2 * 2123) % 255).toByte;
        val g = ((x._2._2 * 931) % 255).toByte;
        val b = ((x._2._2 * 1137) % 255).toByte;

        try {
        pixels(xyAbs._1 + width*xyAbs._2) = 0xff000000 | r << 16 | g << 8 | b;
        } 
        catch {
          case exception => println(exception)
        }

    })
    pimage.updatePixels()



    pimage

  }
  
  /**
  * Returns a formatted version of the preset bank data, that is,
  * a sequence of tuples of (x,y) value pairs and associated tuples of the form (ClusterID, Octave, isPercussive).
  * 
  */
  def getFormattedData(width: Int, height: Int): Seq[((Int, Int), (Int, Int, Boolean))] = {    
    val formattedData:Seq[((Int, Int), (Int, Int, Boolean))] = data.map({
      entry =>
        val (x,y) = relToAbs(entry._1._1, entry._1._2, width, height)
        val clusterID = entry._2._2
        val parameters = entry._2._1          
        ((x,y), (entry._2._2, parameters(parameters.size-2).toInt, if (parameters(parameters.size-1) < 1f) false else true))          
    })
    
    formattedData
  }
  
  

  def relToAbs(x: Float , y:Float, width: Int, height: Int): (Int, Int) = {
    val xAbs: Int = ( ( (x + 1.0f)*0.5f ) * (width-1)).round
    val yAbs: Int = ( ( (y + 1.0f)*0.5f ) * (height-1)).round

    (xAbs, yAbs)
  }

  def relToAbs(x: Float , y:Float,app: MTApplication): (Int, Int) = {
    relToAbs(x,y,app.width, app.height)
  }


  def parameterAppCoord(vector: Vector3D, app: MTApplication) : ((Int,Int),Array[Float]) = {
    val xAbs = vector.x
    val yAbs = vector.y

    val xRel = 2.0f*((xAbs / app.width.toFloat) - 0.5f)
    val yRel = 2.0f*((yAbs / app.height.toFloat) - 0.5f)

    val ret = parameterRelCoord(xRel,yRel)

    (relToAbs(ret._1._1, ret._1._2, app), ret._2)

  }

  def parameterRelCoord(x: Float, y: Float) : ((Float,Float),Array[Float]) = {
    val seq = kdmap.findNearest( (x,y), 1 )

    (seq(0)._1,seq(0)._2._1)
  }

  private def distance(x1: Float, y1: Float, x2: Float, y2: Float, pow: Double = 1.0 ) = {
    scala.math.pow(scala.math.sqrt( ( (x1-x2)*(x1-x2) )+ ( (y1-y2) * (y1-y2) ) ), pow )
  }

  def parameterAppCoordInterp(vector: Vector3D, app: MTApplication, neighbors: Int) : (Seq[(Int,Int)],Array[Float]) = {
    val xAbs = vector.x
    val yAbs = vector.y

    val xRel = 2.0f*((xAbs / app.width.toFloat) - 0.5f)
    val yRel = 2.0f*((yAbs / app.height.toFloat) - 0.5f)

    val ret = parameterRelCoordInterp(xRel,yRel, neighbors)

    val coords = ret._1.map( xy => relToAbs(xy._1, xy._2, app))

    (coords, ret._2)

  }

  def parameterRelCoordInterp(x: Float, y: Float, neighbors: Int) : (Seq[(Float,Float)],Array[Float]) = {
    val seq = kdmap.findNearest( (x,y), neighbors )

    //println(">>interpol")
    //seq.foreach( s => println(unwrapParameterString(s._2._1)))
    //println("<<interpol")

    val weights = seq.map { n => 1f / distance(x,y, n._1._1, n._1._2) }

    //println(weights)

    val weightSum = weights.sum

    val normalizedWeights = weights.map( w => w/weightSum)


    val parmSequences = normalizedWeights.zipWithIndex.map( {
      wIdx =>
        val w = wIdx._1
        val idx=wIdx._2

        val wSeq = seq(idx)._2._1.map( parameter => parameter*w)

        wSeq
    })

    val parametersDouble = parmSequences.foldLeft(new Array[Double](seq(0)._2._1.size)){
      (ps1, ps2) =>  (0 to (ps1.size-1)).map( i => ps1(i) + ps2(i)).toArray
    }

    val parameters = parametersDouble.map( p => p.toFloat)

    val coordinates = seq.map( s => s._1)

    (coordinates, parameters)
  }
}
