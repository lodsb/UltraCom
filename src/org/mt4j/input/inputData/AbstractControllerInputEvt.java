package org.mt4j.input.inputData;

public class AbstractControllerInputEvt extends MTInputEvent {
	private IControllerData data;
	
	public AbstractControllerInputEvt(Object source, IControllerData data) {
		super(source);
		this.data = data;
	}
	
	

}
