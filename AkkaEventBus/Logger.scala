package net.benhowell.example

import java.io.{PrintStream, IOException}

/**
 * Created by Ben Howell [ben@benhowell.net] on 21-Apr-2014.
 */
object Logger {

  val log = (ps: PrintStream, msg: String) => {
    try {
      ps.println(msg)
    }
    catch {
      case ioe: IOException => println("IOException: " + ioe.toString)
      case e: IOException => println("Exception: " + e.toString)
    }
  }

  def stop(ps: PrintStream) = ps.close()
}
