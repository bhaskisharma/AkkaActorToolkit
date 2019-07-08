package Actors

import Actors.ChildActor.Parent.{CreateChild, TellChild}
import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}

object ChildActor extends App {

  object Parent {

    case class CreateChild(name: String)

    case class TellChild(message: String)

  }

  class Parent extends Actor {

    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        val childActor = context.actorOf(Props[Child], "Child")
        context.become(withChild(childActor))
    }

    def withChild(ref: ActorRef): Receive = {
      case TellChild(message) => ref forward message
    }
  }


  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  val system = ActorSystem("ParentChildActor")
  val parent = system.actorOf(Props[Parent], "Parent")
  parent ! CreateChild("Child")
  parent ! TellChild("Hi How are you")

  val childSelection = system.actorSelection("/user/Parent/Child")
  childSelection ! "I found you!"


  object NaiveBankAccount {

    case class Deposit(amount: Int)

    case class Withdraw(amount: Int)

    case object InitializeAccount

  }

  class NaiveBankAccount extends Actor {

    import NaiveBankAccount._
    import CreditCard._

    var amount = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this) // !!
      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)

    }

    def deposit(funds: Int) = {
      println(s"${self.path} depositing $funds on top of $amount")
      amount += funds
    }

    def withdraw(funds: Int) = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount -= funds
    }
  }

  object CreditCard {

    case class AttachToAccount(bankAccount: NaiveBankAccount) // !!
    case object CheckStatus

  }

  class CreditCard extends Actor {
    import CreditCard._

    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }

    def attachedTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} your messasge has been processed.")
        // benign
        account.withdraw(1) // because I can
    }
  }

  import NaiveBankAccount._
  import CreditCard._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

}
