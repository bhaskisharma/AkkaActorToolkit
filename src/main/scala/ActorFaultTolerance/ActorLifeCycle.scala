package ActorFaultTolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifeCycle extends App {

  object StartChild

  class LifeCycleActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info(" I am stopped")

    override def receive: Receive = {
      case StartChild => context.actorOf(Props[LifeCycleActor], "Child")
    }
  }

  val system = ActorSystem("LifeCycleActor")
  val lifeCycleActor = system.actorOf(Props[LifeCycleActor],"LifeCycleActor")

//  lifeCycleActor ! StartChild
//  lifeCycleActor ! PoisonPill



  object Fail
  object FailChild
  object CheckChild
  object Check

  class Parent extends Actor{
    private val child = context.actorOf(Props[Child],"superChild")
    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("Super Actor Child Start")

    override def postStop(): Unit = log.info("Super Actor Post Stop")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting because of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("I failed")
      case Check =>
        log.info("alive and kicking")
    }
  }

  val supervisor = system.actorOf(Props[Parent],"ParentActor")
  supervisor ! FailChild
  supervisor ! CheckChild
}
