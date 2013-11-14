package org.mt4j.util

import java.util.concurrent.LinkedBlockingDeque
import org.mt4j.MTApplication
import java.util

object SessionLogger {

  object SessionEvent extends Enumeration {
    type SessionEvent = Value
    val Event, BeginGesture, EndGesture = Value
  }
  import SessionEvent._

  import java.io._

  import com.github.nscala_time.time.Imports._

  type LogMsgType = (DateTime, SessionEvent, String, Object, Object, Object)

  val delimiter = " | "

  private val dateFormat = DateTimeFormat.forPattern("dd.MM.yy--k-m-s");
  private val timeFormat = DateTimeFormat.forPattern("k:m:s:S");

  private def getDateFileFormat(date: DateTime) : String = {
    dateFormat.print(date)
  }

  private def getSimpleTimeFormat(date: DateTime) : String = {
    timeFormat.print(date)
  }


  private val prefixName: String = MT4jSettings.getInstance().getDefaultSessionLogName;
  private val filename = prefixName +"_start_at_"+ getDateFileFormat(MTApplication.getStartDateOfApplication)+".slog";
  private val fileWriter = new PrintWriter(new File(filename ));
  private val queue = new LinkedBlockingDeque[LogMsgType]()

  private class LogThread extends Runnable {
    def run() {
      MTApplication.logInfo("Started SessionLogger, using file "+filename)
      fileWriter.println("atMillis, eventType, message, object, millisPassed, payload");

      while(true) {
        val message = queue.take()
        SessionLogger._log(message)
      }
    }
  }

  private val logThread = new Thread(new LogThread());
  logThread.start()

  private var gestureMap = Map[String, DateTime]();
  private def _log(message: LogMsgType) {
    var commitToLog = true;

    var millisGesture = -1L
     
    var millisGestureString = "";
    // fake "hash"
    val keyString = ""+message._4+message._5

    if (message._2 == SessionEvent.BeginGesture) {
      // only add message if it is the first one (usually called from within update)
      if(!gestureMap.contains(keyString)) {
        gestureMap += (keyString -> message._1)
      } else {
        commitToLog = false
      }

    } else if (message._2 == SessionEvent.EndGesture) {

      if (gestureMap.contains(keyString)) {
        val gestureBeginDateTime = gestureMap(keyString)
        gestureMap -= (keyString)

        millisGesture = (gestureBeginDateTime to message._1).millis
      }

    } 
//FIXME: hack!!!
if(message._2 == SessionEvent.Event) {
millisGestureString = message._5+"";
} else {
	millisGestureString = millisGesture+""
}


    if (commitToLog) {
      val intervalSinceAppStart = (MTApplication.getStartDateOfApplication to message._1).millis
      val csvString = intervalSinceAppStart + delimiter + message._2 + delimiter + "\""+message._3+"\"" + delimiter + message._4 + delimiter + millisGestureString + delimiter + message._6;

      fileWriter.println(csvString);
      fileWriter.flush();

      println(csvString)
    }

  }

  def log(msg: String, et: SessionEvent, obj: Object, src: Object, payload: Object) {
    val message : LogMsgType = (DateTime.now, et, msg, obj, src, payload);
    queue.add( message )
  }

}
