/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 10 - 16 :: 11 : 7
    >>  Origin: mt4j (project) / prototaip (module)
    >>
  +3>>
    >>  Copyright (c) 2012:
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

package prototaip

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import processing.core.{PConstants, PApplet, PImage}
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.{DragEvent, DragProcessor}
import collection.mutable
import org.mt4j.util.math.{Tools3D, Vector3D}
import org.mt4j.input.inputProcessors.{MTGestureEvent, IGestureEventListener}
import org.mt4j.components.visibleComponents.widgets.MTColorPicker
import org.mt4j.components.visibleComponents.widgets
import org.mt4j.components.MTComponent
import org.lodsb.reakt.property.Attribute


class TimbreSurface(applet: PApplet, filename: String, x: Int, y: Int, w: Int, h: Int)
	extends MTRectangle(applet,x,y,w,h) 	{

	val soundPicked:Attribute[Tuple2[Int, String]] = new Attribute[(Int, String)]("soundPicked", (-1, "none"));

	val dataSet = loadTimbreSurfaceDataset(filename)
	val latentSize = dataSet._2
	val audioFileName = dataSet._1
	val coordinates = dataSet._5;
	val latentDim = scala.math.sqrt(latentSize).toInt;

	val backgroundTexture = new PImage(w,h)
	calcColors
	setTexture(backgroundTexture)


	removeAllGestureEventListeners(classOf[DragProcessor])




	this.addGestureListener(classOf[DragProcessor], new IGestureEventListener {
			def processGestureEvent(ge: MTGestureEvent): Boolean = {
				val de: DragEvent = ge.asInstanceOf[DragEvent]

				val hitPoint:Vector3D = TimbreSurface.this.localHitPoint(de.getDragCursor.getCurrentEvtPosX, de.getDragCursor.getCurrentEvtPosY)

				if(hitPoint != null) {
					val latentIndex = localCoordsToLatentIdx(hitPoint.getX, hitPoint.getY)

					coordinates.get(latentIndex) match {
						case Some(list) => {
							list.foreach( x => {
									soundPicked() = (x._1,audioFileName);
							})
						}
						case _ => ()
					}

				}




				return false
			}
		})


	private def localCoordsToLatentIdx(x: Float, y:Float ) : Int = {
		val latentIdx = ((x/w.toFloat)*latentDim + (scala.math.max((y/h.toFloat)*latentSize-latentDim,0))).toInt
		scala.math.min(latentIdx,latentSize)
	}

	/**
		 * Sets the gradient.
		 *
		 * @param x  the x
		 * @param y  the y
		 * @param w  the w
		 * @param h  the h
		 * @param c1 the c1
		 * @param c2 the c2
		 */
		private def setGradient(x: Int, y: Int, w: Float, h: Float, c1: Int, c2: Int) {
			val deltaR: Float = applet.red(c2) - applet.red(c1)
			val deltaG: Float = applet.green(c2) - applet.green(c1)
			val deltaB: Float = applet.blue(c2) - applet.blue(c1)

				var j: Int = y
				while (j < (y + h)) {
					{   val ca = 255f;//150f
						val c: Int = applet.color(applet.red(c1) + (j - y) * (deltaR / h), applet.green(c1) + (j - y) * (deltaG / h), applet.blue(c1) + (j - y) * (deltaB / h),ca)
						backgroundTexture.set(x, j, (c/(j+1)) )
					}
					j += 1; j
				}

		}

		/**
		 * Calc colors.
		 */
		private def calcColors {
			val cw: Int = w

				var i: Int = 0
				while (i < cw) {
					{
						val nColorPercent: Float = i / cw.asInstanceOf[Float]
						val rad: Float = (-360 * nColorPercent) * (PConstants.PI / 180)
						val nR: Int = ((PApplet.cos(rad) * 127 + 128).asInstanceOf[Int] << 16)
						val nG: Int = ((PApplet.cos(rad + 2 * scala.math.Pi.asInstanceOf[Float] / 3) * 127 + 128).asInstanceOf[Int] << 8)
						val nB: Int = (Math.cos(rad + 4 * scala.math.Pi  / 3) * 127 + 128).asInstanceOf[Int]
						val nColor: Int = nR | nG | nB
						setGradient(i, 0, 1, h / 2, 0xFFFFFF, nColor)
						setGradient(i, (h / 2), 1, h / 2, nColor, 0x000000)
					}
					({
						i += 1; i
					})
				}

			val dimIncY = (h/latentDim);
			val c = applet.color(90f,90f,90f);

			for(yLine <- 1 to latentSize ) {
				val yCoord = yLine*dimIncY

				for(xCoord <- 0 to w) {

					backgroundTexture.set(xCoord, yCoord, c);
				}
			}

			val dimIncX = (w/latentDim);
			for(xLine <- 1 to latentSize ) {
				val xCoord = xLine*dimIncX

				for(yCoord <- 0 to h) {

					backgroundTexture.set(xCoord, yCoord, c);
				}
			}
		}


	import scala.io._;
	import scala.collection.mutable.HashMap
	private def loadTimbreSurfaceDataset(filename: String) = {
		val set = Source.fromFile(filename).getLines();

		var audioFilename: String = "";
		var latentSize = 0;
		var blockSize  = 0;
		var blocks = 0;

		val coordinateMap = new mutable.HashMap[Int, List[Tuple2[Int,Float]]]

		// encoding of file
		// "filename", latentsize, blocksize
		// fileoffset, xcoord, value, xcoord, value, xcoord, value ...
		// .
		// .
		// .

		var firstLine = true;
		set.foreach( s => {
			var lineSplits = s.split(' ');
			if(!firstLine) {
				var currentFileOffset = -1;
				var currentXCoord = -1;

				lineSplits.foreach( ls => {
					if (currentFileOffset != -1) {
						if(currentXCoord != -1) {
							val intensity = ls.toFloat
							var coordList = coordinateMap.getOrElse(currentXCoord,List[Tuple2[Int,Float]]())
							coordList ::= (currentFileOffset, intensity);
							coordinateMap += ( currentXCoord -> coordList )

							currentXCoord = -1
						} else {
							currentXCoord = ls.toInt
						}
					} else {
						currentFileOffset = ls.toInt
					}
				})

			} else {
				audioFilename 	= lineSplits(0)
				blocks 			= lineSplits(1).toInt
				latentSize		= lineSplits(2).toInt
				blockSize 		= lineSplits(3).toInt
				firstLine 		= false;
			}
		})
		(audioFilename, latentSize, blocks, blockSize, coordinateMap)
	}

}

