package cyntersizer

import org.mt4j.util.MTColor
import org.mt4j.components.visibleComponents.shapes.MTEllipse

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  17.03.13 :: 17:11
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 * ^     ^
 * ^   ^
 * (o o)
 * {  |  }                  (Wong)
 * "
 *
 * Don't eat the pills!
 */
class CircleForm(radius: Float, color: MTColor)
  extends MTEllipse(app, app.center, radius, radius) {

  this.setFillColor(color)
}
