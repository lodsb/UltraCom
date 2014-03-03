package mutator

object Util {
  def linlin(input: Double, inmin: Double, inmax: Double, outmin: Double, outmax: Double) : Double = {
    val scale = (outmax - outmin) / (inmax - inmin)
    val offset= outmin

//    println("i " +input + " o  "+offset + " s "+scale+ " "+ inmin + "  "+inmax + " "+outmin)

    (input-inmin)*scale + offset
  }
}