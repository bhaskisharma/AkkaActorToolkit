package playground

object Maine extends App {
  var number = 4223
  var rem: Int = 0
  var sum: Int = 0

  while (number > 0) {
    rem = number % 10
    sum = sum * 10 + rem
    number = number/10
  }

  println(sum)

}
