package org.mt4j.util

import org.mt4j.components.MTComponent

object Util {
  protected def zSort(components: Array[MTComponent]) = {
    components.sortBy({ comp =>
      comp.getCenterPointGlobal.z
    })
  }
}
