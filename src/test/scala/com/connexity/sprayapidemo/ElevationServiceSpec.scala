package com.connexity.sprayapidemo

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import spray.http.StatusCodes._
import spray.testkit.ScalatestRouteTest
import com.connexity.sprayapidemo.model._
import com.connexity.sprayapidemo.actor._

class ElevationServiceSpec extends FreeSpec with SprayApiDemoService with ScalatestRouteTest with Matchers {
  def actorRefFactory = system
 
  "The Elevation Service" - {
    "when calling GET api/ElevationService/39/80" - {
      "should return '1159.288940429688'" in {
        Get("/api/ElevationService/39/80") ~> elevationRoute ~> check {
          status should equal(OK)
          entity.toString should include("1159.288940429688")
        }
      }
    }
  }
}