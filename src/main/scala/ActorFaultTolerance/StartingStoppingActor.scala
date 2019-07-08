package ActorFaultTolerance

import ActorFaultTolerance.StartingStoppingActor.Parent.StartChild
import Actors.ActorLoggings
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object StartingStoppingActor extends App {

  val system = ActorSystem("StartingStoppingActor")

  object Parent{
    case class StartChild(name:String)
    case class StopChild(name:String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = withChildern(Map())

    def withChildern(childern : Map[String,ActorRef]) : Receive = {
      case StartChild(name) =>
        log.info(s"Starting Child $name")
        context.become(withChildern(childern+ (name -> context.actorOf(Props[Child],name))))

    }
  }

  class Child extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  import Parent._

  val parent = system.actorOf(Props[Parent],"Parent")
  parent ! StartChild("child1")

}
