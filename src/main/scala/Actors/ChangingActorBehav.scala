package Actors

import Actors.ChangingActorBehav.Kid.KidAccept
import Actors.ChangingActorBehav.Mom.MomStart
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.japi

object ChangingActorBehav extends App {

  object Kid {

    case object KidAccept

    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }


  class Kid extends Actor {

    import Mom._
    import Kid._

    var STATE: String = HAPPY

    override def receive: Receive = {
      case Food(VEG) => STATE = SAD
      case Food(CHOC) => STATE = HAPPY
      case Ask(_) =>
        if (STATE == HAPPY)
          sender() ! KidAccept
        else
          sender() ! KidReject
    }
  }


  class StateLessKid extends Actor {

    import Kid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEG) => context.become(sadReceive, false)
      case Food(CHOC) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEG) => context.become(sadReceive, false)
      case Food(CHOC) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {

    case class MomStart(ref: ActorRef)

    case class Food(food: String)

    case class Ask(message: String)

    val VEG = "veg"
    val CHOC = "choc"

  }


  class Mom extends Actor {

    import Mom._
    import Kid._

    override def receive: Receive = {
      case MomStart(ref) =>
        ref ! Food(VEG)
        ref ! Food(VEG)
        ref ! Food(VEG)
        ref ! Food(VEG)
        ref ! Food(VEG)
        ref ! Food(VEG)
        ref ! Food(CHOC)
        ref ! Food(CHOC)
        ref ! Ask("Hi How are you")
      case KidAccept => println("Yay, my kid is happy!")
      case KidReject => println("My kid is sad, but as he's healthy!")
    }
  }

  val system = ActorSystem("ChangingActorSystem")
  val momActor = system.actorOf(Props[Mom], "Mom")
  val kidActor = system.actorOf(Props[Kid], "Kid")
  val stateLessKid = system.actorOf(Props[Kid], "StateLess")

  //  momActor ! MomStart(stateLessKid)


  /**
    * Exercises
    * 1 - recreate the Counter Actor with context.become and NO MUTABLE STATE
    */

  object Counter {

    case object Increment

    case object Decrement

    case object Print

  }


  class Counter extends Actor {

    import Counter._

    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"[countReceive($currentCount)] incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"[countReceive($currentCount)] decrementing")
        context.become(countReceive(currentCount - 1))
      case Print =>
        println(s"[countReceive($currentCount)] my current count is $currentCount")
    }

  }

  import Counter._

  val counter = system.actorOf(Props[Counter], "myCounter")

//  (1 to 5).foreach(_ => counter ! Increment)
//  (1 to 3).foreach(_ => counter ! Decrement)
//  counter ! Print


  /**
    * Exercise 2 - a simplified voting system
    */

  case class Vote(candidate: String)

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(candidate) => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }


  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {
    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizensRef => citizensRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest // this might end up in an infinite loop
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $newStats")
        } else {
          // still need to process some statuses
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

}
