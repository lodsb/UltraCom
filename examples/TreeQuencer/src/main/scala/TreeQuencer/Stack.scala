package main.scala.TreeQuencer

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  24.06.13 :: 17:39
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
 
 
 class FloatStack(val size: Int) {
  private val floats = new Array[Float](size)
  for (i<-1 to floats.size-1) floats(i) = 0
  def isZero: Boolean = {
    for (i<-1 to size-1){
      if (floats(i)!=0) {
        return false
      }
    }
    true
  }
  def push(value: Float) {
    for (i <- 1 to size-1) {
      if (i>0) {
        floats(i-1) = floats(i)
      }
    }
    floats(size-1) = value
  }
  def max: Float = {
    floats.max
  }
}
