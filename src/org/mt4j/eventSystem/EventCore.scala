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

import java.util.concurrent.ConcurrentHashMap
import org.mt4j.input.inputData.MTInputEvent
import scala.collection.mutable.ListBuffer
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


class EventHandler {


  // Tuples listener -> fun(e)
  private val listeners = new ConcurrentHashMap[Class[_], ListBuffer[Tuple2[Any, (_ <: MTEvent) => Boolean]]]

  def handle(e: MTEvent) = {
    val f = listeners.get(e.getClass); //foreach(fun => fun(e));
    if(f != null) {
      f.foreach(t => {t._2.asInstanceOf[MTEvent => Boolean].apply(e)});
        true
    } else false
  }

  private def _register[EVT <: MTEvent](handler: Any, fun: EVT => Boolean) (implicit m: scala.reflect.Manifest[EVT]) = {
    val f = listeners.get(m.erasure);
    if(f == null) {
      listeners.put(m.erasure, (new ListBuffer)+new Tuple2(handler, fun));
    } else {
      listeners.put(m.erasure, f+ new Tuple2(handler,fun))
    }

  }

  def register[T <: MTEvent](ei: TrEventListener[T]) (implicit m: scala.reflect.Manifest[T]) : Unit = {
    this._register[T](ei, ei.processEvent);
  }

  def registerTypeless(ei: TrEventListener[MTEvent]) = {
    this.register(ei, ei.processEvent);
  }

  def register[T <: MTEvent](fun: T => Boolean) (implicit m: scala.reflect.Manifest[T]) : Unit = {
    this._register[T](None, fun);
  }

  def register[T <: MTEvent](handler: Any, fun: T => Boolean) (implicit m: scala.reflect.Manifest[T]) : Unit = {
    this._register[T](handler, fun);
  }

  def unregister[T <: MTEvent](ei: TrEventListener[T]) (implicit m: scala.reflect.Manifest[T]): Boolean = {
    unregister(ei,ei.processEvent)
  }

  def unregister[T <: MTEvent](fun: T => Boolean) (implicit m: scala.reflect.Manifest[T]): Boolean = {
    unregister(None, fun)
  }

  def unregister[T <: MTEvent](handler: Any, fun: T => Boolean) (implicit m: scala.reflect.Manifest[T]) : Boolean = {

    val list = listeners.get(m.erasure)
    if(list != null) {
        var index = -123;
        list.zipWithIndex.foreach({case (value,id) => if(value._1 == handler && value._2 == fun){index = id}});
        if(index >= 0) {
          list.remove(index);

          true

        } else {

          false
        }
    } else {

          false
    }

  }

  def listOfListeners : List[Tuple3[Class[_], Any, (_ <: MTEvent) => Boolean]] = {
    var list = List[Tuple3[Class[_], Any, (_ <: MTEvent) => Boolean]]()
    val iterator = listeners.entrySet.iterator

    while(iterator.hasNext) {
      val entry = iterator.next

      val listeners = entry.getValue.foreach({lt => {list = (new Tuple3(entry.getKey,lt._1, lt._2))::list}})
    }

    list
  }

}

class foo extends MTEvent {}
class bar extends MTEvent {}

class listener1 extends TrEventListener[foo] {
  override def processEvent(event: foo) = {println("listener1 "+event.getClass); true}
}

class listener2 extends TrEventListener[bar] {
  override def processEvent(event: bar) = {println("listener2 "+event.getClass); true}
}


object EventCoreTest {

  def test(func: Int => Int): Unit = {
    println(func(4));
    println(123)
  }

  def main(args: Array[String]): Unit = {
    val handler = new EventHandler;

    handler.register(new listener1);
    handler.register(new listener1);
    handler.register(new listener2);

    println("handle event of type foo")
    handler.handle(new foo)
    println("handle event of type bar")
    handler.handle(new bar)

  }

  def test(): Unit = {}

}