package org.mt4j.input.inputData;

import org.mt4j.components.IMTTargetable;

public abstract class AbstractControllerInputEvt<T> extends MTInputEvent<IMTTargetable> {
    protected T controllerMessage;

    public AbstractControllerInputEvt(Object source, IMTTargetable target, T controllerMessage) {
        super(source, target);
        this.controllerMessage = controllerMessage;
    }

    public AbstractControllerInputEvt(Object source, T controllerMessage) {
        super(source);
        this.controllerMessage = controllerMessage;
    }

    public T getControllerMessage() {
        return this.controllerMessage;
    }


}
