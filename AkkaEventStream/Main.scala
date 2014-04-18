package net.benhowell.example

/**
 * Created by Ben Howell [ben@benhowell.net] on 17-Apr-2014.
 */

import java.io.{File, FileOutputStream, PrintStream}

object Main {

  val ps = new PrintStream(new FileOutputStream(new File("output.txt")))

  def main(args: Array[String]) {

    // setup subscriptions
    EventStream.subscribe(Foo.onEvent, "foosubscriber")
    EventStream.subscribe(Bar.onEvent, "barsubscriber")
    EventStream.subscribe(Logger.onEvent(ps), "loggersubscriber")

    run
  }

  def run {
    EventStream.publish("topic A", "payload A")
    EventStream.publish("topic B", "payload B")
    stop
  }

  def stop = Logger.stop(ps)

}
