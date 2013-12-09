package org.mt4j.components.visibleComponents.widgets

import org.mt4j.MTApplication
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle
import org.mt4j.components.{StateChangeEvent, StateChangeListener}

/**
 * Created by lodsb on 12/9/13.
 */
class Group(app: MTApplication) extends MTRoundRectangle(app, 0f, 0f, 100f, 100f, 15f, 15f, 20 ) with StateChangeListener {

  def stateChanged(evt: StateChangeEvent): Unit = {

  }
    fillColor
}
