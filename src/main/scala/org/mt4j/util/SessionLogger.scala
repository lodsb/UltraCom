package org.mt4j.util

import java.util.concurrent.LinkedBlockingDeque
import org.mt4j.MTApplication
import java.util

object SessionLogger {

  val delimiter = " , "

  object SessionEvent extends Enumeration {
    type SessionEvent = Value
    val Event, BeginGesture, EndGesture = Value
  }
  import SessionEvent._

  import java.io._

  import com.github.nscala_time.time.Imports._

  private val dateFormat = DateTimeFormat.forPattern("dd.MM.yy--k-m-s");

  private def getDateFileFormat(date: DateTime) : String = {
    dateFormat.print(date)
  }

  private val prefixName: String = MT4jSettings.getInstance().getDefaultSessionLogName;
  private val filename = prefixName +"_start_at_"+ getDateFileFormat(MTApplication.getStartDateOfApplication)+".slog";
  private val fileWriter = new PrintWriter(new File(filename ));
  private val queue = new LinkedBlockingDeque[(DateTime, SessionEvent, String, Object)]()

  private class LogThread extends Runnable {
    def run() {
      MTApplication.logInfo("Started SessionLogger, using file "+filename)

      while(true) {
        val message = queue.take()
        SessionLogger._log(message)
      }
    }
  }

  private var gestureMap = Map[Object, DateTime]();
  private def _log(message: (DateTime, SessionEvent, String, Object)) {
    var millisGesture = -1L

    if (message._2 == SessionEvent.BeginGesture) {
      gestureMap += (message._4 -> message._1)
    } else if (message._2 == SessionEvent.EndGesture) {

      if (gestureMap.contains(message._4)) {
        val gestureBeginDateTime = gestureMap(message._4)
        gestureMap -= (message._4)

        millisGesture = (gestureBeginDateTime to message._1).millis
      }

    }

    val dtString = message._1.toString()
    fileWriter.println(dtString + delimiter + message._2
                      + delimiter + message._3
                      + delimiter + message._4
                      + delimiter + millisGesture);
    fileWriter.flush();

  }

  def log(msg: String, et: SessionEvent, obj: Object) {
    queue.add( (DateTime.now , et, msg, obj) )
  }

}
