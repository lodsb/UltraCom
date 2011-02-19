/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 2 - 18 :: 0 : 13
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

import scala.actors.Actor
import scala.actors.Actor._

class StateMachine {
	private var smHash = this.hashCode
	protected var Start: StateSymbol = null;
	var sm = {}
	var isSmSet = true;

	implicit def sym2SS(original: Symbol) :StateSymbol = {

		funMap.getOrElse(original.name, {
			val s = new StateSymbol(original);
			funMap.put(original.name, s)
			s
		})
	}

	val funMap = new scala.collection.mutable.HashMap[String, StateSymbol]

	var stateMachine:Actor = null;/*
		if(Start != null) {
			actor {
 				Start.transitionToThis
			}
		} else {
			throw new Exception("No Start-Symbol defined!")
		}

	})                            */


	class StateSymbol(var original: Symbol) {
		abstract class StateBody {
			def function();
		}

		var function: StateBody = null;

		def is(body: => Unit): Unit = {
		  	val a = new StateBody {
				def function() = body
		  	}

			function = a;
		}

		def transitionToThis : Unit = {
			function.function
		}
	}

	def transition(sym: StateSymbol) = {
		  sym.transitionToThis
	}

	def S(sym: StateSymbol): Unit = {
		Start = sym;
	}

	def ?(sym: StateSymbol) = transition(sym);

	def statemachine(body: => Unit) : Unit = {
			println("init!")
			body
			sm = body;
			this.isSmSet = true;

		if(Start != null) {

			stateMachine = actor {
 				Start.transitionToThis
			}
		} else {
			throw new Exception("No Start-Symbol defined!")
		}
		}

}


class TestFSM extends StateMachine {
	statemachine {
		//Start = 'A;
		S('A)

		'A is {
			println("-----State A");
			react {
					case x:String => println("sdklfsdklfj!!!!!"+x);	this transition 'B;
					case x:Int =>  println(x); 						transition('B);
			}
		}

		'B is {
			println("---------State B");
			react {
					case x:Int => println("NUMBERWHUMBA");			?('A);
					case _ => println("FUCKYOURMOM");     			?('B);
			}
		}
	}
}

object Hello {
	def main(args: Array[String]): Unit = {
		val fsm = new TestFSM
		fsm.stateMachine ! 23;
		fsm.stateMachine ! 2222222;
		fsm.stateMachine ! "sdfsdfsdf"
		fsm.stateMachine ! "test"
	}
}
