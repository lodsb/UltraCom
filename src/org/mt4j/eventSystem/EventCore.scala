/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 2 - 11 :: 6 : 53
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

package org.mt4j.eventSystem

import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.{IMTInputEventListener, IMTEventListener, MTEvent}

trait TrEventListener[T <: MTEvent] {
  def processEvent(event: T): Boolean
}

trait TrMTEventListener[T <: MTEvent] extends IMTEventListener with TrEventListener[T] {
  override def processEvent(event: T) = {
    processMTEvent(event);
    true;
  }
}

trait TrMTInputEventListener[T <: MTInputEvent[_]] extends IMTInputEventListener[T] with TrEventListener[T] {
  override def processEvent(event: T) = {
    this.processInputEvent(event.asInstanceOf[T]);
    true;
  }
}

class TrEventHandler {

  import scala.collection.mutable.HashMap

  private val listeners = new HashMap[Class[_], List[(_ <: MTEvent) => Boolean]]

  def handle(e: MTEvent) = {
    val f = listeners.get(e.getClass); //foreach(fun => fun(e));
    f match {
      case None => false;
      case Some(list) => list.foreach(fun => fun.asInstanceOf[(MTEvent) => Boolean].apply(e)); true
    }
  }

  private def _register[EVT <: MTEvent](fun: EVT => Boolean)
                                       (implicit m: scala.reflect.Manifest[EVT]): Unit = {
    val f = listeners.get(m.erasure);
    f match {
      case None => listeners.put(m.erasure, List(fun));
      case Some(list) => listeners.put(m.erasure, fun :: list)
    }

  }

  def register[T <: MTEvent](ei: TrEventListener[T])
                            (implicit m: scala.reflect.Manifest[T]): Unit = {
    this._register[T](ei.processEvent);
  }

  def register[T <: MTEvent](fun: T => Boolean)
                            (implicit m: scala.reflect.Manifest[T]): Unit = {
    this._register[T](fun);
  }
}


object EventCoreTest {

  def test(func: Int => Int): Unit = {
    println(func(4));
    println(123)
  }

  def main(args: Array[String]): Unit = {
    /*
      val ea = new Array[TrEventListener](3)
      //ea(0) =  new EInterface[Event];
      ea(1) =  new ETest;
      ea(2) =  new ETest3;

      val eh = new EventHandler
      eh.register[Event2](new ETest);
      eh.register[Event3](new ETest2);

      eh.register[Event[Int]]((x: Event[_]) => {println("Fuck yeah "+ea.size); true} );
      /*eh.register[Event2](ea(0))
      eh.register(ea(1))
      eh.register(ea(2))
      eh.register(classOf[Event4] , (x: Event) => {println("Fuck yeah "+ea.size); true}  )
      */
      //eh.handle(new Event)
      eh.handle(new Event[Int])
      eh.handle(new Event3)
      eh.handle(new Event[Double])
    */
  }


}