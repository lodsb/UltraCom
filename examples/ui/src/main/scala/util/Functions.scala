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
  
}
