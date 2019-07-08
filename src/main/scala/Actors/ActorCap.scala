package Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCap extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello there"
      case msg: String => println(s" ${self}Msg i got from system =>  $msg")
      case number: Int => println(s"Number i got from system => $number")
      case SpecialMessage(content) => println(s"Speical message => $content")
      case SendMessage(content) => self ! content
      case SayHello(ref) => ref ! "Hi"
      case WirelessMessage(content, ref) => ref forward (content + "s")
    }
  }

  val system = ActorSystem("ActorCap")
  val simpleActor = system.actorOf(Props[SimpleActor], "SimpleActor")

  //  simpleActor ! "Hello Actor"
  //
  //  simpleActor ! 42

  // 1 - messages can be of any type
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  // in practice use case classes and case objects

  case class SpecialMessage(msg: String)

  //  simpleActor ! SpecialMessage("How are you ")


  //Actor have info about their context ==> DS

  case class SendMessage(content: String)

  //  simpleActor ! SendMessage("Message to the System")


  // 3 - actors can REPLY to message
  val abc = system.actorOf(Props[SimpleActor], "Abc")
  val defs = system.actorOf(Props[SimpleActor], "Def")

  case class SayHello(ref: ActorRef)

  abc ! SayHello(defs)

  // forwarding messages
  // D -> A -> B
  // forwarding = sending a message with the ORIGINAL sender

  case class WirelessMessage(content: String, ref: ActorRef)

  abc ! WirelessMessage("Hi", defs)


  /**
    * Exercises
    *
    * 1. a Counter actor
    *   - Increment
    *   - Decrement
    *   - Print
    *
    * 2. a Bank account as an actor
    * receives
    *   - Deposit an amount
    *   - Withdraw an amount
    *   - Statement
    * replies with
    *   - Success
    *   - Failure
    *
    * interact with some other kind of actor
    */
  object Counter {

    case object Inc

    case object Dec

    case object print

  }

  class Counter extends Actor {

    import Counter._

    var count = 0

    override def receive: Receive = {
      case Inc => count += 1
      case Dec => count -= 1
      case _ => println("Counter value " + count)

    }
  }

  import Counter._

  val counter = system.actorOf(Props[Counter], "Counter")
  counter ! Inc
  counter ! Inc
  counter ! Inc
  counter ! Inc
  counter ! Inc
  counter ! Dec
  counter ! print


  /** * 2. a Bank account as an actor
    * receives
    *   - Deposit an amount
    *   - Withdraw an amount
    *   - Statement
    * replies with
    *   - Success
    *   - Failure
    *
    * interact with some other kind of actor
    */
  object BankAccount {

    case class Deposit(amount: Int)

    case class WithDraw(amount: Int)

    case object Statement

    case class TransSuccess(message: String)

    case class TransFailure(message: String)

  }

  class BankAccount extends Actor {

    import BankAccount._

    var fund = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0)
          sender() ! TransFailure("Amount invalid")
        else
          fund += amount
        sender() ! TransSuccess("Deposit Successfully")
      case WithDraw(amount) =>
        if (amount < 0)
          sender() ! TransFailure("Amount invalid")
        else if (amount > fund)
          sender() ! TransFailure("insufficent balance ")
        else
          fund -= amount
        sender() ! TransSuccess("withdraw Successfully")
      case Statement => sender() ! s"your mini statement $fund"
    }
  }

  import BankAccount._

  object Person {

    case class BankCycle(ref: ActorRef)

  }

  class Person extends Actor {

    import Person._

    override def receive: Receive = {
      case BankCycle(ref) =>
        ref ! Deposit(10000)
        ref ! Deposit(10000)
        ref ! Deposit(10000)
        ref ! Deposit(10000)
        ref ! Statement
        ref ! WithDraw(10000)
        ref ! Statement
      case message => println(message.toString)
    }
  }

  import Person._

  val person = system.actorOf(Props[Person], "PersonActor")
  val bankAccount = system.actorOf(Props[BankAccount], "BankAccountActor")

  person ! BankCycle(bankAccount)


}
