package org.mt4j.input.inputData.osc;

import org.mt4j.components.IMTTargetable;
import org.mt4j.input.inputData.AbstractControllerInputEvt;

import de.sciss.net.OSCMessage;

public class MTOSCControllerInputEvt extends AbstractControllerInputEvt<OSCMessage> {

    public MTOSCControllerInputEvt(Object source, IMTTargetable target,
                                   OSCMessage controllerMessage) {
        super(source, target, controllerMessage);
    }

    public MTOSCControllerInputEvt(Object source,
                                   OSCMessage controllerMessage) {
        super(source, controllerMessage);
    }

}
