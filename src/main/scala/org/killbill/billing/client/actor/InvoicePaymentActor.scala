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
 * Created by jgomez on 06/11/2015.
 */
object InvoicePaymentActor {
  case class GetInvoicePaymentsForAccount(accountId: UUID, auditLevel: String, withPluginInfo: String)
  case class GetInvoicePayment(invoiceId: UUID)
  case class PayAllInvoices(accountId: UUID, externalPayment: Boolean, paymentAmount: BigDecimal)
  case class CreateInvoicePayment(invoiceId: UUID, invoicePaymet: InvoicePayment, isExternal: Boolean)
  case class CreateInvoicePaymentRefund(paymentId: UUID, refundTransaction: InvoicePaymentTransaction)
  case class CreateInvoicePaymentChargeback(paymentId: UUID, chargebackTransaction: InvoicePaymentTransaction)

}

case class InvoicePaymentActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import InvoicePaymentActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetInvoicePaymentsForAccount(accountId, auditLevel, withPluginInfo) =>
      getInvoicePaymentsForAccount(sender, accountId, auditLevel, withPluginInfo)
      context.stop(self)

    case GetInvoicePayment(invoiceId) =>
      getInvoicePayment(sender, invoiceId)
      context.stop(self)

    case PayAllInvoices(accountId, externalPayment, paymentAmount) =>
      payAllInvoices(sender, accountId, externalPayment, paymentAmount)
      context.stop(self)

    case CreateInvoicePayment(invoiceId, invoicePayment, isExternal) =>
      createInvoicePayment(sender, invoiceId, invoicePayment, isExternal)
      context.stop(self)

    case CreateInvoicePaymentRefund(paymentId, refundTransaction) =>
      createInvoicePaymentRefund(sender, paymentId, refundTransaction)
      context.stop(self)

    case CreateInvoicePaymentChargeback(paymentId, chargebackTransaction) =>
      createInvoicePaymentChargeback(sender, paymentId, chargebackTransaction)
      context.stop(self)
  }

  def createInvoicePaymentChargeback(originalSender: ActorRef, paymentId: UUID, chargebackTransaction: InvoicePaymentTransaction) = {
    log.info("Creating an Invoice Payment Chargeback...")

    import InvoicePaymentTransactionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoicePayments/$paymentId/chargebacks", chargebackTransaction) ~> addHeaders(headers)
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

  def createInvoicePaymentRefund(originalSender: ActorRef, paymentId: UUID, refundTransaction: InvoicePaymentTransaction) = {
    log.info("Creating an Invoice Payment Refund...")

    import InvoicePaymentTransactionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoicePayments/$paymentId/refunds", refundTransaction) ~> addHeaders(headers)
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

  def createInvoicePayment(originalSender: ActorRef, invoiceId: UUID, invoicePayment: InvoicePayment, isExternal: Boolean) = {
    log.info("Creating an Invoice Payment...")

    import InvoicePaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoices/$invoiceId/payments", invoicePayment) ~> addHeaders(headers)
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

  def payAllInvoices(originalSender: ActorRef, accountId: UUID, externalPayment: Boolean, paymentAmount: BigDecimal) = {
    log.info("Attempting to Pay All Invoices for Account: " + accountId.toString)

    val pipeline = sendReceive

    var suffixUrl = ""
    if (paymentAmount != null) {
      suffixUrl = "&paymentAmount" + paymentAmount
    }

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/invoicePayments?externalPayment=$externalPayment" + suffixUrl) ~> addHeaders(headers)
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

  def getInvoicePayment(originalSender: ActorRef, invoiceId: UUID) = {
    log.info("Requesting Invoice Payments for Id: " + invoiceId.toString)

    import InvoicePaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[InvoicePaymentResult[InvoicePayment]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/invoices/$invoiceId/payments") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getInvoicePaymentsForAccount(originalSender: ActorRef, accountId: UUID, auditLevel: String, withPluginInfo: String) = {
    log.info("Requesting Invoice Payments For Account: " + accountId.toString)

    import InvoicePaymentJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[InvoicePaymentResult[InvoicePayment]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/invoicePayments?audit=$auditLevel&withPluginInfo=$withPluginInfo") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
