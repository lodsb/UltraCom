package mutator


import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle
import org.mt4j.components.visibleComponents.widgets.{TextArea, TextField, Slider}
import org.mt4j.util.math.Vector3D
import org.mt4j.components.ComponentImplicits._
import org.mt4j.types.Vec3d
import org.mt4j.util.{Color, MTColor}
import org.mt4j.components.visibleComponents.widgets.Button
import org.mt4j.util.Color._
import org.lodsb.reakt.sync.VarS

/**
 * Created by lodsb on 3/3/14.
 */
class VotingPanel(pos: Vector3D) extends MTRoundRectangle(pos.x, pos.y, pos.z, 150, 110, 10, 10, Mutator) {
  this.setPickable(true)

  var isPanelEnabled = true

  val color = Color.AQUA.opacity(0.4f)
  val disabledColor = Color.RED.opacity(0.4f)
  val buttonColor = Color.WHITE.opacity(0.6f)

  val voted = new VarS[Float](-1)

  this.setFillColor(color)

  //val text = TextField("Voting")
  //text.setFillColor(MTColor.BLACK)
  //text.setStrokeColor(color.opacity(0.0f))

  val button = Button("Vote",50,25)
  val slider = Slider(0,1, 100,30)

  slider.setFillColor(color)

  this += button ++ slider

  button.setPositionRelativeToParent(Vec3d(75, 90))
  //button.setPickable(false)
  button.setFillColor(buttonColor)

  button.pressed.observe({x =>
    if(x && isPanelEnabled) {
      this.enablePanel(false)
      voted.update(slider.value())
    }

    true
  })

  //text.setPositionRelativeToParent(Vec3d(100, 10,10))
  slider.setPositionRelativeToParent(Vec3d(75, 30))




  //text.setPickable(false)
  slider.setPickable(false)

  def enablePanel(enable : Boolean ) = {
    isPanelEnabled = enable

    if(!enable) {
      this.setFillColor(disabledColor)
      slider.setFillColor(disabledColor)
      button.setPickable(false)
    } else {
      this.setFillColor(color)
      slider.setFillColor(color)
      slider.setValue(0.5f)
      button.setPickable(true)
    }
  }

}
