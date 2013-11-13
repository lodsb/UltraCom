package ui.util

/**
* Encapsulates special functions.
*/
object Functions {
  

  /**
  * This function is composed of a 'normal' sigmoid function between x = 0 and x= 0.5 and a mirrored sigmoid function between x = 0.5 and x = 1,
  * with a single maxima y = 1 at x = 0.5 and two roots at x = 0 and x = 1. For values outside the range [0,1], the function returns 0.
  */
  def mirroredSigmoid(value: Float) = {
    val a = 2.0f
    val x = if (value <= 0.5) 2*value else 2*(1 - value)
    if (value < 0 || value > 1) 0
    else (math.pow(x,a)/(math.pow(x,a) + math.pow(1-x,a))).toFloat
  }
  
  /**
  * This function returns 1 for input values between 0 and 1, otherwise 0.
  */
  def bool(value: Float) = {
    if (value >= 0 && value <= 1) 1 else 0
  }
  
  /**
  * This function returns 1 always.
  */
  def one(value: Float) = {
    1.0f
  }
  
  /**
  * Curried function which takes an expected value my and a radius and returns a gaussian distribution for which 99% of the points lie inside that radius.
  */
  private def radiusGaussian(my: Float, radius: Float)(x: Float) = {
    val stdDev = radius/3 //standard deviation; /3 because the radius shall correspond to 3 sigmas
    // it follows the probability density function of a normal distribution
    (1.0f/(math.sqrt(2 * math.Pi) * stdDev))* math.exp(-math.pow(x-my, 2)/(2*stdDev*stdDev))
  }
  
  /**
  * Curried function which takes two points and returns a new function representing a line between those points.
  * Supplying this function with a value between 0 and 1 then yields a point on that line.
  */
  def line(firstPoint: (Float, Float), secondPoint: (Float, Float))(t: Float) = {
    val (x1, y1) = firstPoint
    val (x2, y2) = secondPoint
    (t*x2 + (1-t)*x1, t*y2 + (1-t)*y1)
  }
  
  /**
  * Curried function which first takes a center and radius specifying a circle. 
  * Supplying this function with an angle in radians then yields the point on the circumference of the circle.
  */
  def circle(center: (Float, Float), radius: Float)(radAngle: Float) = {
    (radius*math.cos(radAngle).toFloat + center._1, radius*math.sin(radAngle).toFloat + center._2)
  }  
  
  /**
  * Returns for a given item (starting with 0) out of a fixed number of items the position on an arc segment with specified center, radius and length (in radians),
  * with the premise that the items are equidistantly distributed on the arc segment.
  * Use (2 * math.Pi) if you are aiming for a whole circle.
  */
  def positionOnCircle(x:Float, y: Float, radius: Float, arc: Float, item: Int, items: Int) = {
    this.circle((x,y), radius)(math.Pi.toFloat/2 + arc * ((2*item+1).toFloat/(2*items)))
  }  
  
  /**
  * Applies a coordinate transformation to the specified point using a given center and gradient.
  */
  def transform(center: (Float, Float), gradient: (Float, Float), point: (Float, Float)): (Float, Float) = {
    val (cx, cy) = (center._1, center._2)
    val (px, py) = (point._1, point._2)
    val (tx, ty) = (gradient._1, gradient._2)
    val atan2 = math.atan2(tx, ty)
    val radAngle = if (atan2 > 0) atan2 - math.Pi/2 else 1.5*math.Pi + atan2 //get radians for tangent
    ((cx + (px-cx)*math.cos(-radAngle) - (py-cy)*math.sin(-radAngle)).toFloat, (cy + (px-cx)*math.sin(-radAngle) + (py-cy)*math.cos(-radAngle)).toFloat)              
  }  
  
  /** 
  * Returns the gradient of the line between the two specified points.
  */
  def gradient(firstPoint: (Float, Float), secondPoint: (Float, Float)) = {
    val (x1, y1) = firstPoint
    val (x2, y2) = secondPoint    
    (x2 - x1, y2 - y1)
  }  
  
  def gradientToDegrees(gradient: (Float, Float)) = {
    val (tx,ty) = gradient
    val atan2 = math.atan2(ty,tx).toFloat //get angle for mirrored vector
    val angle = ((if (atan2 > 0) atan2 else (2*math.Pi + atan2)) * 360 / (2*math.Pi)).toFloat //convert to degrees
    angle
  }
  
  
  
   /** Converts an HSL color value to RGB. Conversion formula
   * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
   * Assumes h, s, and l are contained in the set [0, 1] and
   * returns r, g, and b in the set [0, 255].
   *
   * Code adapted from http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
   *
   * @param h       The hue
   * @param s       The saturation
   * @param l       The lightness
   * @return        the RGB representation
   */
   def hslToRgb(h: Float, s: Float, l: Float): (Int, Int, Int) = {
      var r: Float = 0f
      var g: Float = 0f
      var b: Float = 0f
  
      if(s == 0f) {
          r = l
          g = l
          b = l // achromatic
      }
      else {
          def hue2rgb(p: Float, q: Float, valT: Float): Float = {
              var t: Float = valT
              if (t < 0) t = t + 1
              if (t > 1) t = t - 1
              if (t < 1/6f) return p + (q - p) * 6f * t
              if (t < 1/2f) return q
              if (t < 2/3f) return p + (q - p) * (2/3f - t) * 6f
              p
          }
  
          var q = if (l < 0.5) l * (1 + s) else l + s - l * s
          var p = 2 * l - q
          r = hue2rgb(p, q, h + 1/3f)
          g = hue2rgb(p, q, h)
          b = hue2rgb(p, q, h - 1/3f)
      }
  
      if (r > 1 || g > 1 || b > 1) {
        println("hslToRbg: r: " + r + " g: " + g + " b: " + b)
      }      
      return ((r * 255).toInt, (g * 255).toInt, (b * 255).toInt)
  }
   
  
  
   /**
   * Converts an RGB color value to HSL. Conversion formula
   * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
   * Assumes r, g, and b are contained in the set [0, 255] and
   * returns h, s, and l in the set [0, 1].
   *
   * Code adapted from http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
   *
   * @param   r       The red color value
   * @param   g       The green color value
   * @param   b       The blue color value
   * @return          The HSL representation
   */
  def rgbToHsl(valR: Int, valG: Int, valB: Int): (Float, Float, Float) = {
      var r: Float = valR/255.0f
      var g: Float = valG/255.0f
      var b: Float = valB/255.0f
      var max = math.max(math.max(r, g), b)
      var min = math.min(math.min(r, g), b)
      var h = (max + min) / 2
      var s = (max + min) / 2
      var l = (max + min) / 2
  
      if (max == min) {
          h = 0
          s = 0 // achromatic
      }
      else {
          var d = max - min
          s = if (l > 0.5) d / (2 - max - min) else d / (max + min)
            
          if (max == r) {
            h = (g - b) / d + (if (g < b) 6f else 0)
          }
          else if (max == g) {
            h = (b - r) / d + 2f
          }
          else if (max == b) {
            (r - g) / d + 4f
          }
          h = h / 6f
      } 
      (h, s, l)
  }
  
  
}
