package ui.properties

import processing.opengl.PGraphicsOpenGL
import org.mt4j.util.MTColor

import ui.paths._


/**
* This abstract class represents a complex property.
* A complex property manages a fixed number of 'buckets', each associated with a specific value.
*/
abstract class ComplexProperty(val connection: ManipulableBezierConnection, val numberOfBuckets: Int) extends Property {
  protected var values = new Array[Float](this.buckets)
  val (min, max) = this.range
  this.values = this.values.map(_ + (max - min)/2 + min) //initialize values with mean per default
  
  /**
  * Returns the value of this property at the given index.
  */
  def apply(index: Int) = {
    this.values(index)
  }
  
  /**
  * Updates this property using the specified function.
  */
  def update(function: Int => Float) = {
    val (min, max) = this.range
    for (index <- 0 to this.values.size - 1) {
      val newValue = function(index)
      this.values(index) = math.max(math.min(max, newValue), min)
    }
  }
 
  /**
  * Returns the number of buckets of this property.
  */
  def buckets = {
    this.numberOfBuckets
  }
  
  
  /**
  * Maps the property values to a drawable range. If not overridden, this range is [0, visualWidth].
  */
  protected def mappedValues(index: Int) = {
    val (min, max) = this.range
    ((this.values(index) - min)/(max - min)) * this.visualWidth
  }
  
  /**
  * Returns the mean over all buckets of this property.
  */
  def mean: Float = {
    this.sum/this.buckets
  }
  
  /**
  * Returns the sum over all buckets of this property.
  */
  def sum: Float = {
    this.partialSum(this.buckets-1)
  }  
  
  /**
  * Returns the sum up to the specified bucket.
  */
  def partialSum(bucket: Int) = {
    this.values.take(bucket+1).foldLeft(0.0f)((a,b) => a+b)
  }
  
  override def toXML = {
    "<property type = '" + this.toString + "'><buckets number = '" + this.buckets + "'>" + this.values.zipWithIndex.map(bucket => {"<bucket index = '" + bucket._2 + "'>" + bucket._1 + "</bucket>"}).foldLeft("")((b1, b2) => b1 + " " + b2) + "</buckets></property>"
  }
  
}
