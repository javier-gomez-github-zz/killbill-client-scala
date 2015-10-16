package org.killbill.api.client.scala

import akka.actor.{Actor, Props}
import akka.event.Logging
import spray.routing._
import spray.http._
import MediaTypes._

class KillbillApiServiceActor extends Actor with KillbillApiService {
  
  def actorRefFactory = context

  def receive = runRoute(killbillApiRoute)
}

trait KillbillApiService extends HttpService {
  val killbillApiRoute =
    pathPrefix("api") {
//      path("ElevationService" / DoubleNumber / DoubleNumber) { (long, lat) =>
//        requestContext =>
//          val elevationService = actorRefFactory.actorOf(Props(new ElevationService(requestContext)))
//          elevationService ! ElevationService.Process(long, lat)
//      } ~
//      path("TimezoneService" / DoubleNumber / DoubleNumber / Segment) { (long, lat, timestamp) =>
//        requestContext =>  
//          val timezoneService = actorRefFactory.actorOf(Props(new TimezoneService(requestContext)))
//          timezoneService ! TimezoneService.Process(long, lat, timestamp)
//      }
      path("AccountService" / Rest) { (externalKey) =>
        requestContext =>
          val accountService = actorRefFactory.actorOf(Props(new AccountService(requestContext)))
          accountService ! AccountService.Process(externalKey)
      }
    }
}
