package basic.helloWorld

import org.mt4j.input.inputProcessors.globalProcessors.GlobalOSCInputProcessor
import org.mt4j.util.math.Vector3D
import org.mt4j.components.visibleComponents.widgets.MTTextArea
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.components.visibleComponents.font.IFont
import org.mt4j.input.inputSources.osc.OSCInputSource
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer
import org.mt4j.util.MTColor
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt
import org.mt4j.MTApplication
import org.mt4j.sceneManagement.AbstractScene
import org.mt4j.input.IMTInputEventListener


class HelloWorldScene2(mtApplication: MTApplication, name: String)
						extends AbstractScene(mtApplication, name) 
						with IMTInputEventListener[MTOSCControllerInputEvt]{
	
    val clazz = this.getClass
		var white = new MTColor(255,255,255);
		this.setClearColor(new MTColor(146, 150, 188, 255));
		//Show touches
		//this.registerGlobalInputProcessor(new CursorTracer(mtApplication, this));
		//mtApplication.getInputManager().registerInputSource(new OSCInputSource(mtApplication));
		
		var proc = new GlobalOSCInputProcessor("/test");
		proc.addProcessorListener(this);
		
		//this.registerGlobalInputProcessor(proc);
		
		/*var fontArial = FontManager.getInstance().createFont(mtApplication, "arial.ttf",
				50, 	//Font size
				white,  //Font fill color
				white);	//Font outline color
		*///Create a textfield
		var textField = new MTTextArea(mtApplication);//, fontArial);
		
		textField.setNoStroke(true);
		textField.setNoFill(true);
		
		textField.setText("Hello World!");
		//Center the textfield on the screen
		textField.setPositionGlobal(new Vector3D(mtApplication.width/2f, mtApplication.height/2f));
		//Add the textfield to our canvas
		this.getCanvas().addChild(textField);

  override def processInputEvent(sdf: MTOSCControllerInputEvt )  = { true};

     private val serialVersionUID = 1L;
}

object Test extends MTApplication {
	
	def main(args: Array[String]) : Unit = {
		this.execute(false)
	}

  override def startUp() = {
    System.out.println("SADSDFSDF");
    this.addScene(new HelloWorldScene2(this, "FPPPP"))
  }
	
}