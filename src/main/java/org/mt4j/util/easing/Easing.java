/*
Ani (a processing animation library) 
Copyright (c) 2010 Benedikt Gro�

http://www.looksgood.de/libraries/Ani

Standing on the shoulders of giants:
Jack Doyle - TweenLite AS3 Library (http://blog.greensock.com/tweenlite/)
Robert Penner - Equations (http://robertpenner.com/easing/)
Andreas Schlegel - ControlP5 (http://www.sojamo.de/libraries/);
Ekene Ijeoma - Tween Processin Library (http://www.ekeneijeoma.com/processing/tween/)

AniCore, Ani and AniSequence includes many ideas and code of the nice people above!
Thanks a lot for sharing your code with the rest of the world!

This library is free software; you can redistribute it and/or modify it under the terms 
of the GNU Lesser General Public License as published by the Free Software Foundation; 
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with this 
library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
Boston, MA 02110, USA

This is all taken from the sweet ani lib @ https://github.com/b-g/Ani or see above
*/
package org.mt4j.util.easing;

import de.looksgood.ani.AniConstants;
import org.mt4j.util.animation.EasingFunction;

/**
 * The abstract class Easing, which maps time to position.
 */
public abstract class Easing extends EasingFunction implements AniConstants {
	public int easingMode = OUT;
	
	public Easing() {
	}

	/**
	 * Calc easing: map time to position. 
	 * 
	 * @param t the time
	 * @param b the begin
	 * @param c the change
	 * @param d the duration
	 * @return the float
	 */
	public final float calcEasing(float t, float b, float c, float d){
		float out;
		switch( easingMode ){
		case IN:
			out = easeIn(t, b, c, d);
			break;
		case OUT:
		default:
			out = easeOut(t, b, c, d);
			break;
		case IN_OUT:
			out = easeInOut(t, b, c, d);
			break;
		}
		return out;
	}
	
	/**
	 * Set the shape mode
	 * @param theEasingMode IN, OUT, IN_OUT
	 */
	public final void setMode(int theEasingMode){
		easingMode = theEasingMode;
	}

}
