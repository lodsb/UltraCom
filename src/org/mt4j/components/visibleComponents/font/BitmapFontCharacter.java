/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009 C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
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
package org.mt4j.components.visibleComponents.font;

import javax.media.opengl.GL;

import org.mt4j.components.bounds.IBoundingShape;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GLTexture;
import org.mt4j.util.opengl.GLTexture.EXPANSION_FILTER;
import org.mt4j.util.opengl.GLTexture.SHRINKAGE_FILTER;
import org.mt4j.util.opengl.GLTexture.WRAP_MODE;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * The Class BitmapFontCharacter.
 * @author Christopher Ruff
 */
public class BitmapFontCharacter extends MTRectangle implements IFontCharacter {
	
	/** The unicode. */
	private String unicode;
	
	/** The horizontal dist. */
	private int horizontalDist;

	/** The left offset. */
	private int leftOffset;
	
	
	/**
	 * Instantiates a new bitmap font character.
	 * @param applet the applet
	 * @param texture the texture
	 * @param unicode the unicode
	 * @param leftOffset the left offset
	 * @param topOffset the top offset
	 * @param horizontalAdvance the horizontal advance
	 */
	public BitmapFontCharacter(PApplet applet, PImage texture, String unicode, int leftOffset, int topOffset, int horizontalAdvance) {
		super(applet, new Vertex(leftOffset, topOffset,0), texture.width, texture.height);
		//hm..this is for the card loading, because
		//when we init gl texture in other thread it breaks..
//		this.setUseDirectGL(false);
//		this.setUseDirectGL(true);
		
		this.setTexture(texture);
		this.setTextureEnabled(true);
		
		this.leftOffset = leftOffset;
		this.horizontalDist = horizontalAdvance;
		this.unicode = unicode;
		
		this.setNoStroke(true); 
		this.setPickable(false);
		
		if (MT4jSettings.getInstance().isOpenGlMode()){
			//Set the texture to be non-repeating but clamping to the border to avoid artefacts
			PImage tex = this.getTexture();
			if (tex instanceof GLTexture) {
				GLTexture glTex = (GLTexture) tex;
//				glTex.setWrap(GL.GL_CLAMP, GL.GL_CLAMP);
//				glTex.setWrap(GL.GL_CLAMP_TO_EDGE, GL.GL_CLAMP_TO_EDGE);
				
				glTex.setWrapMode(WRAP_MODE.CLAMP_TO_EDGE, WRAP_MODE.CLAMP_TO_EDGE);
				
//				glTex.setFilter(SHRINKAGE_FILTER.Trilinear, EXPANSION_FILTER.Bilinear);
				glTex.setFilter(SHRINKAGE_FILTER.BilinearNoMipMaps, EXPANSION_FILTER.Bilinear);
//				glTex.setFilter(SHRINKAGE_FILTER.NearestNeighborNoMipMaps, EXPANSION_FILTER.NearestNeighbor);
//				glTex.setFilter(SHRINKAGE_FILTER.BilinearNoMipMaps, EXPANSION_FILTER.NearestNeighbor);
			}
		}
	}
	
	

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFontCharacter#drawComponent(javax.media.opengl.GL)
	 */
	//@Override
	public void drawComponent(GL gl) { //FIXME
//		this.drawPureGl(gl);
//		/*
		if (MT4jSettings.getInstance().isOpenGlMode()){
			if (this.isUseDisplayList() && this.getGeometryInfo().getDisplayListIDs()[0] != -1){
				gl.glCallList(this.getGeometryInfo().getDisplayListIDs()[0]);
//				gl.glCallList(this.getGeometryInfo().getDisplayListIDs()[1]); //Outline rectangle
			}else{
				this.drawPureGl(gl);
			}
		}
//		*/
	}
	
	
	@Override
	protected void setDefaultGestureActions() {
		//no gestures
	}
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.shapes.MTRectangle#computeDefaultBounds()
	 */
	//@Override
	protected IBoundingShape computeDefaultBounds() {
		//We assume that font characters never get picked or anything 
		//and hope the creation speeds up by not calculating a bounding shape
		return null;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFontCharacter#getHorizontalDist()
	 */
	//@Override
	public int getHorizontalDist() {
		return this.horizontalDist;
	}
	
	/**
	 * Sets the horizontal dist.
	 * 
	 * @param horizontalDist the new horizontal dist
	 */
	public void setHorizontalDist(int horizontalDist) {
		this.horizontalDist = horizontalDist;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFontCharacter#getUnicode()
	 */
	//@Override
	public String getUnicode() {
		return this.unicode;
	}

	/**
	 * Sets the unicode.
	 * 
	 * @param unicode the new unicode
	 */
	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}
	
	
	public int getLeftOffset() {
		return this.leftOffset;
	}


	//FIXME TEST
	public void setTextureFiltered(boolean scalable) {
		if (MT4jSettings.getInstance().isOpenGlMode()){
			PImage tex = this.getTexture();
			if (tex instanceof GLTexture) {
				GLTexture glTex = (GLTexture) tex;
				//normally we would use GL_LINEAR as magnification filter but sometimes
				//small text is too filtered and smudged so we use NEAREST -> but this makes
				//scaled text very ugly and pixelated..
				if (scalable){
//					glTex.setFilter(GL.GL_LINEAR, GL.GL_LINEAR);
					glTex.setFilter(SHRINKAGE_FILTER.BilinearNoMipMaps, EXPANSION_FILTER.Bilinear);
				}else{
//					glTex.setFilter(GL.GL_LINEAR, GL.GL_NEAREST); 
					glTex.setFilter(SHRINKAGE_FILTER.BilinearNoMipMaps, EXPANSION_FILTER.NearestNeighbor);
				}
			}
		}
	}

}
