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
  * Created by jgomez on 04/11/2015.
  */
object TagActor {
  case class GetTags(offset: Long, limit: Long, auditMode: String)
  case class SearchTags(searchKey: String, offset: Long, limit: Long, auditMode: String)
  case class GetAllAccountTags(accountId: UUID, objectType: String, auditMode: String, includedDeleted: Boolean)
  case class GetAccountTags(accountId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreateAccountTag(accountId: UUID, tagDefinitionId: UUID)
  case class DeleteAccountTag(accountId: UUID, tagDefinitionId: UUID)
  case class GetBundleTags(bundleId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreateBundleTag(bundleId: UUID, tagDefinitionId: UUID)
  case class DeleteBundleTag(bundleId: UUID, tagDefinitionId: UUID)
  case class GetSubscriptionTags(subscriptionId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreateSubscriptionTag(subscriptionId: UUID, tagDefinitionId: UUID)
  case class DeleteSubscriptionTag(subscriptionId: UUID, tagDefinitionId: UUID)
  case class GetInvoiceTags(invoiceId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreateInvoiceTag(invoiceId: UUID, tagDefinitionId: UUID)
  case class DeleteInvoiceTag(invoiceId: UUID, tagDefinitionId: UUID)
  case class GetPaymentTags(paymentId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreatePaymentTag(paymentId: UUID, tagDefinitionId: UUID)
  case class DeletePaymentTag(paymentId: UUID, tagDefinitionId: UUID)
}

case class TagActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import TagActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)
  def sendAndReceive = sendReceive

  def receive = {
    case GetTags(offset, limit, auditLevel) =>
      getTags(sender, offset, limit, auditLevel)
      context.stop(self)

    case SearchTags(searchKey, offset, limit, auditLevel) =>
      searchTags(sender, searchKey, offset, limit, auditLevel)
      context.stop(self)

    case GetAllAccountTags(accountId, objectType, auditMode, includedDeleted) =>
      getAllAccountTags(sender, accountId, objectType, auditMode, includedDeleted)
      context.stop(self)

    case GetAccountTags(accountId, auditMode, includedDeleted) =>
      getAccountTags(sender, accountId, auditMode, includedDeleted)
      context.stop(self)

    case CreateAccountTag(accountId, tagDefinitionId) =>
      createAccountTag(sender, accountId, tagDefinitionId)
      context.stop(self)

    case DeleteAccountTag(accountId, tagDefinitionId) =>
      deleteAccountTag(sender, accountId, tagDefinitionId)
      context.stop(self)

    case GetBundleTags(bundleId, auditMode, includedDeleted) =>
      getBundleTags(sender, bundleId, auditMode, includedDeleted)
      context.stop(self)

    case CreateBundleTag(bundleId, tagDefinitionId) =>
      createBundleTag(sender, bundleId, tagDefinitionId)
      context.stop(self)

    case DeleteBundleTag(bundleId, tagDefinitionId) =>
      deleteBundleTag(sender, bundleId, tagDefinitionId)
      context.stop(self)

    case GetSubscriptionTags(subscriptionId, auditMode, includedDeleted) =>
      getSubscriptionTags(sender, subscriptionId, auditMode, includedDeleted)
      context.stop(self)

    case CreateSubscriptionTag(subscriptionId, tagDefinitionId) =>
      createSubscriptionTag(sender, subscriptionId, tagDefinitionId)
      context.stop(self)

    case DeleteSubscriptionTag(subscriptionId, tagDefinitionId) =>
      deleteSubscriptionTag(sender, subscriptionId, tagDefinitionId)
      context.stop(self)

    case GetInvoiceTags(invoiceId, auditMode, includedDeleted) =>
      getInvoiceTags(sender, invoiceId, auditMode, includedDeleted)
      context.stop(self)

    case CreateInvoiceTag(invoiceId, tagDefinitionId) =>
      createInvoiceTag(sender, invoiceId, tagDefinitionId)
      context.stop(self)

    case DeleteInvoiceTag(invoiceId, tagDefinitionId) =>
      deleteInvoiceTag(sender, invoiceId, tagDefinitionId)
      context.stop(self)

    case GetPaymentTags(paymentId, auditMode, includedDeleted) =>
      getPaymentTags(sender, paymentId, auditMode, includedDeleted)
      context.stop(self)

    case CreatePaymentTag(paymentId, tagDefinitionId) =>
      createPaymentTag(sender, paymentId, tagDefinitionId)
      context.stop(self)

    case DeletePaymentTag(paymentId, tagDefinitionId) =>
      deletePaymentTag(sender, paymentId, tagDefinitionId)
      context.stop(self)
  }

  def deletePaymentTag(originalSender: ActorRef, paymentId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Payment Tag: " + tagDefinitionId.toString)
    deleteObjectTag(originalSender, paymentId, tagDefinitionId, "invoicePayments")
  }

  def createPaymentTag(originalSender: ActorRef, paymentId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Payment Tag for Payment: " + paymentId.toString)
    createObjectTag(originalSender, paymentId, tagDefinitionId, "invoicePayments")
  }

  def getPaymentTags(originalSender: ActorRef, paymentId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Payment Tags for Payment: " + paymentId.toString)
    getObjectTags(originalSender, paymentId, auditLevel, includedDeleted, "invoicePayments")
  }

  def deleteInvoiceTag(originalSender: ActorRef, invoiceId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Invoice Tag: " + tagDefinitionId.toString)
    deleteObjectTag(originalSender, invoiceId, tagDefinitionId, "invoices")
  }

  def createInvoiceTag(originalSender: ActorRef, invoiceId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Invoice Tag for Invoice: " + invoiceId.toString)
    createObjectTag(originalSender, invoiceId, tagDefinitionId, "invoices")
  }

  def getInvoiceTags(originalSender: ActorRef, invoiceId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Invoice Tags for Invoice: " + invoiceId.toString)
    getObjectTags(originalSender, invoiceId, auditLevel, includedDeleted, "invoices")
  }

  def deleteSubscriptionTag(originalSender: ActorRef, subscriptionId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Subscription Tag: " + tagDefinitionId.toString)
    deleteObjectTag(originalSender, subscriptionId, tagDefinitionId, "subscriptions")
  }

  def createSubscriptionTag(originalSender: ActorRef, subscriptionId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Subscription Tag for Subscription: " + subscriptionId.toString)
    createObjectTag(originalSender, subscriptionId, tagDefinitionId, "subscriptions")
  }

  def getSubscriptionTags(originalSender: ActorRef, subscriptionId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Subscription Tags for Bundle: " + subscriptionId.toString)
    getObjectTags(originalSender, subscriptionId, auditLevel, includedDeleted, "subscriptions")
  }

  def deleteBundleTag(originalSender: ActorRef, bundleId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Bundle Tag: " + tagDefinitionId.toString)
    deleteObjectTag(originalSender, bundleId, tagDefinitionId, "bundles")
  }

  def createBundleTag(originalSender: ActorRef, bundleId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Bundle Tag for Bundle: " + bundleId.toString)
    createObjectTag(originalSender, bundleId, tagDefinitionId, "bundles")
  }

  def getBundleTags(originalSender: ActorRef, bundleId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Bundle Tags for Bundle: " + bundleId.toString)
    getObjectTags(originalSender, bundleId, auditLevel, includedDeleted, "bundles")
  }

  def deleteAccountTag(originalSender: ActorRef, accountId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Account Tag: " + tagDefinitionId.toString)
    deleteObjectTag(originalSender, accountId, tagDefinitionId, "accounts")
  }

  def createAccountTag(originalSender: ActorRef, accountId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Account Tag for Account: " + accountId.toString)
    createObjectTag(originalSender, accountId, tagDefinitionId, "accounts")
  }

  def getAccountTags(originalSender: ActorRef, accountId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Tags for Account: " + accountId.toString)
    getObjectTags(originalSender, accountId, auditLevel, includedDeleted, "accounts")
  }

  def getObjectTags(originalSender: ActorRef, objectId: UUID, auditLevel: String, includedDeleted: Boolean, resourcePathPrefix: String) = {
    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendAndReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/$resourcePathPrefix/$objectId/tags?audit=$auditLevel&includedDeleted=$includedDeleted") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def createObjectTag(originalSender: ActorRef, objectId: UUID, tagDefinitionId: UUID, resourcePathPrefix: String) = {
    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/$resourcePathPrefix/$objectId/tags?tagList=$tagDefinitionId") ~> addHeaders(headers)
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

  def deleteObjectTag(originalSender: ActorRef, objectId: UUID, tagDefinitionId: UUID, resourcePathPrefix: String) = {
    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/$resourcePathPrefix/$objectId/tags?tagList=$tagDefinitionId") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("204")) {
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

  def getAllAccountTags(originalSender: ActorRef, accountId: UUID, objectType: String, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting All Tags for Account: " + accountId.toString)

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendAndReceive ~> unmarshal[List[TagResult[Tag]]]

    var suffixUrl = ""
    if (!objectType.equalsIgnoreCase("")) {
      suffixUrl = "&objectType=" + objectType
    }

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/allTags?audit=$auditLevel&includedDeleted=$includedDeleted" + suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def searchTags(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Searching All Tags with searchKey=" + searchKey)

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendAndReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tags/search/$searchKey?offset=$offset&limit=$limit&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getTags(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Tags")

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendAndReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tags/pagination?offset=$offset&limit=$limit&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
