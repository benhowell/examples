package net.benhowell.example

/**
 * Created by Ben Howell [ben@benhowell.net] on 17-Apr-2014.
 */

object Foo {
  val onEvent = (topic: String, payload: Any) => Some(topic) collect {
    case "topic A" => println("Foo received: topic = " + topic + ", payload = " + payload)
  }
}
