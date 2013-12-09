package org.mt4j.components.visibleComponents.font

import org.mt4j.util.MTColor
import org.mt4j.MTApplication
import java.io.File

/**
 * Created by lodsb on 12/9/13.
 */
object Font {
  def apply(s: String) : IFont = {
    val f = new File(s).getAbsolutePath;
    val app = MTApplication.getInstance()
    FontManager.getInstance().createFont(app, f, 24, MTColor.WHITE, MTColor.WHITE);
  }
  def apply() : IFont = {
    val app = MTApplication.getInstance()
    FontManager.getInstance().getDefaultFont(app)
  }
}
