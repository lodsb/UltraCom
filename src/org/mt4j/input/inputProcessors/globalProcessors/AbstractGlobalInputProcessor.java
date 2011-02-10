/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009, C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *  
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package org.mt4j.input.inputProcessors.globalProcessors;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.components.IMTTargetable;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.input.GlobalInputProcessorVisitor;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTComponent3DInputEvent;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.GestureUtils;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputSources.IInputSourceListener;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsGeometry;
import org.mt4j.util.math.Vector3D;


import processing.core.PApplet;


/**
 * The Class AbstractInputprocessor.
 * 
 * @author Christopher Ruff
 */
public abstract class AbstractGlobalInputProcessor<T extends MTInputEvent> implements IInputSourceListener<T>, IInputProcessor<T> {
	protected static final Logger logger = Logger.getLogger(AbstractGlobalInputProcessor.class.getName());
	static{
		logger.setLevel(Level.ERROR);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	/** if disabled. */
	private boolean disabled;
	
	private List<IMTInputEventListener<T>> inputListeners;
	
	/**
	 * Instantiates a new abstract input processor.
	 */
	public AbstractGlobalInputProcessor() {
		this.disabled 	= false;
//		this.gestureListeners = new ArrayList<IMTEventListener>();
		inputListeners = new ArrayList<IMTInputEventListener<T>>();
	} 


	/*
	 * Std Impl searches for method with matching signature for Event of type T
	 * 
	 * 
	 */
	private HashMap<Class<?>, Boolean> eventClassMap = 
											new HashMap<Class<?>, Boolean>();
    protected boolean canHandleEvent(MTInputEvent e) {
    	Boolean ret = null;
    	
    	ret = this.eventClassMap.get(e.getClass());
    	
    	if(ret == null) {
    		for (Method m : this.getClass().getDeclaredMethods()) {
    			if (m.getName().equals("processInputEvtImpl")) {
    				Class<?>[] params = m.getParameterTypes();
    				
    				if (!this.eventClassMap.containsKey(e.getClass()) && 
    					!params[0].getName().equals(e.getClass().getName())) {
    					this.eventClassMap.put(e.getClass(), false);
    				}
    				this.eventClassMap.put(params[0], true);
     			}
    		}
    	}
    	
    	ret = this.eventClassMap.get(e.getClass());
    	
    	return ret;
    }
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputSources.IinputSourceListener#processInputEvent(org.mt4j.input.test.MTInputEvent)
	 */
	public boolean processInputEvent(T inputEvent){
		try {
			if(this.canHandleEvent(inputEvent)) {
				this.processInputEvtImpl(inputEvent);
			}
		} catch (Exception e) {
			
			System.err.println(e.getStackTrace());
			System.err.println("--- SHIT ! ---");
		}
			
			return true;
//		}
	}
	
	
	/**
	 * Process input evt implementation.
	 * 
	 * @param inputEvent the input event
	 */
	abstract public void processInputEvtImpl(T inputEvent);
	
	
	//FIXME disabled property isnt honored anywhere anymore!
	/**
	 * Checks if is disabled.
	 * 
	 * @return true, if is disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Sets the disabled.
	 * 
	 * @param disabled the new disabled
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}


	/**
	 * Adds the processor listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void addProcessorListener(IMTInputEventListener<T> listener){
		if (!inputListeners.contains(listener)){
			inputListeners.add(listener);
		}
		
	}
	
	/**
	 * Removes the processor listener.
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeProcessorListener(IMTInputEventListener<T> listener){
		if (inputListeners.contains(listener)){
			inputListeners.remove(listener);
		}
	}
	
	/**
	 * Gets the processor listeners.
	 * 
	 * @return the processor listeners
	 */
	public synchronized IMTInputEventListener<T>[] getProcessorListeners(){
		return inputListeners.toArray(new IMTInputEventListener[this.inputListeners.size()]);
	}
	
	/**
	 * Fire gesture event.
	 *
	 * @param ie the ie
	 */
	protected void fireInputEvent(T ie) {
		for (IMTInputEventListener<T> listener : inputListeners){
			listener.processInputEvent(ie);
		}
	}
	
	////
	/**
	 * Gets the intersection point of a cursor and a specified component.
	 * Can return null if the cursor doesent intersect the component.
	 *
	 * @param app the app
	 * @param c the c
	 * @return the intersection
	 */
	public Vector3D getIntersection(PApplet app, InputCursor c){
		return GestureUtils.getIntersection(app, c.getTarget(), c);
	}
	
	/**
	 * Gets the intersection point of a cursor and a specified component.
	 * Can return null if the cursor doesent intersect the component.
	 *
	 * @param app the app
	 * @param component the component
	 * @param c the c
	 * @return the intersection
	 */
	public Vector3D getIntersection(PApplet app, IMTComponent3D component, InputCursor c){
		return GestureUtils.getIntersection(app, component, c);
	}
	
	public Vector3D getPlaneIntersection(PApplet app, Vector3D planeNormal, Vector3D pointInPlane, InputCursor c){
		return GestureUtils.getPlaneIntersection(app, planeNormal, pointInPlane, c);
	}
	///////////
}
