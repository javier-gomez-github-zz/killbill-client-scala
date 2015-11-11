package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.{HttpCharset, HttpHeader}
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 04/11/2015.
  */
object OverdueActor {
  case class UploadXMLOverdueConfig(overdueConfigPath: String)
  case class GetXMLOverdueConfig()
  case class GetOverdueStateForAccount(accountId: UUID)
}

case class OverdueActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import OverdueActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case UploadXMLOverdueConfig(overdueConfigPath) => {
      uploadXMLOverdueConfig(sender, overdueConfigPath)
      context.stop(self)
    }

    case GetXMLOverdueConfig() => {
      getXMLOverdueConfig(sender)
      context.stop(self)
    }

    case GetOverdueStateForAccount(accountId) => {
      getOverdueStateForAccount(sender, accountId)
      context.stop(self)
    }
  }

  def uploadXMLOverdueConfig(originalSender: ActorRef, overdueConfigPath: String) = {
    log.info("Uploading XML Overdue Config...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/overdue", overdueConfigPath) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("200")) {
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

  def getXMLOverdueConfig(originalSender: ActorRef) = {
    log.info("Requesting XML Overdue Config...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Get(killBillUrl+s"/overdue") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        if (response.status.toString().contains("200")) {
          originalSender ! response.entity.asString
        }
        else if (response.status.toString().contains("404")) {
          originalSender ! "Invoice Translations not found"
        }
        else {
          originalSender ! response.status.toString()
        }
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getOverdueStateForAccount(originalSender: ActorRef, accountId: UUID) = {
    log.info("Requesting Overdue State for Account: {}", accountId.toString)

    import OverdueStateJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[OverdueStateResult[OverdueState]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/overdue") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val overdueState = new OverdueState(response.name, response.externalMessage, response.daysBetweenPaymentRetries,
          response.disableEntitlementAndChangesBlocked, response.blockChanges, response.isClearState, response.reevaluationIntervalDays)
        originalSender ! overdueState
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
