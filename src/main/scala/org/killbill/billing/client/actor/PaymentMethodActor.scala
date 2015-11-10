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
  * Created by jgomez on 09/11/2015.
  */
object PaymentMethodActor {
  case class GetPaymentMethods(offset: Long, limit: Long, auditMode: String)
  case class SearchPaymentMethods(searchKey: String, offset: Long, limit: Long, auditMode: String, withPluginInfo: Boolean, pluginName: String)
  case class GetPaymentMethodById(paymentMethodId: UUID, withPluginInfo: Boolean, auditMode: String)
  case class GetPaymentMethodByExternalKey(externalKey: String, withPluginInfo: Boolean, auditMode: String)
  case class GetPaymentMethodsForAccount(accountId: UUID, auditMode: String)
  case class CreatePaymentMethod(accountId: UUID, paymentMethod: PaymentMethod, isDefault: Boolean, payAllUnpaidInvoices: Boolean)
  case class UpdatePaymentMethod(accountId: UUID, paymentMethodId: UUID, pluginProperties: Map[String, String], payAllUnpaidInvoices: Boolean)
  case class DeletePaymentMethod(paymentMethodId: UUID, deleteDefault: Boolean, pluginProperties: Map[String, String])
}

case class PaymentMethodActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import PaymentMethodActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetPaymentMethods(offset, limit, auditLevel) =>
      getPaymentMethods(sender, offset, limit, auditLevel)
      context.stop(self)

    case SearchPaymentMethods(searchKey, offset, limit, auditLevel, withPluginInfo, pluginName) =>
      searchPaymentMethods(sender, searchKey, offset, limit, auditLevel, withPluginInfo, pluginName)
      context.stop(self)

    case GetPaymentMethodById(paymentMethodId, withPluginInfo, auditLevel) =>
      getPaymentMethodById(sender, paymentMethodId, withPluginInfo, auditLevel)
      context.stop(self)

    case GetPaymentMethodByExternalKey(externalKey, withPluginInfo, auditLevel) =>
      getPaymentMethodByExternalKey(sender, externalKey, withPluginInfo, auditLevel)
      context.stop(self)

    case GetPaymentMethodsForAccount(accountId, auditMode) =>
      getPaymentMethodsForAccount(sender, accountId, auditMode)
      context.stop(self)

    case CreatePaymentMethod(accountId, paymentMethod, isDefault, payAllUnpaidInvoices) =>
      createPaymentMethod(sender, accountId, paymentMethod, isDefault, payAllUnpaidInvoices)
      context.stop(self)

    case UpdatePaymentMethod(accountId, paymentMethodId, pluginProperties, payAllUnpaidInvoices) =>
      updatePaymentMethod(sender, accountId, paymentMethodId, pluginProperties, payAllUnpaidInvoices)
      context.stop(self)

    case DeletePaymentMethod(paymentMethodId, deleteDefault, pluginProperties) =>
      deletePaymentMethod(sender, paymentMethodId, deleteDefault, pluginProperties)
      context.stop(self)
  }

  def deletePaymentMethod(originalSender: ActorRef, paymentMethodId: UUID, deleteDefault: Boolean, pluginProperties: Map[String, String]) = {
    log.info("Deleting Payment Method...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/paymentMethods/$paymentMethodId?deleteDefaultPmWithAutoPayOff=$deleteDefault&pluginProperty=$pluginProperties") ~> addHeaders(headers)
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

  def updatePaymentMethod(originalSender: ActorRef, accountId: UUID, paymentMethodId: UUID, pluginProperties: Map[String, String], payAllUnpaidInvoices: Boolean) = {
    log.info("Updating Payment Method...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/accounts/$accountId/paymentMethods/$paymentMethodId/setDefault?payAllUnpaidInvoices=$payAllUnpaidInvoices&pluginProperty=$pluginProperties") ~> addHeaders(headers)
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

  def createPaymentMethod(originalSender: ActorRef, accountId: UUID, paymentMethod: PaymentMethod, isDefault: Boolean, payAllUnpaidInvoices: Boolean) = {
    log.info("Creating Payment Method...")

    import ResponseUriJsonProtocol._
    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[ResponseUriResult[ResponseUri]]

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts/$accountId/paymentMethods?isDefault=$isDefault&payAllUnpaidInvoices=$payAllUnpaidInvoices", paymentMethod) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        getPaymentMethodWithUrl(originalSender, response.uri.mkString)
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentMethodWithUrl(originalSender: ActorRef, uri: Any) = {
    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[PaymentMethodResult[PaymentMethod]]

    val responseFuture = pipeline {
      Get(uri.asInstanceOf[String]) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val paymentMethod = new PaymentMethod(response.paymentMethodId, response.externalKey, response.accountId,
          response.isDefault, response.pluginName, response.pluginInfo)
        originalSender ! paymentMethod
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentMethodsForAccount(originalSender: ActorRef, accountId: UUID, auditLevel: String) = {
    log.info("Requesting All Payment Methods for Account: " + accountId.toString)

    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[PaymentMethodResult[PaymentMethod]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/paymentMethods?audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentMethodByExternalKey(originalSender: ActorRef, externalKey: String, withPluginInfo: Boolean, auditLevel: String) = {
    log.info("Requesting Payment Method with externalKey: {}", externalKey)

    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[PaymentMethodResult[PaymentMethod]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/paymentMethods?externalKey=$externalKey&audit=$auditLevel&withPluginInfo=$withPluginInfo") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val paymentMethod = new PaymentMethod(response.paymentMethodId, response.externalKey, response.accountId, response.isDefault, response.pluginName, response.pluginInfo)
        originalSender ! paymentMethod
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentMethodById(originalSender: ActorRef, paymentMethodId: UUID, withPluginInfo: Boolean, auditLevel: String) = {
    log.info("Requesting Payment Method with ID: {}", paymentMethodId.toString)

    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[PaymentMethodResult[PaymentMethod]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/paymentMethods/$paymentMethodId?audit=$auditLevel&withPluginInfo=$withPluginInfo") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val paymentMethod = new PaymentMethod(response.paymentMethodId, response.externalKey, response.accountId, response.isDefault, response.pluginName, response.pluginInfo)
        originalSender ! paymentMethod
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def searchPaymentMethods(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String,
                           withPluginInfo: Boolean, pluginName: String) = {
    log.info("Searching All Payment Methods with searchKey=" + searchKey)

    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[PaymentMethodResult[PaymentMethod]]]

    var suffixUrl = ""
    if (!pluginName.equalsIgnoreCase("")) {
      suffixUrl = "&pluginName=" + pluginName
    }

    val responseFuture = pipeline {
      Get(killBillUrl+s"/paymentMethods/search/$searchKey?offset=$offset&limit=$limit&audit=$auditLevel&withPluginInfo=$withPluginInfo" + suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentMethods(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Payment Methods")

    import PaymentMethodJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[PaymentMethodResult[PaymentMethod]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/paymentMethods/pagination?offset=$offset&limit=$limit&audit="+auditLevel) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}