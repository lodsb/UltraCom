import com.thesamet.spatial.KDTreeMap
import io.Source
import org.mt4j.components.visibleComponents.widgets.MTImage
import org.mt4j.MTApplication
import org.mt4j.util.math.Vector3D
import processing.core.PImage
import scala.util.Random

class PresetBank(csvFilename: String, mappingJitter:Float = 0.0f) {
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

    val seq:Iterator[((Float, Float), (Array[Float], Int))] = lines.map({
      l =>
        val values = l.split(",").map(_.toFloat)
        val xy = ( scala.math.max(values(0)+generateJitter(jitter),-1.0f)
          , scala.math.max(values(1) + generateJitter(jitter),-1.0f) )

        updateMinMax(values(0), values(1))

        val cluster = values(2).toInt

        val parameters = values.slice(3, values.size)

        println("PARSED "+unwrapParameterString(parameters)+" @ "+values(0)+","+values(1)+" cl "+values(2))

        (xy, (parameters, cluster))
    })

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

  def generateMappingImage(app: MTApplication) : MTImage = {
    val pimage = new PImage(app.width, app.height)


    pimage.loadPixels()

    val pixels = pimage.pixels

    // black bg
    (0 to pixels.size-1).foreach({x => pixels(x) = 0xff000000;})

    data.foreach({
      x =>
        val xyAbs = relToAbs(x._1._1, x._1._2, app);

        val r = ((x._2._2 * 2123) % 255).toByte;
        val g = ((x._2._2 * 931) % 255).toByte;
        val b = ((x._2._2 * 1137) % 255).toByte;

        pixels(xyAbs._1 + app.width*xyAbs._2) = 0xff000000 | r << 16 | g << 8 | b;

    })
    pimage.updatePixels()



    new MTImage(app, pimage)

  }

  def relToAbs(x: Float , y:Float,app: MTApplication): (Int, Int) = {
    val xAbs: Int = ( ( (x + 1.0f)*0.5f ) * (app.width-1)).round
    val yAbs: Int = ( ( (y + 1.0f)*0.5f ) * (app.height-1)).round

    (xAbs, yAbs)
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
}
