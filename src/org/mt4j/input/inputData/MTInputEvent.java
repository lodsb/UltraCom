package org.mt4j.input.inputData;

import org.mt4j.input.MTEvent;

public abstract class MTInputEvent<TargetType> extends MTEvent {
	/** The target component. */
	protected TargetType target;
	protected TargetType currentTarget;
	
	public MTInputEvent(Object source) {
		super(source);
	}
	
	public MTInputEvent(Object source, TargetType target) {
		super(source);
		this.target = target;
	}
	
	/**
	 * Checks if this input event has a target component.
	 * 
	 * @return true, if successful
	 */
	public boolean hasTarget(){
		return this.target != null;
	}
	
	/**
	 * Gets the target of this input event.
	 * <br><strong>NOTE:</strong> Not every event has a target component! To check this
	 * we can call <code>event.hasTarget()</code>.
	 * 
	 * @return the target component
	 */
	public TargetType getTarget() {
		return this.target;
	}
	
	/**
	 * Sets the target component of this input event. 
	 * <br>NOTE: This is supposed to be called internally by
	 * MT4j and not by users.
	 * 
	 * @param targetComponent the new target component
	 */
	public void setTarget(TargetType target) {
		this.target = target;
	}
	
	/**
     * The <code>setCurrentTarget</code> method is used by the DOM 
     * implementation to change the value of a <code>currentTarget</code> 
     * attribute on the <code>Event</code> interface.
     * @param target Specifies the <code>currentTarget</code> attribute on 
     *   the <code>Event</code> interface.
     */
    public void setCurrentTarget(TargetType target) {
    	this.currentTarget = target;
    }
    public TargetType getCurrentTarget() {
    	return this.currentTarget; 
    }
}
