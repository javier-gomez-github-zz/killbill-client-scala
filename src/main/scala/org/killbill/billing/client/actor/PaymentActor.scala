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
  //  case class GetInvoiceByNumber(invoiceNumber: Int, withItems: Boolean, auditMode: String)
  //  case class GetInvoiceByIdOrNumber(invoiceIdOrNumber: String, withItems: Boolean, auditMode: String)
//  case class CreateInvoice(accountId: UUID, futureDate: String)
//  case class CreateDryRunInvoice(accountId: UUID, futureDate: String, dryRunInfo: InvoiceDryRun)
//  case class AdjustInvoiceItem(invoiceId: UUID, requestedDate: String, invoiceItem: InvoiceItem)
//  case class CreateExternalCharge(accountId: UUID, requestedDate: String, autoPay: Boolean, externalCharges: List[InvoiceItem])
//  case class TriggerInvoiceNotification(invoiceId: UUID)
//  case class UploadInvoiceTemplate(invoiceTemplate: String, manualPay: Boolean)
//  case class GetInvoiceTemplate(manualPay: Boolean)
//  case class UploadInvoiceTranslation(invoiceTemplate: String, locale: String)
//  case class GetInvoiceTranslation(locale: String)
//  case class UploadCatalogTranslation(invoiceTemplate: String, locale: String)
//  case class GetCatalogTranslation(locale: String)
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


    //    case GetInvoiceByNumber(invoiceNumber, withItems, auditMode) =>
    //      getInvoiceByNumber(sender, invoiceNumber, withItems, auditMode)
    //      context.stop(self)
    //
    //    case GetInvoiceByIdOrNumber(invoiceIdOrNumber, withItems, auditMode) =>
    //      getInvoiceByIdOrNumber(sender, invoiceIdOrNumber, withItems, auditMode)
    //      context.stop(self)
    //
    //
//
//    case CreateInvoice(accountId, futureDate) =>
//      createInvoice(sender, accountId, futureDate)
//      context.stop(self)
//
//    case CreateDryRunInvoice(accountId, futureDate, dryRunInfo) =>
//      createDryRunInvoice(sender, accountId, futureDate, dryRunInfo)
//      context.stop(self)
//
//    case AdjustInvoiceItem(invoiceId, requestedDate, invoiceItem) =>
//      adjustInvoiceItem(sender, invoiceId, requestedDate, invoiceItem)
//      context.stop(self)
//
//    case CreateExternalCharge(accountId, requestedDate, autoPay, externalCharges) =>
//      createExternalCharges(sender, accountId, requestedDate, autoPay, externalCharges)
//      context.stop(self)
//
//    case TriggerInvoiceNotification(invoiceId) =>
//      triggerInvoiceNotification(sender, invoiceId)
//      context.stop(self)
//
//    case UploadInvoiceTemplate(invoiceTemplate, manualPay) =>
//      uploadInvoiceTemplate(sender, invoiceTemplate, manualPay)
//      context.stop(self)
//
//    case GetInvoiceTemplate(manualPay) =>
//      getInvoiceTemplate(sender, manualPay)
//      context.stop(self)
//
//    case UploadInvoiceTranslation(invoiceTemplate, locale) =>
//      uploadInvoiceTranslation(sender, invoiceTemplate, locale)
//      context.stop(self)
//
//    case GetInvoiceTranslation(locale) =>
//      getInvoiceTranslation(sender, locale)
//      context.stop(self)
//
//    case UploadCatalogTranslation(invoiceTemplate, locale) =>
//      uploadCatalogTranslation(sender, invoiceTemplate, locale)
//      context.stop(self)
//
//    case GetCatalogTranslation(locale) =>
//      getCatalogTranslation(sender, locale)
//      context.stop(self)
  }

//  def getCatalogTranslation(originalSender: ActorRef, locale: String) = {
//    log.info("Getting Catalog Translation...")
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Get(killBillUrl + s"/invoices/catalogTranslation/$locale") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        if (response.status.toString().contains("200")) {
//          originalSender ! response.entity
//        }
//        else if (response.status.toString().contains("404")) {
//          originalSender ! "Catalog Translations not found"
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      case Failure(error) =>
//        originalSender ! error.getMessage
//    }
//  }
//
//  def uploadCatalogTranslation(originalSender: ActorRef, invoiceTemplate: String, locale: String) = {
//    log.info("Uploading Catalog Translation...")
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Post(killBillUrl + s"/invoices/catalogTranslation/$locale", invoiceTemplate) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("200")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//
//  def getInvoiceTranslation(originalSender: ActorRef, locale: String) = {
//    log.info("Getting Invoice Translation...")
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Get(killBillUrl + s"/invoices/translation/$locale") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        if (response.status.toString().contains("200")) {
//          originalSender ! response.entity
//        }
//        else if (response.status.toString().contains("404")) {
//          originalSender ! "Invoice Translations not found"
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      case Failure(error) =>
//        originalSender ! error.getMessage
//    }
//  }
//
//  def uploadInvoiceTranslation(originalSender: ActorRef, invoiceTemplate: String, locale: String) = {
//    log.info("Uploading Invoice Translation...")
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Post(killBillUrl + s"/invoices/translation/$locale", invoiceTemplate) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("200")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//
//  def getInvoiceTemplate(originalSender: ActorRef, manualPay: Boolean) = {
//    log.info("Getting Invoice Template...")
//
//    val pipeline = sendReceive
//
//    var suffixUrl = ""
//    if (manualPay) {
//      suffixUrl = "manualPayTemplate"
//    }
//    else suffixUrl = "template"
//
//    val responseFuture = pipeline {
//      Get(killBillUrl + s"/invoices/$suffixUrl") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        originalSender ! response.entity
//      case Failure(error) =>
//        originalSender ! error.getMessage
//    }
//  }
//
//  def uploadInvoiceTemplate(originalSender: ActorRef, invoiceTemplate: String, manualPay: Boolean) = {
//    log.info("Uploading Invoice Template...")
//
//    val pipeline = sendReceive
//
//    var suffixUrl = ""
//    if (manualPay) {
//      suffixUrl = "manualPayTemplate"
//    }
//    else suffixUrl = "template"
//
//    val responseFuture = pipeline {
//      Post(killBillUrl + s"/invoices/$suffixUrl", invoiceTemplate) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("200")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//
//  def triggerInvoiceNotification(originalSender: ActorRef, invoiceId: UUID) = {
//    log.info("Triggering Invoice Notification Email to Invoice: " + invoiceId.toString)
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Post(killBillUrl + s"/invoices/$invoiceId/emailNotifications") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("200")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//
//  def createExternalCharges(originalSender: ActorRef, accountId: UUID, requestedDate: String, autoPay: Boolean, externalCharges: List[InvoiceItem]) = {
//    log.info("Creating External Charge(s) for Account=" + accountId.toString)
//
//    import InvoiceItemJsonProtocol._
//    import SprayJsonSupport._
//
//    val pipeline = sendReceive ~> unmarshal[List[InvoiceItemResult[InvoiceItem]]]
//
//    var suffixUrl = ""
//    if (!requestedDate.equalsIgnoreCase("")) {
//      suffixUrl = "?requestedDate=" + requestedDate
//    }
//
//    val responseFuture = pipeline {
//      Post(killBillUrl+s"/invoices/charges/$accountId?payInvoice=$autoPay" + suffixUrl, externalCharges) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        originalSender ! response
//      case Failure(error) =>
//        originalSender ! error.getMessage
//    }
//  }
//
//  def adjustInvoiceItem(originalSender: ActorRef, invoiceId: UUID, requestedDate: String, invoiceItem: InvoiceItem) = {
//    log.info("Adjusting Invoice Item: " + invoiceId.toString)
//
//    import InvoiceItemJsonProtocol._
//    import SprayJsonSupport._
//
//    val pipeline = sendReceive
//
//    var suffixUrl = ""
//    if (!requestedDate.equalsIgnoreCase("")) {
//      suffixUrl = "?requestedDate=" + requestedDate
//    }
//
//    val responseFuture = pipeline {
//      Post(killBillUrl+s"/invoices/$invoiceId" + suffixUrl, invoiceItem) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("201")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//
//  def createDryRunInvoice(originalSender: ActorRef, accountId: UUID, futureDate: String, dryRunInfo: InvoiceDryRun) = {
//    log.info("Creating new Dry Run Invoice...")
//
//    import InvoiceDryRunJsonProtocol._
//    import SprayJsonSupport._
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Post(killBillUrl+s"/invoices/dryRun?accountId=$accountId&targetDate=$futureDate", dryRunInfo) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("201")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//
//  def createInvoice(originalSender: ActorRef, accountId: UUID, futureDate: String) = {
//    log.info("Creating new Invoice...")
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Post(killBillUrl+s"/invoices?accountId=$accountId&targetDate=$futureDate") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) => {
//        if (!response.status.toString().contains("201")) {
//          originalSender ! response.entity.asString
//        }
//        else {
//          originalSender ! response.status.toString()
//        }
//      }
//      case Failure(error) => {
//        originalSender ! error.getMessage()
//      }
//    }
//  }
//

//
//
//
//  def getInvoiceByIdOrNumber(originalSender: ActorRef, invoiceIdOrNumber: String, withItems: Boolean, audit: String) = {
//    log.info("Requesting Invoice with ID or Number: {}", invoiceIdOrNumber.toString)
//
//    import InvoiceJsonProtocol._
//    import SprayJsonSupport._
//
//    val pipeline = sendReceive ~> unmarshal[InvoiceResult[Invoice]]
//
//    val responseFuture = pipeline {
//      Get(killBillUrl+s"/invoices/$invoiceIdOrNumber/?withItems=$withItems&audit=$audit") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        val invoice = new Invoice(response.amount, response.currency, response.invoiceId, response.invoiceDate, response.targetDate,
//          response.invoiceNumber, response.balance, response.creditAdj, response.refundAdj, response.accountId, response.items,
//          response.bundleKeys, response.credits)
//        originalSender ! invoice
//      case Failure(error) =>
//        originalSender ! error.getMessage
//    }
//  }
//
//  def getInvoiceByNumber(originalSender: ActorRef, invoiceNumber: Int, withItems: Boolean, audit: String) = {
//    log.info("Requesting Invoice with Number: {}", invoiceNumber.toString)
//
//    import InvoiceJsonProtocol._
//    import SprayJsonSupport._
//
//    val pipeline = sendReceive ~> unmarshal[InvoiceResult[Invoice]]
//
//    val responseFuture = pipeline {
//      Get(killBillUrl+s"/invoices/$invoiceNumber/?withItems=$withItems&audit=$audit") ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        val invoice = new Invoice(response.amount, response.currency, response.invoiceId, response.invoiceDate, response.targetDate,
//          response.invoiceNumber, response.balance, response.creditAdj, response.refundAdj, response.accountId, response.items,
//          response.bundleKeys, response.credits)
//        originalSender ! invoice
//      case Failure(error) =>
//        originalSender ! error.getMessage
//    }
//  }

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
