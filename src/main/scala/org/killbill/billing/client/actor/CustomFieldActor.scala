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
  * Created by jgomez on 10/11/2015.
  */
object CustomFieldActor {
  case class GetCustomFields(offset: Long, limit: Long, auditMode: String)
  case class SearchCustomFields(searchKey: String, offset: Long, limit: Long, auditMode: String)
  case class GetAccountCustomFields(accountId: UUID, auditMode: String)
  case class CreateAccountCustomFields(accountId: UUID, customFields: List[CustomField])
  case class DeleteAccountCustomFields(accountId: UUID, customFields: List[UUID])
  case class GetPaymentMethodCustomFields(paymentMethodId: UUID, auditMode: String)
  case class CreatePaymentMethodCustomFields(paymentMethodId: UUID, customFields: List[CustomField])
  case class DeletePaymentMethodCustomFields(paymentMethodId: UUID, customFields: List[UUID])
}

case class CustomFieldActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import CustomFieldActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetCustomFields(offset, limit, auditLevel) =>
      getCustomFields(sender, offset, limit, auditLevel)
      context.stop(self)

    case SearchCustomFields(searchKey, offset, limit, auditLevel) =>
      searchCustomFields(sender, searchKey, offset, limit, auditLevel)
      context.stop(self)

    case GetAccountCustomFields(accountId, auditMode) =>
      getAccountCustomFields(sender, accountId, auditMode)
      context.stop(self)

    case CreateAccountCustomFields(accountId, customFields) =>
      createAccountCustomFields(sender, accountId, customFields)
      context.stop(self)

    case DeleteAccountCustomFields(accountId, customFields) =>
      deleteAccountCustomFields(sender, accountId, customFields)
      context.stop(self)

    case GetPaymentMethodCustomFields(accountId, auditMode) =>
      getPaymentMethodCustomFields(sender, accountId, auditMode)
      context.stop(self)

    case CreatePaymentMethodCustomFields(accountId, customFields) =>
      createPaymentMethodCustomFields(sender, accountId, customFields)
      context.stop(self)

    case DeletePaymentMethodCustomFields(accountId, customFields) =>
      deletePaymentMethodCustomFields(sender, accountId, customFields)
      context.stop(self)
  }

  def deletePaymentMethodCustomFields(originalSender: ActorRef, paymentMethodId: UUID, customFields: List[UUID]) = {
    log.info("Deleting Payment Method Custom Fields...")

    val pipeline = sendReceive

    var suffixUrl = ""
    if (!customFields.isEmpty) {
      suffixUrl = "?customFieldList=" + customFields.toString()
    }

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/paymentMethods/$paymentMethodId/customFields" + suffixUrl) ~> addHeaders(headers)
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

  def createPaymentMethodCustomFields(originalSender: ActorRef, paymentMethodId: UUID, customFields: List[CustomField]) = {
    log.info("Creating Payment Method Custom Fields...")

    import CustomFieldJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/paymentMethods/$paymentMethodId/customFields", customFields) ~> addHeaders(headers)
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

  def getPaymentMethodCustomFields(originalSender: ActorRef, paymentMethodId: UUID, auditLevel: String) = {
    log.info("Requesting All Custom Fields for Payment Method: " + paymentMethodId.toString)

    import CustomFieldJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[CustomFieldResult[CustomField]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/paymentMethods/$paymentMethodId/customFields?audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def deleteAccountCustomFields(originalSender: ActorRef, accountId: UUID, customFields: List[UUID]) = {
    log.info("Deleting Account Custom Fields...")

    val pipeline = sendReceive

    var suffixUrl = ""
    if (!customFields.isEmpty) {
      suffixUrl = "?customFieldList=" + customFields.toString()
    }

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/accounts/$accountId/customFields" + suffixUrl) ~> addHeaders(headers)
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

  def createAccountCustomFields(originalSender: ActorRef, accountId: UUID, customFields: List[CustomField]) = {
    log.info("Creating Account Custom Fields...")

    import CustomFieldJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts/$accountId/customFields", customFields) ~> addHeaders(headers)
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

  def getAccountCustomFields(originalSender: ActorRef, accountId: UUID, auditLevel: String) = {
    log.info("Requesting All Custom Fields for Account: " + accountId.toString)

    import CustomFieldJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[CustomFieldResult[CustomField]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/customFields?audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def searchCustomFields(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Searching All Custom Fields with searchKey=" + searchKey)

    import CustomFieldJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[CustomFieldResult[CustomField]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/customFields/search/$searchKey?offset=$offset&limit=$limit&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getCustomFields(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Custom Fields")

    import CustomFieldJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[CustomFieldResult[CustomField]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/customFields/pagination?offset=$offset&limit=$limit&audit="+auditLevel) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
