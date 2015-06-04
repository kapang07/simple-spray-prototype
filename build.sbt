name := "SprayApiDemo"
 
version := "0.1"
 
scalaVersion := "2.11.6"
//"2.10.2"
 
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature")
 
//resolvers ++= Seq(
//  "spray repo" at "http://repo.spray.io"
//)

resolvers += "Maven central mirror" at "http://nexus.shopzilla.com:2099/nexus/content/repositories/central"

//resolvers += "Secured Central Repository" at "https://repo1.maven.org/maven2"

externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)
 
libraryDependencies ++= {
  val sprayVersion = "1.3.3"
  //"1.2-M8"
  val akkaVersion = "2.3.9"
  //"2.2.0-RC1"
  Seq(
  "io.spray" % "spray-can_2.11" % sprayVersion,
  "io.spray" % "spray-routing_2.11" % sprayVersion,
  "io.spray" % "spray-testkit_2.11" % sprayVersion,
  "io.spray" % "spray-client_2.11" % sprayVersion,
  "io.spray" % "spray-client_2.11" % sprayVersion,
  //"io.spray" %%  "spray-json" % "1.2.5",
  "io.spray" %%  "spray-json" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  //"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.12",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  //"org.scalatest" %% "scalatest" % "2.0.M7" % "test"
  )
}
 
seq(Revolver.settings: _*)