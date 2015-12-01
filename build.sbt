organization := "org.killbill"

name := "killbill-api-client-scala"

version := "0.1"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= {
  val sprayVersion = "1.3.2"
  val akkaVersion = "2.3.6"
  Seq(
    "io.spray" % "spray-can_2.11" % sprayVersion,
    "io.spray" % "spray-routing_2.11" % sprayVersion,
    "io.spray" % "spray-client_2.11" % sprayVersion,
    "io.spray" % "spray-json_2.11" % sprayVersion,
    "io.spray" % "spray-testkit_2.11" % sprayVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion
  )
}

exportJars := true

jacoco.settings

import org.scoverage.coveralls.Imports.CoverallsKeys._

coverallsToken := Some("vocfsvVrn9iYaVtqbq7u8KiJruS7F3Muh")