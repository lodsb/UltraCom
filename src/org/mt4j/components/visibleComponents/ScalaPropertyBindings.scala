/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 2 :: 1 : 16
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.mt4j.components.visibleComponents {

import org.mt4j.util.MTColor
import org.mt4j.util.math.{Vertex, Vector3D}
import processing.core.PImage
import shapes.{MTEllipse, MTRectangle, AbstractShape}
import widgets.{MTTextArea, MTSlider, MTColorPicker}
import org.mt4j.types.Rotation
import org.mt4j.components.MTComponent

class _genSetterGetterHelper[T](set: (T) => Unit) {
	var setVal: T = _;

	def getter() = {
		{
			() => setVal
		}
	};

	def setter() = {
		x: T => setVal = x; set(x)
	}
}

object ScalaPropertyBindings {

	/*def getDummy(z: AbstractVisibleComponent) : () => Unit = {

	} */

	////////AbstractVisibleComponentMethods
	def setStrokeColor(z: AbstractVisibleComponent): (MTColor) => Unit = {
		z.setStrokeColor
	}

	def getStrokeColor(z: AbstractVisibleComponent): () => MTColor = {
		z.getStrokeColor
	}

	def setFillColor(z: AbstractVisibleComponent): (MTColor) => Unit = {
		z.setFillColor
	}

	def getFillColor(z: AbstractVisibleComponent): () => MTColor = {
		z.getFillColor
	}

	def setStrokeWeight(z: AbstractVisibleComponent): (Float) => Unit = {
		z.setStrokeWeight
	}

	def getStrokeWeight(z: AbstractVisibleComponent): () => Float = {
		z.getStrokeWeight
	}

	/////////////////////////////////////

	////////AbstractShape
	def setPositionGlobal(z: AbstractShape): (Vector3D) => Unit = {
		z.setPositionGlobal
	}

	def setPositionRelParent(z: AbstractShape): (Vector3D) => Unit = {
		z.setPositionRelativeToParent
	}

	def setVertices(z: AbstractShape): (Array[Vertex]) => Unit = {
		z.setVertices
	}

	def getVertices(z: AbstractShape): () => Array[Vertex] = {
		z.getVerticesLocal
	}

	def setTexture(z: AbstractShape): PImage => Unit = {
		z.setTexture
	}

	def getTexture(z: AbstractShape): () => PImage = {
		z.getTexture
	}

	/////////////////////////////////////

	////////MTComponent
	def setRotGlobal(z: MTComponent): (Rotation) => Unit = {
		z.rotate3DGlobal
	}

	def setRotLocal(z: MTComponent): (Rotation) => Unit = {
		z.rotate3DLocal
	}

	def setRotRel(z: MTComponent): (Rotation) => Unit = {
		z.rotate3DRelativeToParent
	}


	/////////////////////////////////////


	////////MTRectangle
	def setWidth(z: MTRectangle): Float => Unit = {
		z.setWidthLocal
	}

	def getWidth(z: MTRectangle): () => Float = {
		z.getWidthLocal
	}

	def setHeight(z: MTRectangle): Float => Unit = {
		z.setHeightLocal
	}

	def getHeight(z: MTRectangle): () => Float = {
		z.getHeightLocal
	}

	/////////////////////////////////////

	////////MTEllipse
	def setDegrees(z: MTEllipse): Float => Unit = {
		z.setDegrees
	}

	def getDegrees(z: MTEllipse): () => Float = {
		z.getDegrees
	}

	/////////////////////////////////////

	////////MTColorPicker
	def setColorPicked(z: MTColorPicker): MTColor => Unit = {
		z.setSelectedColor
	}

	def getColorPicked(z: MTColorPicker): () => MTColor = {
		z.getSelectedColor
	}

	/////////////////////////////////////

	////////MTSlider
	def setValue(z: MTSlider): Float => Unit = {
		z.setValue
	}

	def getValue(z: MTSlider): () => Float = {
		z.getValue
	}

	/////////////////////////////////////

	////////MTTextArea
	def setText(z: MTTextArea): String => Unit = {
		z.setText
	}

	def getText(z: MTTextArea): () => String = {
		z.getText
	}

	def setPadding(z: MTTextArea): Float => Unit = {
		z.setInnerPadding
	}

	def getPadding(z: MTTextArea): () => Float = {
		z.getInnerPaddingTop
	}


}

}
