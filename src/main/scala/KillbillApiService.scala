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
      path("AccountService" / Rest) { (externalKey) =>
        requestContext =>
          val accountService = actorRefFactory.actorOf(Props(new AccountService(requestContext)))
          accountService ! AccountService.Process(externalKey)
      }
    }
}
