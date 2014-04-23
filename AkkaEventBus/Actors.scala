package net.benhowell.example

import akka.actor.{Actor, ActorRef, Props, ActorSystem}

/**
 * Created by Ben Howell [ben@benhowell.net] on 22-Apr-2014.
 */

sealed class Subscription(f: (Any, Subscription, ActorRef) => Unit) extends Actor {
  override def receive = { case (payload: Any) => f(payload, this, sender) }
}

object Actors {

  val system = ActorSystem()

  def create(actorType: Class[_], name: String, args: AnyRef): ActorRef = {
    val props = Props(actorType, args)
    val actor = system.actorOf(props, name = name)
    actor
  }

  // receive handlers
  def onReceive = (payload: Any, receiver: Any, sender: ActorRef) => {
    println(s"$sender -> $receiver: $payload")
  }

  def onReceive(log: (String) => Unit) =
    (payload: Any, receiver: Any, sender: ActorRef) => {
      log(s"$sender -> $receiver: $payload")
      println(s"$sender -> $receiver: $payload")
  }
}
