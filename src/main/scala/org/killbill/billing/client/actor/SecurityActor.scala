package org.killbill.billing.client.actor

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 11/11/2015.
  */
object SecurityActor {
  case class GetPermissions()
}

case class SecurityActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import SecurityActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetPermissions() =>
      getPermissions(sender)
  }

  def getPermissions(originalSender: ActorRef) = {
    log.info("Getting Permissions...")

    import SprayJsonSupport._
    import DefaultJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[String]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/security/permissions") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}