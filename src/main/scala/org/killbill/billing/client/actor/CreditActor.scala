package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 05/11/2015.
  */
object CreditActor {
  case class GetCredit(creditId: UUID)
  case class CreateCredit(credit: Credit)
}

case class CreditActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import CreditActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetCredit(creditId) =>
      getCredit(sender, creditId)
      context.stop(self)
    case CreateCredit(credit) =>
      createCredit(sender, credit)
  }

  def createCredit(originalSender: ActorRef, credit: Credit) = {
    log.info("Creating new Credit...")

    import CreditJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/credits", credit) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("201")) {
          originalSender ! response.entity.asString
        }
        else {
          originalSender ! response.status.toString()
        }
      }
      case Failure(error) => {
        originalSender ! error.getMessage()
      }
    }
  }

  def getCredit(originalSender: ActorRef, creditId: UUID) = {
    log.info("Requesting Credit with ID: {}", creditId.toString)

    import CreditJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[CreditResult[Credit]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/credits/$creditId") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val credit = new Credit(response.creditAmount, response.invoiceId, response.invoiceNumber, response.effectiveDate, response.accountId)
        originalSender ! credit
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}