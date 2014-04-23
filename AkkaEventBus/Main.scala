package net.benhowell.example

import java.io.{File, FileOutputStream, PrintStream}

/**
 * Created by Ben Howell [ben@benhowell.net] on 20-Apr-2014.
 */
object Main extends App{

  // set up logger
  val ps = new PrintStream(new FileOutputStream(new File("output.txt")))
  val log = Logger.log(ps, _: String)

  // create subscribers
  val rootSubscriber = Actors.create(
    classOf[Subscription], "rootSubscriber", Actors.onReceive)

  val eventSubscriber = Actors.create(
    classOf[Subscription], "eventSubscriber", Actors.onReceive)

  val itemSubscriber = Actors.create(
    classOf[Subscription], "itemSubscriber", Actors.onReceive(log))
  
  // set up subscriptions
  SCEventBus.subscribe(rootSubscriber, "/")
  SCEventBus.subscribe(eventSubscriber, "/event")
  SCEventBus.subscribe(itemSubscriber, "/event/42")

  // create event publisher
  val eventPublisher = Actors.create(
    classOf[Subscription], "eventPublisher", Actors.onReceive)

  // generate some events
  SCEventBus.publish(("/", "payload A", eventPublisher))
  SCEventBus.publish(("/event", "payload B", eventPublisher))
  SCEventBus.publish(("/event/42", "payload C", eventPublisher))

  // clean up
  Logger.stop(ps)
}
