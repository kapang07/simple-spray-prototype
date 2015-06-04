package com.connexity.sprayapidemo.model

import spray.json.DefaultJsonProtocol

case class Rejection(rejectType :String, status: Int, message: String)

object RejectionJsonProtocol extends DefaultJsonProtocol{
   implicit val rejectionFormat = jsonFormat3(Rejection)
}