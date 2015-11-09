package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 05/11/2015.
  */
object PaymentActor {
  case class GetPayments(offset: Long, limit: Long, pluginName: String, pluginProperties: Map[String, String],
                         auditMode: String, withPluginInfo: Boolean)
  case class GetPaymentById(paymentId: UUID, withPluginInfo: Boolean, pluginProperties: Map[String, String], auditMode: String)
  case class SearchPayments(searchKey: String, offset: Long, limit: Long, auditMode: String, withPluginInfo: Boolean)
  case class GetPaymentsForAccount(accountId: UUID, auditMode: String)
  case class CreateComboPayment(comboPaymentTransaction: ComboPaymentTransaction, controlPluginNames: List[String],
                                pluginProperties: Map[String, String])
  case class CreatePayment(accountId: UUID, paymentMethodId: UUID, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String])
  case class CompletePayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String])
  case class CaptureAuthorization(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String])
  case class RefundPayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String])
  case class ChargebackPayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String])
  case class VoidPayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String])
}

case class PaymentActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import PaymentActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetPayments(offset, limit, pluginName, pluginProperties, auditLevel, withPluginInfo) =>
      getPayments(sender, offset, limit, pluginName, pluginProperties, auditLevel, withPluginInfo)
      context.stop(self)

    case GetPaymentById(paymentId, withPluginInfo, pluginProperties, audit) =>
      getPaymentById(sender, paymentId, withPluginInfo, pluginProperties, audit)
      context.stop(self)

    case SearchPayments(searchKey, offset, limit, auditLevel, withPluginInfo) =>
      searchPayments(sender, searchKey, offset, limit, auditLevel, withPluginInfo)
      context.stop(self)

    case GetPaymentsForAccount(accountId, auditMode) =>
      getPaymentsForAccount(sender, accountId, auditMode)
      context.stop(self)

    case CreateComboPayment(comboPaymentTransaction, controlPluginNames, pluginProperties) =>
      createComboPayment(sender, comboPaymentTransaction, controlPluginNames, pluginProperties)
      context.stop(self)

    case CreatePayment(accountId, paymentMethodId, paymentTransaction, pluginProperties) =>
      createPayment(sender, accountId, paymentMethodId, paymentTransaction, pluginProperties)
      context.stop(self)

    case CompletePayment(paymentTransaction, pluginProperties) =>
      completePayment(sender, paymentTransaction, pluginProperties)
      context.stop(self)

    case CaptureAuthorization(paymentTransaction, pluginProperties) =>
      captureAuthorization(sender, paymentTransaction, pluginProperties)
      context.stop(self)

    case RefundPayment(paymentTransaction, pluginProperties) =>
      refundPayment(sender, paymentTransaction, pluginProperties)
      context.stop(self)

    case ChargebackPayment(paymentTransaction, pluginProperties) =>
      chargebackPayment(sender, paymentTransaction, pluginProperties)
      context.stop(self)

    case VoidPayment(paymentTransaction, pluginProperties) =>
      voidPayment(sender, paymentTransaction, pluginProperties)
      context.stop(self)
  }

  def voidPayment(originalSender: ActorRef, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String]) = {
    log.info("Voiding Payment...")

    var url = ""
    if(paymentTransaction.paymentId != null) {
      val paymentId = paymentTransaction.paymentId.mkString
      url = killBillUrl + s"/payments/$paymentId"
    }
    else url = killBillUrl + "/payments"

    doActionWithPayment(originalSender, url, "Delete", paymentTransaction)
  }

  def chargebackPayment(originalSender: ActorRef, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String]) = {
    log.info("Charging back Payment...")

    var url = ""
    if(paymentTransaction.paymentId != null) {
      val paymentId = paymentTransaction.paymentId.mkString
      url = killBillUrl + s"/payments/$paymentId/chargebacks"
    }
    else url = killBillUrl + "/payments/chargebacks"

    doActionWithPayment(originalSender, url, "Post", paymentTransaction)
  }

  def refundPayment(originalSender: ActorRef, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String]) = {
    log.info("Refunding Payment...")

    var url = ""
    if(paymentTransaction.paymentId != null) {
      val paymentId = paymentTransaction.paymentId.mkString
      url = killBillUrl + s"/payments/$paymentId/refunds"
    }
    else url = killBillUrl + "/payments/refunds"

    doActionWithPayment(originalSender, url, "Post", paymentTransaction)
  }

  def captureAuthorization(originalSender: ActorRef, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String]) = {
    log.info("Capturing Authorization...")

    var url = ""
    if(paymentTransaction.paymentId != null) {
      val paymentId = paymentTransaction.paymentId.mkString
      url = killBillUrl + s"/payments/$paymentId"
    }
    else url = killBillUrl + "/payments"

    doActionWithPayment(originalSender, url, "Post", paymentTransaction)
  }

  def completePayment(originalSender: ActorRef, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String]) = {
    log.info("Completing Payment...")

    var url = ""
    if(paymentTransaction.paymentId != null) {
      val paymentId = paymentTransaction.paymentId.mkString
      url = killBillUrl + s"/payments/$paymentId"
    }
    else url = killBillUrl + "/payments"

    doActionWithPayment(originalSender, url, "Put", paymentTransaction)
  }

  def doActionWithPayment(originalSender: ActorRef, url: String, action: String, paymentTransaction: PaymentTransaction) = {
    import PaymentTransactionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      action match {
        case "Put" => Put(url, paymentTransaction) ~> addHeaders(headers)
        case "Post" => Post(url, paymentTransaction) ~> addHeaders(headers)
        case "Delete" => Delete(url, paymentTransaction) ~> addHeaders(headers)
      }
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

  def createPayment(originalSender: ActorRef, accountId: UUID, paymentMethodId: UUID, paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String]) = {
    log.info("Creating Payment...")

    import ResponseUriJsonProtocol._
    import PaymentTransactionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[ResponseUriResult[ResponseUri]]

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts/$accountId/payments", paymentTransaction) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        getPaymentWithUrl(originalSender, response.uri.mkString)
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
  
  def getPaymentWithUrl(originalSender: ActorRef, uri: Any) = {
    import PaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[PaymentResult[Payment]]

    val responseFuture = pipeline {
      Get(uri.asInstanceOf[String]) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val payment = new Payment(response.accountId, response.paymentId, response.paymentNumber, response.paymentExternalKey,
          response.authAmount, response.capturedAmount, response.purchasedAmount, response.refundedAmount, response.creditedAmount,
          response.currency, response.paymentMethodId, response.transactions)
        originalSender ! payment
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def createComboPayment(originalSender: ActorRef, comboPaymentTransaction: ComboPaymentTransaction, controlPluginNames: List[String],
                         pluginProperties: Map[String, String]) = {
    log.info("Creating a new Payment Transaction on an existing (or not) account...")

    import PaymentJsonProtocol._
    import ComboPaymentTransactionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[PaymentResult[Payment]]

    val responseFuture = pipeline {
      Post(killBillUrl+s"/payments/combo?controlPluginName=$controlPluginNames", comboPaymentTransaction) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val payment = new Payment(response.accountId, response.paymentId, response.paymentNumber, response.paymentExternalKey,
          response.authAmount, response.capturedAmount, response.purchasedAmount, response.refundedAmount, response.creditedAmount,
          response.currency, response.paymentMethodId, response.transactions)
        originalSender ! payment
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentsForAccount(originalSender: ActorRef, accountId: UUID, auditLevel: String) = {
    log.info("Requesting All Payments for Account: " + accountId.toString)

    import PaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[PaymentResult[Payment]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/payments?audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def searchPayments(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String, withPluginInfo: Boolean) = {
    log.info("Searching All Payments with searchKey=" + searchKey)

    import PaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[PaymentResult[Payment]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/payments/search/$searchKey?offset=$offset&limit=$limit&audit=$auditLevel&withPluginInfo=$withPluginInfo") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPaymentById(originalSender: ActorRef, paymentId: UUID, withPluginInfo: Boolean,
                     pluginProperties: Map[String, String], audit: String) = {
    log.info("Requesting Payment with ID: {}", paymentId.toString)

    import PaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[PaymentResult[Payment]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/payments/$paymentId/?withPluginInfo=$withPluginInfo&audit=$audit") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val payment = new Payment(response.accountId, response.paymentId, response.paymentNumber, response.paymentExternalKey,
          response.authAmount, response.capturedAmount, response.purchasedAmount, response.refundedAmount, response.creditedAmount,
          response.currency, response.paymentMethodId, response.transactions)
        originalSender ! payment
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPayments(originalSender: ActorRef, offset: Long, limit: Long, pluginName: String,
                  pluginProperties: Map[String, String], auditLevel: String, withPluginInfo: Boolean) = {
    log.info("Requesting All Payments")

    import PaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[PaymentResult[Payment]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/payments/pagination?offset=$offset&limit=$limit&pluginName=$pluginName&" +
        s"pluginProperty=$pluginProperties&audit=$auditLevel&withPluginInfo=$withPluginInfo") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
