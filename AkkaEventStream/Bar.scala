package net.benhowell.example

/**
 * Created by Ben Howell [ben@benhowell.net] on 17-Apr-2014.
 */

object Bar {
  val onEvent = (topic: String, payload: Any) => Some(topic) collect {
    case "topic B" =>
      println("Bar received: topic = " + topic + " payload = " + payload)
      EventStream.publish("topic C", "payload C")
  }
}
