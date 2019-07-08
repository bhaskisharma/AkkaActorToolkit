package Actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object AkkaConfig extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }


  //1 Inline Conf

  val confString =
    """
      |akka {
      | loglevel = "Error"
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(confString)
  val system = ActorSystem("ConfigApp")
  val inLineSystem = system.actorOf(Props[SimpleActor], "SimpleActor")

  inLineSystem ! "Infor Message"

  /**
    * 2 - config file
    */

  val defaultFile = ActorSystem("DefaultConfig")
  val default = defaultFile.actorOf(Props[SimpleActor], "Simple")

  default ! "Remember me"

  //Separate config

  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val speicalSystem = ActorSystem("SpeicalSystem",specialConfig)
  val specialConfigActor = speicalSystem.actorOf(Props[SimpleActor])
  specialConfigActor ! "Remember me, I am special"


}
