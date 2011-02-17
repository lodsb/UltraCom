package basic.helloWorld

import org.mt4j.util.math.Vector3D
import org.mt4j.components.visibleComponents.widgets.MTTextArea
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.components.visibleComponents.font.IFont
import org.mt4j.input.inputSources.osc.OSCInputSource
import org.mt4j.util.MTColor
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt
import org.mt4j.MTApplication
import org.mt4j.eventSystem.TrEventListener
import org.mt4j.sceneManagement.SimpleAbstractScene
import org.mt4j.input.IMTInputEventListener
import org.mt4j.input.inputProcessors.globalProcessors.{AbstractGlobalInputProcessor, GlobalOSCInputProcessor, CursorTracer}
import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.components.interfaces.IMTComponent3D


class HelloWorldScene2(mtApplication: MTApplication, name: String)
  extends SimpleAbstractScene(mtApplication, name)
  with TrEventListener[MTOSCControllerInputEvt] {

  val clazz = this.getClass
  var white = new MTColor(255, 255, 255);
  this.setClearColor(new MTColor(146, 150, 188, 255));
  //Show touches
  var tracer = new CursorTracer(this, mtApplication);
  var proc = new GlobalOSCInputProcessor(this,"/test");

  //this.registerGlobalInputProcessor(proc);

  var fontArial = FontManager.getInstance().createFont(mtApplication, "arial",
    150, //Font size
    white, //Font fill color
    white);
  //Font outline color
  //Create a textfield
  var textField = new MTTextArea(mtApplication); //, fontArial);

  textField.setNoStroke(true);
  textField.setNoFill(true);

  textField.setText("BITSCH");
  //Center the textfield on the screen

  textField.setPositionGlobal(new Vector3D(mtApplication.width / 2f, mtApplication.height / 2f));
  //Add the textfield to our canvas
  this.getCanvas().addChild(textField);

  proc.register({(e:MTOSCControllerInputEvt) => textField.setText(e.getControllerMessage().getArg(0).toString()); true});

  override def processEvent(sdf: MTOSCControllerInputEvt): Boolean = {
    textField.setText(sdf.getControllerMessage.getArg(0).toString());
    true
  };

  private val serialVersionUID = 1L;
}

object Test extends MTApplication {

  def main(args: Array[String]): Unit = {
    this.execute(false)
  }

  override def startUp() = {
    System.out.println("SADSDFSDF");
    this.addScene(new HelloWorldScene2(this, "FPPPP"))
  }

}