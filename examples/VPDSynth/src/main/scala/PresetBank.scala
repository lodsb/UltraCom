import com.thesamet.spatial.KDTreeMap
import io.Source
import org.mt4j.components.visibleComponents.widgets.MTImage
import org.mt4j.MTApplication
import org.mt4j.util.math.Vector3D
import processing.core.PImage

class PresetBank(csvFilename: String) {
  private var minX = Float.MaxValue
  private var minY = Float.MaxValue

  private var maxX = Float.MinValue
  private var maxY = Float.MinValue

  private val data = parseCSVMap(csvFilename);
  private val kdmap : KDTreeMap[(Float, Float), Array[Float]] = KDTreeMap.fromSeq( data )

  private def parseCSVMap(filename: String) : Seq[((Float,Float),Array[Float])] = {

    val src = Source.fromFile(filename)

    val lines = src.getLines()

    val seq: Iterator[((Float, Float), Array[Float])] = lines.map({
      l =>
        val values = l.split(",").map(_.toFloat)
        val xy = (values(0), values(1))

        updateMinMax(values(0), values(1))

        val parameters = values.slice(2, values.size)

        println("PARSED "+unwrapParameterString(parameters)+" @ "+values(0)+","+values(1))

        (xy, parameters)
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

        pixels(xyAbs._1 + app.width*xyAbs._2) = 0xffaabbcc;

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

    seq(0)
  }
}
