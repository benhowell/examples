package net.benhowell.example

import akka.actor.ActorRef
import java.io.{File, FileOutputStream, PrintStream}

/**
 * Created by Ben Howell [ben@benhowell.net] on 20-Apr-2014.
 */
object Main extends App{

  // set up logger
  val ps = new PrintStream(new FileOutputStream(new File("output.txt")))
  val log = Logger.log(ps, _: String)

  // create message receiver handlers
  def onReceive(receiver: String) = (payload: Any, sender: ActorRef) => {
    println(s"$receiver received: payload = $payload from sender: $sender")
  }

  def onReceive(receiver: String, log: (String) => Unit) = (payload: Any, sender: ActorRef) => {
    log(sender + " -> EventItemSubscriber: " + payload)
    println(s"$receiver received: payload = $payload from sender: $sender")
  }

  // create subscribers
  val rootSubscriber = ActorBase.createActor(classOf[Subscription], "rootSubscriber", onReceive("rootSubscriber"))
  val eventSubscriber = ActorBase.createActor(classOf[Subscription], "eventSubscriber", onReceive("eventSubscriber"))
  val itemSubscriber = ActorBase.createActor(classOf[Subscription], "itemSubscriber", onReceive("itemSubscriber", log))
  
  // set up subscriptions
  SCEventBus.subscribe(rootSubscriber, "/")
  SCEventBus.subscribe(eventSubscriber, "/event")
  SCEventBus.subscribe(itemSubscriber, "/event/42")

  // create event publisher
  val eventPublisher = ActorBase.createActor(classOf[Subscription], "eventPublisher", onReceive("eventPublisher"))

  // generate some events
  SCEventBus.publish(("/", "payload A", eventPublisher))
  SCEventBus.publish(("/event", "payload B", eventPublisher))
  SCEventBus.publish(("/event/42", "payload C", eventPublisher))

  // clean up
  Logger.stop(ps)
}
