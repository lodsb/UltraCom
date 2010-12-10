package org.mt4j.input.gestureAction;

import org.mt4j.input.inputProcessors.MTGestureEvent;

public interface ICollisionAction {
	public boolean gestureAborted();
	
	public void setGestureAborted(boolean aborted);
	
	public MTGestureEvent getLastEvent();
}
