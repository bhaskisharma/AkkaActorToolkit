name := "AkkaActorToolkit"

version := "0.1"

scalaVersion := "2.11.8"

val akkaVersion = "2.5.13"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= {
  Seq(
    "org.scala-lang" % "scala-library" % "2.11.8",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion
  )
}
