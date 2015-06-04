package com.connexity.sprayapidemo.actor

 
import com.connexity.sprayapidemo.model._
import com.connexity.sprayapidemo.actor.OrderActor._

import akka.actor.Actor
import akka.event.Logging
import spray.routing.RequestContext
import spray.client.pipelining._
import scala.util.{ Success, Failure }
import spray.httpx.SprayJsonSupport._
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.json.{ JsonFormat, DefaultJsonProtocol }
import spray.httpx.SprayJsonSupport._


object OrderActor {
  case class OrderItemRequest(item: OrderItem)
  case class OrderRequest(id: Int)
}
 
class OrderActor extends Actor {
 
  implicit val system = context.system
  import system.dispatcher
  val log = Logging(system, getClass)
 
  def receive = {
    case OrderItemRequest(item) => {
      sender ! process(item)
    }
    case OrderRequest(id) => {
      sender ! retrieveOrder(id)
    }
    case _ =>
      log.error(new Exception("invalid request sent"), "request did not match any expected")
  }
 
  def process(item: OrderItem) = { 
 
    log.info("Requesting order name: {}, amt: {}", item.name, item.amount)
    
    // process the order
    val resultItem: OrderItem = OrderItem(item.name + "-processed", item.amount, item.category)
    
    resultItem
  }
  
  def retrieveOrder(id: Int) = { 
 
    log.info("Requesting order id: {}", id)
    
    // do actual stuff in here
    val result = OrderId(id, new OrderItem("Laptop", 1, Some("Electronics")))
    log.info(s"my result: ${result}")
    
    result
  }
}