package cyntersizer

import org.mt4j.util.MTColor
import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.util.math.Vertex

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  13.03.13 :: 16:36
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


class SquareForm(width: Float, color: MTColor)
  extends MTRectangle(app, new Vertex(app.center), width, width) {

  this.setFillColor(color)
}
