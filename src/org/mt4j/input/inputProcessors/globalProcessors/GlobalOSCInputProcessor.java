package org.mt4j.input.inputProcessors.globalProcessors;

import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt;
import org.mt4j.input.inputProcessors.globalProcessors.osc.GlobalOSCInputRedirectSingleton;

public class GlobalOSCInputProcessor extends AbstractGlobalInputProcessor {
	private String url;
	
	public GlobalOSCInputProcessor(String url) {
		this.url = url;
		
		GlobalOSCInputRedirectSingleton.getInstance()
								.registerGlobalOSCInputProcessor(url, this);
	}
	@Override
	public void processInputEvtImpl(MTInputEvent inputEvent) {
		
		if(inputEvent instanceof MTOSCControllerInputEvt) {
			if(((MTOSCControllerInputEvt)inputEvent)
					.getOSCMessage()
					.getName().equals(url)) {
				
					System.out.println("BUM");
					//this.fireInputEvent(inputEvent);
			}
		}
	}

}
