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
    >>                    // Niklas Kl?gel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.mt4j.stateMachine

import org.mt4j.commandSystem.Command
import org.mt4j.eventSystem.{foo, bar}
import scala.actors.Actor._
import actors.SchedulerAdapter

object StateMachine {
	def apply(body: => Unit): StateMachine = {
		val fsm = new FSM
		fsm.statemachine(body)

		fsm
	}
}


class FSM extends StateMachine;

trait StateMachine {

	import scala.actors.Actor
	import scala.actors.Actor._

	protected var Start: StateSymbol = null;
	var sm = {}
	var isSmSet = true;

	implicit def sym2SS(original: Symbol): StateSymbol = {

		funMap.getOrElse(original.name, {
			val s = new StateSymbol(original);
			funMap.put(original.name, s)
			s
		})
	}

	def consume[T](msg: T): Boolean = {
		if (this.isSmSet) {
			this.stateMachine ! msg
			true
		} else {
			false
		}
	}

	val funMap = new scala.collection.mutable.HashMap[String, StateSymbol]

	var stateMachine: Actor = null;

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

		def transitionToThis: Unit = {
			if(function == null) {
				throw new Exception("Undefined state?");
			}

			function.function
		}
	}

	def transition(sym: StateSymbol) = {
		sym.transitionToThis
	}

	def S(sym: StateSymbol): Unit = {
		Start = sym;
	}

	def →(sym: StateSymbol) = transition(sym);

	def ->(sym: StateSymbol) = transition(sym);

	'End is {}

	var sameThreadContext = true;

	def statemachine(body: => Unit): Unit = fsm(this.sameThreadContext)(body);

	def fsm(body: =>Unit): Unit = fsm(true)(body)

	def fsm(sameThreadContext: Boolean = true)(body: => Unit): Unit = {

		body
		sm = body;
		this.isSmSet = true;

		if (Start != null) {
			funMap.put("Start", Start)

			if (!sameThreadContext) {
				stateMachine = actor {
					Start.transitionToThis
				}
			} else {
				stateMachine = new InternalActor;
				stateMachine.start
			}

		} else {
			throw new Exception("No Start-Symbol defined!")
		}
	}

	class InternalActor extends Actor {
		override def scheduler = new SchedulerAdapter {
			def execute(block: => Unit) = {
				block;
			}
		}

		def act() {
			Start.transitionToThis
		}
	}

}

class TestFSM extends StateMachine {
	fsm(false) {

		S('A)

		'A is {
			println("-----State A");
			react {
				case Command('Test, _) => println("test"); →('A)
				case x: Int => println(x); transition('B);
				case x: foo => println("FOO"); →('A);
				case x: bar => println("BAR"); →('B);
				case "death" => →('End)
				case x: String => println("sdklfsdklfj!!!!!" + x); this transition 'B;
			}
		}

		'B is {
			println("---------State B");
			react {
				case x: Int => println("TEST"); →('A);
				case _ => println("TEST2"); →('B);
			}
		}
	}
}

object Hello {
	def main(args: Array[String]): Unit = {
		var fsm = new TestFSM

		fsm.stateMachine ! Command('Test, null);
		fsm.stateMachine ! new foo;
		fsm.stateMachine ! new bar;
		fsm.stateMachine ! "sdfsdfsdf"
		fsm.stateMachine ! "test"
		fsm.stateMachine ! 2;
		fsm.stateMachine ! "death"

		fsm = new TestFSM

		fsm.consume(new foo);
		fsm.consume(new bar);
		fsm.consume("test");
		fsm.consume(2);
		fsm.consume("death")
	}
}
