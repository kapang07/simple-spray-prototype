package com.connexity.sprayapidemo.model
 
import spray.json.{ JsonFormat, DefaultJsonProtocol }
import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._
import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.{ ActorRef }
import akka.util.Timeout
import spray.http._
import scala.Some
import akka.pattern.ask
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
//import akka.event.Logging

import com.connexity.sprayapidemo.actor._
import  com.connexity.sprayapidemo.actor.OrderActor._
 
/*
 * Order objects to interact with the order service
 */
case class OrderItem(name: String, amount: Int, category: Option[String])
case class OrderId(id: Int, item: OrderItem)
case class OrderResult[T](status: String, id: OrderId, results: List[T])

/**
 * Json marshalling/unmarshalling definitions for Order classes
 */
object OrderJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val orderItemFormat = jsonFormat3(OrderItem)
  implicit val orderIdFormat = jsonFormat2(OrderId)
  implicit def orderResultFormat[T :JsonFormat] = jsonFormat3(OrderResult.apply[T])
}

/**
 * Defines the endpoints of the order service
 */
class OrderService(orderRef: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives {

  import OrderJsonProtocol._
  
  implicit val timeout = Timeout(10.seconds)
//  implicit val timeout = Timeout(5.seconds)
  
    
//  val log = Logging(orderRef, getClass)
  
  val route =
    pathPrefix("api") {
      path("OrderService" / "retrieveOrder" / IntNumber) { id => 
        get { 
          complete {
            (orderRef ? OrderActor.OrderRequest(id)).collect {
              case resultId: OrderId if resultId.id <= 10 => resultId//complete(resultId)
              case resultId: OrderId  => throw new ServiceException("id cannot be greater than 10")
              case failResponse => {
                throw new Exception("something bad happened")
              }
            }
          }
        }
      } ~
      path("OrderService" / "makeOrder") {
        post {
          handleWith { 
            //req: OrderItem => (orderRef ? OrderActor.OrderItemRequest(req)).mapTo[OrderItem]
            req: OrderItem => req match {
              case orderItem: OrderItem if req.name == "SlowItem" => {
                Thread sleep 11000 // only adding a sleep to trigger timeout ex
                 (orderRef ? OrderActor.OrderItemRequest(req)).collect {
                  case item: OrderItem => item
                  case invalidReq => throw new ServiceException("Something bad happened.")
                }
              }
              case orderItem: OrderItem if orderItem.amount <= 100 => {
                (orderRef ? OrderActor.OrderItemRequest(req)).collect {
                  case item: OrderItem => item
                  case invalidReq => throw new ServiceException("Something bad happened.")
                }
              }
              
              case orderItem: OrderItem => throw new ServiceException("order amount cannot be greater than 100")
              case _ => throw new ServiceException("Bad Request.")
            }
          }
        } //~
      }
    }

}