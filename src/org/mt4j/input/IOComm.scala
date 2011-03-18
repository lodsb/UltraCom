/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 9 :: 10 : 38
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

package org.mt4j.input

import react.{Observing, Var, EventSource}

trait TraitInputSource[A] {
	val receipt: EventSource[A] = new EventSource
}

trait TraitOutputSink[A] extends Observing{
	var send = new Var[A](null.asInstanceOf[A])
	private 	var defaultAction	: A => Boolean = {x => true}
	var sendAction 		: A => Boolean = defaultAction

	observe(send){ x =>
		if(sendAction != defaultAction) {
			sendAction(x)
		} else {true}
	}
}
