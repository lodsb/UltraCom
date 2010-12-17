package org.mt4j.input.inputData.osc;

import org.mt4j.input.inputData.AbstractControllerInputEvt;

import de.sciss.net.OSCMessage;

public class MTOSCControllerInputEvt extends AbstractControllerInputEvt {
	private OSCMessage msg;
	public MTOSCControllerInputEvt(Object source, OSCMessage msg) {
		super(source);
		this.msg = msg;
	}
	
	public OSCMessage getOSCMessage() {
		return this.msg;
	}

}
