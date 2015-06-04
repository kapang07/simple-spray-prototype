package com.connexity.sprayapidemo.model

import spray.can.server.Stats
import scala.concurrent.duration._
import spray.http._
import spray.json.DefaultJsonProtocol
import spray.json._

object StatsJsonProtocol extends DefaultJsonProtocol{
  lazy implicit val finiteDurationFormat: RootJsonFormat[FiniteDuration] = new RootJsonFormat[FiniteDuration]{
    def write(f: FiniteDuration): JsObject = JsObject("length" -> JsNumber(10))

    def read(j: JsValue): FiniteDuration = throw new UnsupportedOperationException
  }
  lazy implicit val statsFormat = jsonFormat8(Stats)
}