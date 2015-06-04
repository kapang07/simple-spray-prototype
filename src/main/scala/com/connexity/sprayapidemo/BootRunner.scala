package com.connexity.sprayapidemo
 
import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props, ActorRefFactory}
import akka.event.Logging
import akka.io.IO
import spray.can.Http
import akka.actor.actorRef2Scala
import akka.util.Timeout

import com.connexity.sprayapidemo.actor._
 
trait Core {

  protected implicit def system: ActorSystem

}

trait BootCore extends Core with SprayApiDemoService {//extends App {
 
  // we need an ActorSystem to host our application in
  def system: ActorSystem = ActorSystem("spray-api-service")
  def actorRefFactory: ActorRefFactory = system
 
  // create and start our service actor
  val service = system.actorOf(Props(new SprayApiDemoServiceActor(mainRoute))) //], "spray-service")
  
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http)(system) ! Http.Bind(service, interface = "localhost", port = system.settings.config.getInt("app.port"))
  
}

// actual webapp runner
object BootRunner extends App with BootCore with Core //with CoreActors

