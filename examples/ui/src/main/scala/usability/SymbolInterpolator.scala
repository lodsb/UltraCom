package ui.usability

abstract class Symbol {
  
  /**
  * Curried function which first takes a 'center' point and a circumscribing 'radius' defining the position and size of the symbol.
  * Specifying a 'parameter' between 0 and 1 then yields the point on the outline of the symbol.
  * 
  * @throws IllegalArgumentException if the parameter is out of range
  */
  def apply(center: (Float, Float), radius: Float)(parameter: Float): (Float, Float)
}

object Rectangle extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val r =  radius/math.sqrt(2).toFloat
    val param = (parameter - 0.125f + 1.0f)%1.0f //setting start point
    
    if (param >= 0.0f && param < 0.25f) {
      (cx - r + 8.0f*param*r,   //from -r to r
      cy - r)
    }
    else if (param >= 0.25f && param < 0.5f) {
      (cx + r, 
      cy - r + 8.0f*(param-0.25f)*r)  //from -r to r
    }
    else if (param >= 0.5f && param < 0.75f) {
      (cx + r - 8.0f*(param-0.50f)*r,  //from r to -r
      cy + r)
    }
    else if (param >= 0.75f && param <= 1.0f) {
      (cx - r, 
      cy + r - 8.0f*(param-0.75f)*r)  //from r to -r
    }
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.") 
    
  }
}

object Circle extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val param = (parameter - 0.5f + 1.0f)%1.0f //setting start point
    
    if (param >= 0 && param <= 1) {
      (cx + radius * math.cos(2*math.Pi*param).toFloat, cy + radius * math.sin(2*math.Pi*param).toFloat)
    }
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.")  
  }
}


object Triangle extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val param = (parameter - 1/6.0f + 1.0f)%1.0f //setting start point    
  
    //defining an equilateral triangle circumscribed by a circle of radius 'radius' centered around (cx, cy)
    val a = (3 * radius/math.sqrt(3)).toFloat //side length of triangle
    val h = (math.sqrt(3)/2 * a).toFloat //height of triangle
    val segment = (math.cos(math.toRadians(60)) * radius).toFloat //trigonometric function to calculate the length of one of the segments induced by the orthocenter of the triangle; here, the greater of the two segment lengths is obtained
  
    if (param  >= 0.0f && param  < 1/3.0f) {
      (cx - segment + 3*param *h, //from +0 to +h
      cy - a/2 + 3*param *(a/2)) //from -a/2 to -0
    }
    else if (param  >= 1/3.0f && param  < 2/3.0f) {
      (cx - segment + h - 3*(param -1/3.0f)*h, //from +h to +0
      cy + 3*(param -1/3.0f)*(a/2)) //from +0 to +a/2
    }
    else if (param  >= 2/3.0f && param  <= 1.0f) {
      (cx - segment, //stays same
      cy + a/2 - 6*(param -2/3.0f)*(a/2)) //from +a/2 to -a/2
    }
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.") 
  }
}

object Speaker extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val r = 0.85f * radius //value 0.85f based solely on what looks good, do not try do derive ;)
    val param = (parameter - 7/12.0f + 1.0f)%1.0f //setting start point 
    
    if (param >= 0.0f && param < 1/6.0f) {
      (cx + r - 6*param*r, //from +r to +0
      cy + r/2) //stays same
    }
    else if (param >= 1/6.0f && param < 2/6.0f) {
      (cx - 6*(param-1/6.0f)*r, //from -0 to -r
      cy + r/2 + 6*(param-1/6.0f)*(r/2)) //from +r/2 to +r
    }
    else if (param >= 2/6.0f && param < 3/6.0f) {
      (cx - r, //stays same
      cy + r - 12*(param-2/6.0f)*r) //from +r to -r
    }
    else if (param >= 3/6.0f && param < 4/6.0f) {
      (cx - r + 6*(param-3/6.0f)*r, //from -r to -0
      cy - r + 6*(param-3/6.0f)*(r/2)) //from -r to -r/2
    }
    else if (param >= 4/6.0f && param < 5/6.0f) {
      (cx + 6*(param-4/6.0f)*r, //from +0 to +r
      cy - r/2) //stays same
    }
    else if (param >= 5/6.0f && param <= 1.0f) {
      (cx + r, //stays same
      cy - r/2 + 6*(param-5/6.0f)*r) //from -r/2 to +r/2
    }
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.")   
  }
}


object Pause extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val r =  radius/math.sqrt(2).toFloat
    val param = (parameter - 1/24.0f + 1.0f)%1.0f
    
    if (param >= 0.0f && param < 1/12.0f) {
      (cx - r + 16.0f*(param-0.0f)*r/3,   //from -r to -r/3
      cy - r)
    }
    else if (param >= 1/12f && param < 2/12f) {
      (cx - r/3, 
      cy - r + 12f*(param-1/12f)*r)  //from -r to +0
    }
    else if (param >= 2/12f && param < 3/12f) { //switch over to second part of symbol
      (cx - r/3 + 24f*(param-2/12f)*r/3,  //from -r/3 to +r/3
      cy)
    }    
    else if (param >= 3/12f && param < 4/12f) {
      (cx + r/3,
      cy - 12f*(param-3/12f)*r) //from +0 to -r
    }     
    else if (param >= 4/12f && param < 5/12f) {
      (cx + r/3 + 24f*(param-4/12f)*r/3,  //from +r/3 to +r
      cy - r)
    }   
    else if (param >= 5/12f && param < 6/12f) {
      (cx + r, 
      cy - r + 24f*(param-5/12f)*r)  //from -r to +r
    }
    else if (param >= 6/12f && param < 7/12f) {
      (cx + r - 24f*(param-6/12f)*r/3,   //from +r to +r/3
      cy + r)
    }
    else if (param >= 7/12f && param < 8/12f) {
      (cx + r/3,
      cy + r - 12f*(param-7/12f)*r) //from +r to +0
    }       
    else if (param >= 8/12f && param < 9/12f) { //switch back to first part of symbol
      (cx + r/3 - 24f*(param-8/12f)*r/3,  //from +r/3 to -r/3
      cy)
    } 
    else if (param >= 9/12f && param < 10/12f) {
      (cx - r/3, 
      cy + 12.0f*(param-9/12f)*r)  //from 0 to +r
    }  
    else if (param >= 10/12f && param < 11/12f) {
      (cx - r/3 - 24f*(param-10/12f)*r/3,   //from -r/3 to -r
      cy + r)
    }
    else if (param >= 11/12f && param <= 1.0f) {
      (cx -r, 
      cy + r - 24f*(param-11/12f)*r)  //from +r to -r
    }  
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.") 
    
  }
}


object ConvexRectangle extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val r =  radius/math.sqrt(2).toFloat
    val param = (parameter - 0.125f + 1.0f)%1.0f //setting start point
    
    if (param >= 0.0f && param < 0.25f) {
      (cx - r + 8.0f*param*r,   //from -r to r
      cy - r)
    }
    else if (param >= 0.25f && param < 0.375f) {
      (cx + r - 8.0f*(param-0.25f)*r, //from r to 0
      cy - r + 8.0f*(param-0.25f)*r)  //from -r to 0
    }
    else if (param >= 0.375f && param < 0.5f) {
      (cx + 8.0f*(param-0.375f)*r, //from 0 to +r
      cy + 8.0f*(param-0.375f)*r)  //from 0 to +r
    }    
    else if (param >= 0.5f && param < 0.75f) {
      (cx + r - 8.0f*(param-0.50f)*r,  //from r to -r
      cy + r)
    }
    else if (param >= 0.75f && param <= 1.0f) {
      (cx - r, 
      cy + r - 8.0f*(param-0.75f)*r)  //from r to -r
    }
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.") 
    
  }
}


object Repeat extends Symbol {
  override def apply(center: (Float, Float), radius: Float)(parameter: Float) = {
    val (cx, cy) = (center._1, center._2)
    val r =  radius/math.sqrt(2).toFloat
    val param = (parameter - 0.125f + 1.0f)%1.0f //setting start point
    
    if (param >= 0.0f && param < 0.125f) {
      (cx - r + 16.0f*param*r,   //from -r to r
      cy - r)
    }
    else if (param >= 0.125f && param < 0.25f) {
      (cx + r, 
      cy - r + 16.0f*(param-0.125f)*r)  //from -r to r
    }
    else if (param >= 0.25f && param < 0.25f + 1/24f) {
      (cx + r - 24f*(param-0.25f)*r,  //from r to 0
      cy + r)
    }  
    else if (param >= 0.25f + 1/24f && param < 0.25f + 2/24f) {
      (cx,
      cy + r + 12.0f*(param - (0.25f + 1/24f))*r) //from +r to +1.5r
    }   
    else if (param >= 0.25f + 2/24f && param < 0.25f + 3/24f) {
      (cx - 12.0f*(param - (0.25f + 2/24f))*r, //from 0 to -0.5r
      cy + 1.5f*r - 18.0f*(param - (0.25f + 2/24f))*r) //from +1.5r to +0.75r
    }     
    else if (param >= 0.25f + 3/24f && param < 0.25f + 4/24f) {
      (cx - 0.5f*r + 12.0f*(param-(0.25f + 3/24f))*r, //from -0.5r to 0
      cy + 0.75f*r - 18.0f*(param - (0.25f + 3/24f))*r) //from +0.75r to 0
    }  
    else if (param >= 0.25f + 4/24f && param < 0.25f + 5/24f) {
      (cx,
      cy + 12.0f*(param - (0.25f + 4/24f))*r) //from 0 to +0.5r
    }       
    else if (param >= 0.25f + 5/24f && param < 0.5f) {
      (cx + 12.0f*(param-(0.25f + 5/24f))*r, //from 0 to 0.5r
      cy + 0.5f*r)
    }  
    else if (param >= 0.5f && param < 0.625f) {
      (cx + 0.5f*r,
      cy + 0.5f*r - 8.0f*(param-0.5f)*r) //from +0.5r to -0.5r
    } 
    else if (param >= 0.625f && param < 0.75f) {
      (cx + 0.5f*r - 8.0f*(param-0.625f)*r, //from 0.5r to -0.5r
      cy - 0.5f*r)
    }     
    else if (param >= 0.75f && param < 0.875f - 1/32f) {
      (cx - 0.5f*r, 
      cy - 0.5f*r + 48/3f*(param-0.75f)*r) //from -0.5r to +r
    }     
    else if (param >= 0.875 - 1/32f && param < 0.875f) {
      (cx - 0.5f*r - 16.0f*(param-(0.875f - 1/32f))*r, //from -0.5r to -r
      cy + r)
    }      
    else if (param >= 0.875f && param <= 1.0f) {
      (cx - r, 
      cy + r - 16.0f*(param-0.875f)*r)  //from r to -r
    }
    else throw new IllegalArgumentException("Parameter must be between 0 and 1.") 
    //what a nightmare this symbol is to define ;)...
  }
}



/**
* This object interpolates between different symbols, e.g. triangle, rectangle, circle, speaker...
*/
object SymbolInterpolator {
  
  /**
  * Returns for two given symbol functions and a 'parameter' between 0 and 1 an interpolated point using the specified interpolation value.
  * To elaborate on this, an interpolation value of 0 will simply yield the point of the first symbol corresponding to the given parameter, 
  * while a value of 1 will yield the point of the second symbol corresponding to the given parameter.
  * Everything inbetween 0 and 1 thus yields an interpolated point.
  *
  * @throws IllegalArgumentException if the parameter or the interpolation value are out of range
  */
  def interpolate(firstSymbol: Float => (Float, Float), secondSymbol: Float => (Float, Float), parameter: Float, interpolationValue: Float) = {
    if (interpolationValue >= 0 && interpolationValue <= 1) {
      val (firstX, firstY) = firstSymbol(parameter)
      val (secondX, secondY) = secondSymbol(parameter)
      ((1 - interpolationValue)*firstX + interpolationValue*secondX, 
       (1 - interpolationValue)*firstY + interpolationValue*secondY)
    }
    else throw new IllegalArgumentException("Parameter and interpolation value must be between 0 and 1.") 
  }
  
  
}
