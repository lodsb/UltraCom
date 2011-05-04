/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 1 :: 3 : 7
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

/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.react

/**
 * Indicates a mismatch between the expected level of a reactive dependency and 
 * the actual one. 
 */
case class LevelMismatchNow(dependency: DependencyNode, level: Int) extends Exception {
  // fast exception: don't generate stack trace 
  //override def fillInStackTrace() = this
  override def getMessage = 
    "Level of dependency "+ dbgInfo(dependency) +": "+ dependency.level +" was >= level of acessor: "+ level 
}

case class LevelMismatchPrevious(dependency: DependencyNode, level: Int) extends Exception {
  // fast exception: don't generate stack trace 
  //override def fillInStackTrace() = this
  override def getMessage = 
    "Level of dependency "+ dbgInfo(dependency) +": "+ dependency.level +" was < level of acessor: "+ level 
}
