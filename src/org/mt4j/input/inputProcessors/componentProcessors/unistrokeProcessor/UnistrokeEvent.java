package org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor;


import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture;

public class UnistrokeEvent extends MTGestureEvent{
	private MTPolygon visualization;
	private UnistrokeGesture gesture;

	public UnistrokeEvent(IInputProcessor source, int id, IMTComponent3D targetComponent, MTPolygon visualization, UnistrokeGesture gesture) {
		super(source, id, targetComponent);
		this.visualization = visualization;
		this.gesture = gesture;
	}

	public MTPolygon getVisualization() {
		return this.visualization;
	}
	
	public UnistrokeGesture getGesture(){
		return this.gesture;
	}
	

}
