package ui.util

import collection.immutable.ListMap
import scala.collection.mutable.ArrayBuffer

/**
* Encapsulates functions relating to bezier curves.
*/
object Bezier {
  
  var Precision = 500 //the precision used by the approximation algorithms of this object 
  /* note that the precision should be significantly higher than the number of property buckets/number of drawn rectangles
     to obtain a visually pleasing result */
  
	/**
	* Curried function which takes three points and returns the function corresponding to the quadratic bezier curve defined by these points.
	* Supplying this function with a value between 0 and 1 then yields a point on the curve.
	*/
	def quadraticCurve(p0: (Float, Float), p1: (Float, Float), p2: (Float, Float))(t: Float) = {
	  val (x0, y0) = p0
	  val (x1, y1) = p1
	  val (x2, y2) = p2
	  val x = (1-t)*(1-t)*x0 + 2*(1-t)*t*x1 + t*t*x2
	  val y = (1-t)*(1-t)*y0 + 2*(1-t)*t*y1 + t*t*y2
	  (x,y)
	}
	
	/**
	* Curried function which takes three points and returns a function calculating the tangents of the quadratic bezier curve defined by these points.
	* Supplying this function with a value between 0 and 1 then yields the tangent at that particular point on the curve.
	*/
	def quadraticCurveTangent(p0: (Float, Float), p1: (Float, Float), p2: (Float, Float))(t: Float) = {
	  val (x0, y0) = p0
	  val (x1, y1) = p1
	  val (x2, y2) = p2
	  val tx = 2*(1-t)*(x1-x0) + 2*t*(x2-x1)
	  val ty = 2*(1-t)*(y1-y0) + 2*t*(y2-y1)
	  (tx,ty)
	}
	
	/**
	* Returns for a quadratic bezier curve given by its three points the arc length of that curve, using a closed form solution.
	* For a derivation, see http://segfaultlabs.com/docs/quadratic-bezier-curve-length
	* For an alternative implementation, see http://stackoverflow.com/questions/9245666/calculate-the-perimeter-of-a-bezier-curve-in-java
	* Not actually used at the moment since it is numerically unstable.
	*/
	def quadraticCurveArcLength(p0: (Float, Float), p1: (Float, Float), p2: (Float, Float)): Float = {
	  val (x0, y0) = p0
	  val (x1, y1) = p1
	  val (x2, y2) = p2
    val (ax, ay) = (x0 - 2*x1 + x2, y0 - 2*y1 + y2)
    val (bx, by) = (2*x1 - 2*x0, 2*y1 - 2*y0)
    val a = 4 * (ax * ax + ay * ay) //avoid 0 since we divide by 'a' later on... zero happens when the control point corresponds to the mean of the anchor points
    val b = 4 * (ax * bx + ay * by)
    val c = bx * bx + by * by
    if (a == 0 || b == 0 || c == 0) { //if a, b or c are 0, we have a straight line and return the length immediately
      math.sqrt((x0-x2)*(x0-x2) + (y0-y2)*(y0-y2)).toFloat 
    }   
    else{ //else we have a real curve; note that if we don't define this as an else-case, the if-result will not be recognized as the return value...
      val a32 = math.sqrt(a*a*a)
      val sqrtABC = math.sqrt(a+b+c)
      val logNum = 2*math.sqrt(a) + b/math.sqrt(a) + 2*sqrtABC
      val logDenom = b/math.sqrt(a) + 2*math.sqrt(c)
      val arcLength = (1/(8*a32) * (4*a32*sqrtABC + 2*math.sqrt(a)*b*(sqrtABC - math.sqrt(c)) + (4*c*a - b*b) * math.log(logNum/logDenom))).toFloat
      //println("arcLength = " + arcLength)
      arcLength
    }
	}
	
	/**
	* Returns for a quadratic bezier curve given by its anchor/control points a function mapping any curve parameter to its corresponding arc length parameter,
	* using an approximation technique and the split operation of de casteljau's algorithm.
	*/
	def quadraticCurveToArcLengthParameter(p0: (Float, Float), p1: (Float, Float), p2: (Float, Float)): Float => Float = {
	  val arcLength = this.arcLength(this.quadraticCurve(p0,p1,p2))
    val split = this.deCasteljauSplit(p0,p1,p2)_
	  t => {
	    val (p0, pc, pt) = split(t)._1 //get points of first curve
	    val length = this.arcLength(this.quadraticCurve(p0,pc,pt))
	    length/arcLength
	  }  
	}		
	
	/**
	* Curried function which first takes a quadratic bezier curve given by its anchor/control points.
	* Supplying this method with a curve parameter t then yields the two quadratic bezier curves which, when merged at their ends, make up the specified curve,
	* again represented by their anchor/control points.
	* That is, the algorithm splits the curve at the given curve parameter.
	*/
	def deCasteljauSplit(p0: (Float, Float), p1: (Float, Float), p2: (Float, Float))(t: Float) = {
    val (x0, y0) = p0
	  val (x1, y1) = p1
	  val (x2, y2) = p2
	  val splitPoint = this.quadraticCurve(p0,p1,p2)(t)
	  val firstNewControlPoint = ((1-t) * x0 + t * x1, (1-t) * y0 + t * y1)
	  val secondNewControlPoint = ((1-t) * x1 + t * x2, (1-t) * y1 + t * y2)
	  ((p0, firstNewControlPoint, splitPoint), (splitPoint, secondNewControlPoint, p2))
	}
	
	
	/**
	* Returns for a given start and control point of a quadratic bezier curve the first control point of the corresponding cubic bezier curve.
	*/
  def cubicFirstControlPoint(startPoint: (Float, Float), controlPoint: (Float, Float)) = {
     val (x1, y1) = startPoint
     val (xq, yq) = controlPoint
     (x1 + 2.0f/3.0f * (xq - x1), y1 + 2.0f/3.0f * (yq - y1)) //calculating first control point for cubic bezier curve   
  }

	/**
	* Returns for a given control and end point of a quadratic bezier curve the second control point of the corresponding cubic bezier curve.
	*/  
  def cubicSecondControlPoint(controlPoint: (Float, Float), endPoint: (Float, Float)) = {
     val (x4, y4) = endPoint
     val (xq, yq) = controlPoint
     (x4 + 2.0f/3.0f * (xq - x4), y4 + 2.0f/3.0f * (yq - y4)) //calculating second control point for cubic bezier curve    
  }  
  
  
  /**
  * Returns for a given (arbitrary) bezier curve its numerically approximated arc length.
  * More precisely, this method divides the curve in small line segments whose lengths are then summed, the number of the line segments being equivalent to this object's precision.
  * Thus, a higher value for precision will yield a more precise result since the curve is approximated by a greater number of lines.
  */
  def arcLength(curve: Float => (Float, Float)) = {
    var length = 0.0f
    for (i <- 0 to Precision - 1){
      length += Vector.euclideanDistance(curve(i/Precision.toFloat), curve((i+1)/Precision.toFloat)) //since we divide by precision, we simply sum over the interval [0,1]
    }
    length
  }  

	
	/**
	* Returns for a given bezier curve an array buffer with corresponding (curve parameter, arc length parameter) value pairs, usings this object's precision.
	* Moreover, the elements of the returned array buffer are already sorted in ascending order without any sorting overhead.
	*/
	private def toArcLengthParameterArray(curve: Float => (Float, Float)): ArrayBuffer[(Float, Float)] = {
	  val arcLength = this.arcLength(curve)
	  var arcLengthArray = new ArrayBuffer[(Float, Float)](Precision) //first value is curve parameterization, second arc length parameterization
	  
	  var length = 0.0f
    for (i <- 0 to Precision - 1){
      arcLengthArray += ((i/Precision.toFloat, length/arcLength))
      length += Vector.euclideanDistance(curve(i/Precision.toFloat), curve((i+1)/Precision.toFloat)) //since we divide by precision, we simply sum over the interval [0,1]
    }
    arcLengthArray += (1.0f -> 1.0f)   
    arcLengthArray
	}		
	
	
	/**
	* Returns for a given bezier curve an array buffer with corresponding (arc length parameter, curve parameter) value pairs, usings this object's precision.
	* Moreover, the elements of the returned array buffer are already sorted in ascending order without any sorting overhead.
	*/
	private def toCurveParameterArray(curve: Float => (Float, Float)): ArrayBuffer[(Float, Float)] = {
	  val arcLength = this.arcLength(curve)
	  var arcLengthArray = new ArrayBuffer[(Float, Float)](Precision) //first value is curve parameterization, second arc length parameterization
	  
	  var length = 0.0f
    for (i <- 0 to Precision - 1){
      arcLengthArray += ((length/arcLength, i/Precision.toFloat))
      length += Vector.euclideanDistance(curve(i/Precision.toFloat), curve((i+1)/Precision.toFloat)) //since we divide by precision, we simply sum over the interval [0,1]
    }
    arcLengthArray += (1.0f -> 1.0f)   
    arcLengthArray
	}		
	
	
	/**
	* Returns for a given bezier curve a function mapping any curve parameter to its corresponding arc length parameter,
	* using a numerical approximation technique.
	*/
	def toArcLengthParameter(curve: Float => (Float, Float)): Float => Float = {
	  this.makeContinuous(this.toArcLengthParameterArray(curve))
	}
	
	
  /**
	* Returns for a given bezier curve a function mapping any arc length parameter to its corresponding curve parameter,
	* using a numerical approximation technique.
	*/
	def toCurveParameter(curve: Float => (Float, Float)): Float => Float = {
	  this.makeContinuous(this.toCurveParameterArray(curve))
	}
	
	/**
	* Transforms an array with discrete parameter pairs into a continuous mapping between parameters using interpolation.
	* Note that this method assumes the values of the array to be increasing monotonically.
	*/
	private def makeContinuous(array: ArrayBuffer[(Float, Float)]): Float => Float = {  
	  
	  /**
	  * Returns the greatest index for which the associated value is still less than or equal to the specified value using a binary search on the given array buffer,
	  * or the greatest/lowest index if the specified value is out of range.
	  */
    def binarySearch(array: ArrayBuffer[(Float, Float)], leftIndex: Int, rightIndex: Int, value: Float): Int = {  
      val centerIndex = (leftIndex + rightIndex)/2 //find center of specified range
      if (value <= array(leftIndex)._1) leftIndex
      else if (value >= array(rightIndex)._1) rightIndex
      else if (value == array(centerIndex)._1) centerIndex
      else if (value > array(centerIndex)._1 && value < array(centerIndex+1)._1) centerIndex //else if the value is greater than the center value but less than the next value, return the center index
      else if (value > array(centerIndex)._1) binarySearch(array, centerIndex, rightIndex, value) //else if the value is even greater, keep searching
      else binarySearch(array, leftIndex, centerIndex, value) //else the value is less than the center value, keep searching, also
    }
	  
	  parameter => {
      if (array.size > 0) {
        val key = binarySearch(array, 0, array.size - 1, parameter) //the greatest key smaller than or equal to the specified parameter
        val firstParamDiff = parameter - array(key)._1
        val secondParamDiff = array(if (key+1 >= array.size) key else key+1)._2 - array(key)._2
        array(key)._2 + firstParamDiff*secondParamDiff //if the keyDiff is 0, key == parameter and thus we want to return the value for the current key, which is why no secondParamDiff is added
      }
      else { //if there are no keys, return the start point...
        0
      }	  
    }
    
	}
	
	
 
  /**
  * Curried funtion which takes an (arbitrary) bezier curve and then a curve parameter t between 0 and 1 corresponding to a point p on the curve. 
  * Supplying the method with a distance then yields a list of curve parameters whose corresponding points on the curve are approximately the specified arc distance away from p.
  */
  def pointsWithArcDistance(curve: Float => (Float, Float))(t: Float)(distance: Float): List[Float] = {
    var paramList = List[Float]()
    var currentDist = 0.0f
    
    var i: Float = t * Precision //start value based on given parameter t
    while (currentDist < distance){ //when we finally 'overshoot', stop loop
      currentDist += Vector.euclideanDistance(curve(i/Precision), curve((i+1)/Precision))
      i = i + 1.0f //positive direction
    }
    paramList = i/Precision :: paramList
    
    currentDist = 0.0f
    i = t * Precision //reset and walk in opposite direction
    while (currentDist < distance){ //when we finally 'overshoot', stop loop
      currentDist += Vector.euclideanDistance(curve(i/Precision), curve((i-1)/Precision))
      i = i - 1.0f //negative direction
    }
    paramList = i/Precision :: paramList
    
    paramList   
  }
  
	
}
