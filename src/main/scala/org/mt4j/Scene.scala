/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 2 - 15 :: 4 : 17
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
 */

package org.mt4j

import input.inputProcessors.globalProcessors.CursorTracer
import sceneManagement.SimpleAbstractScene

abstract class Scene(app:Application, name:String) extends SimpleAbstractScene(app, name){
	private var tracer:Option[CursorTracer] = None

	def showTracer(show: Boolean): Unit = {
		if(show) {
			if(tracer.isEmpty) {
				val ct = new CursorTracer(app,this)
				tracer = Some(ct)
			} else {
				val ct = tracer.get
				val overlay = ct.getOverlayGroup
				if(canvas.containsChild(overlay)) {
	    			canvas().removeChild(overlay)
					// create new instance - the overlay is lazily added in the MT4J event handling
					// so we rather not interfere
     				val ct = new CursorTracer(app,this)
					tracer = Some(ct)
				}
			}
		}  else {
			if(!tracer.isEmpty) {
				val ct = tracer.get
				val overlay = ct.getOverlayGroup
				if(canvas.containsChild(overlay)) {
	    			canvas().removeChild(overlay)
				}
				tracer = None
			}
		}
	}

}
