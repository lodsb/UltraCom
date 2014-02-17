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
package org.mt4j.components;

import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsLight;
import org.mt4j.util.math.Vector3D;
import processing.core.PApplet;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * The Class MTLight. Abstracts the opengl lightning.
 *
 * @author Christopher Ruff
 */
public class MTLight {

    /**
     * The light id.
     */
    private int lightId;

    /**
     * The gl.
     */
    private GL2 gl;


    /**
     * The light ambient.
     */
    private float[] lightAmbient; // scattered light

    /**
     * The light diffuse.
     */
    private float[] lightDiffuse; // direct light

    /**
     * The light specular.
     */
    private float[] lightSpecular; // highlight

    /**
     * The light position.
     */
    private float[] lightPosition; //last coord: if 1->pointlight, gegen 0:directionallight


    /**
     * Enable lightning and ambient.
     * Should be called before doing anything with light!
     * <p/>
     * - Enables gl lightning
     * <br>- sets an ambient light
     * <br>- enables color material
     * <br>- enables RESCALE_NORMAL
     *
     * @param pa       the pa
     * @param ambientR the ambient r
     * @param ambientG the ambient g
     * @param anbientB the anbient b
     * @param ambientA the ambient a
     */
    public static void enableLightningAndAmbient(PApplet pa, float ambientR, float ambientG, float anbientB, float ambientA) {
        GL2 gl = GLU.getCurrentGL().getGL2();

        //ENABLE LIGHTNING
        gl.glEnable(GL2.GL_LIGHTING);

        //Set default ambient lightning for all objs
        ToolsLight.setAmbientLight(gl, new float[]{ambientR / 255, ambientG / 255, anbientB / 255, ambientA / 255});

        //This means that glMaterial will control the polygon's specular and emission colours
        //and the ambient and diffuse will both be set using glColor.
//	    	gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
//	    	gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_DIFFUSE);

        //Enable color material
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        /*
               * GL_RESCALE_NORMAL multiplies the transformed normal by a scale factor.
               * If the original normals are unit length, and the ModelView matrix contains
               * uniform scaling, this multiplication will restore the normals to unit length.
               * If the ModelView matrix contains nonuniform scaling, GL_NORMALIZE is the
               * preferred solution.
              */
        gl.glEnable(GL2.GL_RESCALE_NORMAL);
    }


    /**
     * Instantiates a new mT light.
     *
     * @param pa       the pa
     * @param lightId  the light id
     * @param position the position
     */
    public MTLight(PApplet pa, int lightId, Vector3D position) {
        super();

        switch(lightId) {
            case 0: this.lightId = GL2.GL_LIGHT0; break;
            case 1: this.lightId = GL2.GL_LIGHT1; break;
            case 2: this.lightId = GL2.GL_LIGHT2; break;
            case 3: this.lightId = GL2.GL_LIGHT3; break;
            case 4: this.lightId = GL2.GL_LIGHT4; break;
            case 5: this.lightId = GL2.GL_LIGHT5; break;
            case 6: this.lightId = GL2.GL_LIGHT6; break;
            case 7: this.lightId = GL2.GL_LIGHT7; break;
            default: this.lightId = GL2.GL_LIGHT0;
        }

        this.gl = Tools3D.getGL(pa);

        this.lightAmbient = new float[]{.1f, .1f, .1f, 1f}; // scattered light
        this.lightDiffuse = new float[]{0.7f, 0.7f, 0.7f, 1f}; // direct light
        this.lightSpecular = new float[]{.3f, .3f, .3f, 1f};
        this.lightPosition = new float[]{position.x, position.y, position.z, 1.000f};

        this.initLight(this.lightId);
    }

    /**
     * Inits the light.
     *
     * @param lightId the light id
     */
    private void initLight(int lightId) {
        //Set the ligth with aboves settings
        ToolsLight.setLight(gl, lightId, lightDiffuse, lightAmbient, lightSpecular, lightPosition);
    }

    /**
     * Enable.
     */
    public void enable() {
        gl.glEnable(this.lightId);
    }

    /**
     * Disable.
     */
    public void disable() {
        gl.glDisable(this.lightId);
    }

    /**
     * Update light values.
     */
    public void updateLightValues() {
        initLight(this.getLightId());
    }

    /**
     * Update light position.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void updateLightPosition(float x, float y, float z) {
        this.lightPosition = new float[]{x, y, z, 1};
        gl.glLightfv(this.lightId, GL2.GL_POSITION, lightPosition, 0);
    }


    /**
     * Gets the light ambient.
     *
     * @return the light ambient
     */
    public float[] getLightAmbient() {
        return lightAmbient;
    }

    /**
     * Sets the light ambient.
     *
     * @param lightAmbient the new light ambient
     */
    public void setLightAmbient(float[] lightAmbient) {
        this.lightAmbient = lightAmbient;
    }

    /**
     * Gets the light diffuse.
     *
     * @return the light diffuse
     */
    public float[] getLightDiffuse() {
        return lightDiffuse;
    }

    /**
     * Sets the light diffuse.
     *
     * @param lightDiffuse the new light diffuse
     */
    public void setLightDiffuse(float[] lightDiffuse) {
        this.lightDiffuse = lightDiffuse;
    }

    /**
     * Gets the light position.
     *
     * @return the light position
     */
    public float[] getLightPosition() {
        return lightPosition;
    }

//	public void setLightPosition(float[] lightPosition) {
//		this.lightPosition = lightPosition;
//	}

    /**
     * Gets the light specular.
     *
     * @return the light specular
     */
    public float[] getLightSpecular() {
        return lightSpecular;
    }

    /**
     * Sets the light specular.
     *
     * @param lightSpecular the new light specular
     */
    public void setLightSpecular(float[] lightSpecular) {
        this.lightSpecular = lightSpecular;
    }

    /**
     * Gets the light id.
     *
     * @return the light id
     */
    public int getLightId() {
        return lightId;
    }


}
