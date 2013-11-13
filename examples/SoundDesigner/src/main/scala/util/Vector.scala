package ui.util

/**
* Encapsulates calculations on vectors.
*/
object Vector {
  
  /**
  * Returns the euclidean distance between two points in R^2.
  */
	def euclideanDistance(p1: (Float, Float), p2: (Float, Float)): Float = {
	  val (x1, y1) = p1
	  val (x2, y2) = p2
	  math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)).toFloat //euclidean distance between two points
	}
	
	/**
	* Returns the cross product of two vectors in R^3, which is again a vector in R^3.
	*/
	def crossProduct(v1: (Float, Float, Float), v2: (Float, Float, Float)): (Float, Float, Float) = {
	  val (x1, x2, x3) = v1
	  val (y1, y2, y3) = v2	  
	  (x2*y3 - x3*y2, 
	   x1*y3 - x3*y1, 
	   x1*y2 - x2*y1)
	}
	
	def x(v1: (Float, Float, Float), v2: (Float, Float, Float)): (Float, Float, Float) = {
	  this.crossProduct(v1, v2)
	}
	
	/**
	* Returns a normalized version of the specified vector in R^2, which is again a vector in R^2.
	*/
	def normalizedVector(v: (Float, Float)): (Float, Float) = {
	  val (x,y) = v
	  val length = math.sqrt(x*x + y*y).toFloat
	  (x/length, 
	   y/length)
	}
	
	/**
	* Returns the sum of the two vectors in R^2, which is again a vector in R^2.
	*/
	def +(v1: (Float, Float), v2: (Float, Float)): (Float, Float) = {
	  (v1._1 + v2._1, 
	   v1._2 + v2._2)
	}
	
  /**
	* Returns the vector from v1 to v2.
	*/
	def apply(v1: (Float, Float), v2: (Float, Float)): (Float, Float) = {
	  (v2._1 - v1._1, 
	   v2._2 - v1._2)
	}
	
	def /(v: (Float, Float), denominator: Float): (Float, Float) = {
	  (v._1/denominator, v._2/denominator)
	}
	
	
}
