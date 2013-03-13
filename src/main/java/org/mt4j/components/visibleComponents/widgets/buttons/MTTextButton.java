/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 3 - 13 :: 11 : 41
    >>  Origin: mt4j (project) / UltraCom (module)
    >>
  +3>>
    >>  Copyright (c) 2013:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

package org.mt4j.components.visibleComponents.widgets.buttons;

import org.mt4j.components.bounds.BoundsZPlaneRectangle;
import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.interfaces.IclickableButton;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.gestureAction.DefaultButtonClickAction;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import processing.core.PApplet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MTTextButton extends MTTextArea implements IclickableButton {

    /**
     * The selected.
     */
    private boolean selected;

    /**
     * The registered action listeners.
     */
    private ArrayList<ActionListener> registeredActionListeners;


    public MTTextButton(PApplet pApplet, String label) {
        super(pApplet);
        this.setText(label);

        this.registeredActionListeners = new ArrayList<ActionListener>();

        this.setName("Unnamed image button");

        this.selected = false;

        this.setGestureAllowance(DragProcessor.class, false);
        this.setGestureAllowance(RotateProcessor.class, false);
        this.setGestureAllowance(ScaleProcessor.class, false);

        this.setEnabled(true);
        this.setBoundsBehaviour(AbstractShape.BOUNDS_ONLY_CHECK);

        //Make clickable
        this.setGestureAllowance(TapProcessor.class, true);
        this.registerInputProcessor(new TapProcessor(pApplet));
        this.addGestureListener(TapProcessor.class, new DefaultButtonClickAction(this));

        //Draw this component and its children above
        //everything previously drawn and avoid z-fighting
        this.setDepthBufferDisabled(true);
    }


    @Override
    protected void setDefaultGestureActions() {
        //Dont register the usual drag,scale,rot processors
    }


    @Override
    protected IBoundingShape computeDefaultBounds() {
        return new BoundsZPlaneRectangle(this);
    }


    /**
     * Adds the action listener.
     *
     * @param listener the listener
     */
    public synchronized void addActionListener(ActionListener listener) {
        if (!registeredActionListeners.contains(listener)) {
            registeredActionListeners.add(listener);
        }
    }

    /**
     * Removes the action listener.
     *
     * @param listener the listener
     */
    public synchronized void removeActionListener(ActionListener listener) {
        if (registeredActionListeners.contains(listener)) {
            registeredActionListeners.remove(listener);
        }
    }

    /**
     * Gets the action listeners.
     *
     * @return the action listeners
     */
    public synchronized ActionListener[] getActionListeners() {
        return registeredActionListeners.toArray(new ActionListener[this.registeredActionListeners.size()]);
    }

    /**
     * Fire action performed.
     */
    protected synchronized void fireActionPerformed() {
        System.err.println("actionPerformed");
        ActionListener[] listeners = this.getActionListeners();
        for (ActionListener listener : listeners) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "action performed on tangible button"));
        }
    }

    /**
     * fires an action event with a ClickEvent Id as its ID.
     *
     * @param ce the ce
     */
    public synchronized void fireActionPerformed(TapEvent ce) {
        System.err.println("actionPerformed TAP");
        ActionListener[] listeners = this.getActionListeners();
        for (ActionListener listener : listeners) {
            listener.actionPerformed(new ActionEvent(this, ce.getTapID(), "action performed on tangible button"));
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        //		this.setStrokeWeight(selected ? this.getStrokeWeight() + 2 : 0);
    }


}
