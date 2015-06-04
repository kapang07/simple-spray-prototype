package com.connexity.sprayapidemo.model

import spray.http._
import spray.json.DefaultJsonProtocol

/**
 * Custom errors and Exception and formatting
 */

case class ErrorResponseException (
  responseStatus: StatusCode, 
  response: Option[HttpEntity]
) extends Exception

case class ServiceException (errMsg: String) extends Exception

case class ErrorMessage (errMsg: String, responseStatus: Int) 

object ErrorJsonProtocol extends DefaultJsonProtocol{
   implicit val errorFormat = jsonFormat2(ErrorMessage)
}