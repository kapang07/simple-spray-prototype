package com.connexity.sprayapidemo.actor

import akka.actor.Actor
import akka.event.Logging
import spray.routing.RequestContext
import spray.client.pipelining._
import scala.util.{ Success, Failure }
import com.connexity.sprayapidemo.model._
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._
import spray.http._
import spray.http.StatusCodes._
 
object ElevationActor {
  case class Process(long: Double, lat: Double)
}

/**
 * External call to google elevation api's 
 */
class ElevationActor(requestContext: RequestContext) extends Actor {
 
  import ElevationActor._
 
  implicit val system = context.system
  import system.dispatcher
  val log = Logging(system, getClass)
 
  def receive = {
    case Process(long,lat) =>
      process(long,lat)
      //context.stop(self)
  }
 
  def process(long: Double, lat: Double) = { 
 
    log.info("Requesting elevation long: {}, lat: {}", long, lat)
 
    import ElevationJsonProtocol._
    
    val pipeline = sendReceive ~> unmarshal[GoogleElevationApiResult[Elevation]]
 
    val responseFuture = pipeline{
      Get(s"http://maps.googleapis.com/maps/api/elevation/json?locations=$long,$lat&sensor=false")
    }
    responseFuture onComplete {
      //case Success(GoogleElevationApiResult(_, Elevation(_, elevation) :: _)) =>
      case Success(GoogleElevationApiResult(_, elevation :: _)) =>
        log.info("The elevation is: {}", elevation)
        requestContext.complete(elevation)
      case Success(unexpected) =>
        log.warning(s"Unexpected response: ${unexpected}")
        requestContext.complete(StatusCodes.ExpectationFailed, new ServiceException(unexpected.toString()))
      case Failure(error) =>
        log.info(s"Unexpected response: ${error}")
        requestContext.complete(StatusCodes.ExpectationFailed, new ServiceException(error.toString()))
    }
  }
}