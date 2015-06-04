package com.connexity.sprayapidemo.model
 
import spray.json.{ JsonFormat, DefaultJsonProtocol }
 
/**
 * Elevation model objects 
 */
case class Elevation(location: Location, elevation: Double)
case class Location(lat: Double, lng: Double)
case class GoogleElevationApiResult[T](status: String, results: List[T])
 
/**
 * Elevation json marshalling/unmarshaller
 */
object ElevationJsonProtocol extends DefaultJsonProtocol {
  implicit val locationFormat = jsonFormat2(Location)
  implicit val elevationFormat = jsonFormat2(Elevation)
  implicit def googleElevationApiResultFormat[T :JsonFormat] = jsonFormat2(GoogleElevationApiResult.apply[T])
}