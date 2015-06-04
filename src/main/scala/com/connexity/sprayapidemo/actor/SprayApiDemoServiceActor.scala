package com.connexity.sprayapidemo.actor
 
import scala.concurrent.duration._

import akka.actor.{Actor, Props, ActorRef, ActorLogging}
import akka.util.Timeout
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import scala.util.{ Success, Failure }
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._
import spray.httpx.UnsuccessfulResponseException
import akka.pattern.ask
import akka.event.Logging

import spray.can.Http
import spray.can.server.Stats
import spray.http.StatusCodes._
import util.control.NonFatal
//import spray.util.{ LoggingContext}
//import scala.concurrent.ExecutionContext
//import ExecutionContext.Implicits.global

import spray.util.{ LoggingContext }
import spray.routing.RejectionHandler.Default

import com.connexity.sprayapidemo._
import com.connexity.sprayapidemo.model._
import com.connexity.sprayapidemo.model.OrderService
 
/**
 * Root actor to run all the routes
 */
class SprayApiDemoServiceActor(mainRoute: Route) extends Actor with HttpService  with ActorLogging { //with SprayApiDemoService {
   
  import ErrorJsonProtocol._
   
  implicit def actorRefFactory = context
  
  // custom error handling
  implicit val errHandler = ExceptionHandler {
    case err: ServiceException => { ctx =>
      log.warning("{} encountered while handling request: {}", err, ctx.request)
      ctx.complete(StatusCodes.BadRequest, ErrorMessage(err.errMsg, StatusCodes.BadRequest.intValue))
    }
    case err: UnsupportedOperationException => {
      log.error(err, "Unsupported.")
      complete(StatusCodes.InternalServerError, ErrorMessage(err.getMessage, StatusCodes.InternalServerError.intValue))
    }
    case error => {
      log.error(error, "something bad happened.")
      complete(StatusCodes.InternalServerError, ErrorMessage("An unknown error occurred.", StatusCodes.InternalServerError.intValue))
    }
  }
  
  // timeout handling
  def handleTimeouts: Receive = {
    case Timedout(x: HttpRequest) =>
      sender ! HttpResponse(StatusCodes.InternalServerError, "The server could not respond in the alloted time.")
  }
  
  def receive = handleTimeouts orElse runRoute(mainRoute)(errHandler, RejectionHandler.Default, context, RoutingSettings.default, LoggingContext.fromActorRefFactory)
}
 
/* 
 * Provides the routing for the api
 */
trait SprayApiDemoService extends HttpService with Core {
  import OrderJsonProtocol._
  import StatsJsonProtocol._
  
  val log = Logging(system, getClass)
  
  // implicit context so we can use futures
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(10.seconds)
  
    // start other actors
  val orderRef = actorRefFactory.actorOf(Props[OrderActor])
  
  
  def elevationRoute =
    pathPrefix("api") {
      path("ElevationService" / DoubleNumber / DoubleNumber) { (long, lat) =>
        requestContext =>
          val elevationService = actorRefFactory.actorOf(Props(new ElevationActor(requestContext)))
          elevationService ! ElevationActor.Process(long, lat)
      } ~
      path("ElevationService2" / DoubleNumber / DoubleNumber) { (long, lat) =>
        requestContext =>
          val elevationService = actorRefFactory.actorOf(Props(new ElevationActor(requestContext)))
          elevationService ! ElevationActor.Process(long, lat)
      } 
    }
  
  def orderRoute = new OrderService(orderRef).route
  
  lazy val indexHtml = """
    <html>
      <body>
        <h1>Spray Webservice Example</h1>
        <ol>
        <li>Internal Get Requests</li>
        <ul>
          <li><a href="api/OrderService/retrieveOrder/1">Get Request</a></li>
          <li><a href="api/OrderService/retrieveOrder/15">Get Request Invalid </a></li>
        </ul>
        <li>External Get Request</li>
        <ul>
        <li><a href="api/ElevationService/39/80">Call Google's API for elevation</a></li>
        </ul>
        <li>Internal Post Request </li>
        <ul>                  
          <li>Post Request Url: "http://localhost:8090/api/OrderService/makeOrder" </li>
          <li>Post Body with optional field 'category' excluded:</li>
          <code><pre>
    { 
      "name" : "Boots" ,
      "amount": 5
    }
          </pre></code>
          <li>Post Body with optional field 'category' included:</li>
          <code><pre>
    { 
      "name" : "Boots" ,
      "amount": 2,
      "category": "Shoes"
    }
          </pre></code>
          <li>Post Body with validation check erroring out:</li>
          <code><pre>
    { 
      "name" : "Boots" ,
      "amount": 101
    }
          </pre></code>
          <li>Post Request that will timeout:</li>
          <code><pre>
    { 
      "name" : "SlowItem" ,
      "amount": 2
    }
          </pre></code>
        </ul>
        
        </ol>
      </body>
    </html>
  """
  
  def staticRoute =
    path("") {
      redirect("/index", StatusCodes.MovedPermanently)
    } ~
    path("index") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            indexHtml
          }
        }
      }
    } ~
    path("favicon.ico") {
      complete(StatusCodes.NotFound)
    }
//    ~ // TODO: FIGURE out why this timesout
//    path("stats") {
//      complete {
//        //This is another way to use the Akka ask pattern
//        //with Spray.
//        (actorRefFactory.actorSelection("/user/IO-HTTP/listener-0") ? ()).collect {
//            case x: Stats => x
//            case otherResponse => throw new ServiceException(s"unexpected response received: ${otherResponse}")
//        }
//      }
//    }
    
  
  def mainRoute = elevationRoute ~ orderRoute ~ staticRoute
  
}