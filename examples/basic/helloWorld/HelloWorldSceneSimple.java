package basic.helloWorld;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.input.inputProcessors.globalProcessors.GlobalOSCInputProcessor;
import org.mt4j.input.inputSources.osc.OSCInputSource;
import org.mt4j.sceneManagement.SimpleAbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;

public class HelloWorldSceneSimple extends SimpleAbstractScene implements IMTInputEventListener<MTOSCControllerInputEvt> {

    private MTTextArea textField;

    public HelloWorldSceneSimple(MTApplication mtApplication, String name) {
        super(mtApplication, name);

        MTColor white = new MTColor(255, 255, 255);
        this.setClearColor(new MTColor(146, 150, 188, 255));
        //Show touches
        //this.registerGlobalInputProcessor(new CursorTracer(mtApplication, this));
        mtApplication.getInputManager().registerInputSource(new OSCInputSource(mtApplication));
        GlobalOSCInputProcessor proc = new GlobalOSCInputProcessor(this,"/test");
        proc.addProcessorListener(this);

        this.registerGlobalInputProcessor(proc);

        /*IFont fontArial = FontManager.getInstance().createFont(mtApplication, "arial.ttf",
                  50, 	//Font size
                  white,  //Font fill color
                  white);	//Font outline color
          //Create a textfield
          */
        //textField = new MTTextArea(mtApplication, fontArial);
        textField = new MTTextArea(mtApplication);

        textField.setNoStroke(true);
        textField.setNoFill(true);

        textField.setText("Hello World!");
        //Center the textfield on the screen
        textField.setPositionGlobal(new Vector3D(mtApplication.width / 2f, mtApplication.height / 2f));
        //Add the textfield to our canvas
        this.getCanvas().addChild(textField);
    }

    @Override
    public void init() {
    }

    @Override
    public void shutDown() {
    }

    public boolean processInputEvent(MTOSCControllerInputEvt inEvt) {
        textField.setText("Hello World " + inEvt.getControllerMessage().getArg(0));
        return true;
    }
}
