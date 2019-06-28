package playground

import akka.actor.ActorSystem

import scala.concurrent.Future

object Testing extends App {

  val actorSystem = ActorSystem("Akka")
  println("Hello, How are you " + actorSystem.name)

  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 32
    case 2 => 45
    case 3 => 49
  }

  val pf = (x: Int) => x match {
    case 1 => 4
    case 2 => 5
    case 3 => 4
  }

  val function: Int => Int = partialFunction

  val list = List(1, 2, 3).map {
    case 1 => 42
    case _ => "Anything"
  }

  println(list)

  val lifting = partialFunction.lift
  println(lifting(2))

  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 400
  }

  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => 32
    case _ => "Anythong"
  }


  //implicits

  implicit val timeOut = 3000

  def setTimeOut(f: () => Unit)(implicit timeOut: Int) = f

  setTimeOut(() => println(timeOut))

  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(name: String): Person = Person(name)

  println("ABC".greet)


  //implicit class

  implicit class Dog(name: String) {
    def bark = println("bark")
  }

  "Hello".bark

  //sorting using implict

  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)

  List(1, 2, 3, 4).sorted


  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("Hello,Future")
  }


  object Person{
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a,b) =>  a.name.compareTo(b.name) < 0)
  }


}
