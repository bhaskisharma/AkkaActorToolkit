package Actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggings extends App {

  class SimpleActorWithLogger extends Actor {
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message =>logger.info(message.toString)
    }
  }

  val system = ActorSystem("ActorLog")
  val loggerActor = system.actorOf(Props[SimpleActorWithLogger],"SimpleActorLogger")

  loggerActor ! "Info log test"


  class AnotherLogger extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val anotherLogger = system.actorOf(Props[AnotherLogger],"AnotherLogger")
  anotherLogger ! "Another Logger Test"

}
