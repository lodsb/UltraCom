package org.mt4j.input.inputProcessors.globalProcessors;

import org.mt4j.components.IMTTargetable;
import org.mt4j.input.inputData.MTComponent3DInputEvent;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt;
import org.mt4j.input.inputProcessors.globalProcessors.osc.GlobalOSCInputRedirectSingleton;
import org.mt4j.sceneManagement.SimpleAbstractScene;

public class GlobalOSCInputProcessor extends AbstractGlobalInputProcessor<MTOSCControllerInputEvt> {
    private String url;

    public GlobalOSCInputProcessor(SimpleAbstractScene scene, String url) {
        super(scene);
        this.url = url;

        GlobalOSCInputRedirectSingleton.getInstance()
                .registerGlobalOSCInputProcessor(url, this);
    }

    @Override
    public void processInputEvtImpl(MTOSCControllerInputEvt inputEvent) {
        if (((MTOSCControllerInputEvt) inputEvent)
                .getControllerMessage()
                .getName().equals(url)) {

            this.fireInputEvent(inputEvent);
        }
    }
}
