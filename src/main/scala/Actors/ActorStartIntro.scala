package Actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorStartIntro extends App {

  //defining actorsystem
  val actorSystem = ActorSystem("firstActor")
  println(actorSystem.name)

  //creating actor

  class WordCounter extends Actor {
    var totalCounter = 0

    override def receive: Receive = {
      case message: String =>
        println(s"[Word Count] message to counter $totalCounter")
        totalCounter += message.split(" ").length
      case _ => println("Unknown message from Actor System")
    }
  }

  //instantiate the actor
  val wordCounter = actorSystem.actorOf(Props[WordCounter], "wordCounter")

  //calling the actor
  wordCounter ! "Hello How are you dude "


  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"hi my name is $name")
      case _ =>
    }
  }

  val anotherPerson = actorSystem.actorOf(Props(new Person("ABC")), "Person")

  anotherPerson ! "hi"

  //Best Practice to constru the Actor

  object Hello {
    def props(msg :String) = Props(new Hello(msg))
  }

  class Hello(msg :String) extends Actor{
    override def receive: Receive = {
      case "hello" => println(s"Hello how are you $msg")
      case _ =>
    }
  }

  val helloMsg = actorSystem.actorOf(Hello.props("Hello"),"Hello")
  helloMsg ! "hello"




}
