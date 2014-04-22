package net.benhowell.example

import akka.actor.{Actor, ActorRef, Props, ActorSystem}

/**
 * Created by Ben Howell [ben@benhowell.net] on 22-Apr-2014.
 */

sealed class Subscription(f: (Any, ActorRef) => Unit) extends Actor {
  override def receive = { case (payload: Any) => f(payload, sender) }
}

object ActorBase {

  val system = ActorSystem()

  def createActor(actorType: Class[_], name: String, args: AnyRef): ActorRef = {
    val props = Props(actorType, args)
    val actor = system.actorOf(props, name = name)
    actor
  }
}
