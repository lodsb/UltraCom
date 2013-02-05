/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 2 - 25 :: 3 : 38
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

package org.mt4j.commandSystem


case class CommandScope(var parameters: List[_],
				   doCommand: List[_] => Boolean,
				   undoCommand: List[_] => Boolean) {

	def applyLocalScope(parameterScope: List[_]) {
		this.parameters = this.parameters:::parameterScope;
	}
}

case class Command(commandType: Symbol, scope: CommandScope);

trait CommandStack {
	import scala.collection.mutable.Stack

	private val commandStack = new Stack[Command];

	def execute(command: Command): Boolean = {
		if(command.scope.doCommand(command.scope.parameters)) {
			commandStack.push(command)
			true
		} else {
			false
		}
	}

	def undo() : Boolean = {
		if(!commandStack.isEmpty){
			val cmd = commandStack.pop();
			cmd.scope.undoCommand(cmd.scope.parameters)
		} else {
			false
		}
	}

}
