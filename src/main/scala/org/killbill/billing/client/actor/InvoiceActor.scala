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
 * Created by jgomez on 02/11/2015.
 */
object InvoiceActor {
  case class GetInvoices(offset: Long, limit: Long, withItems: Boolean, auditMode: String)
  case class GetInvoiceById(invoiceId: UUID, withItems: Boolean, auditMode: String)
  case class GetInvoiceByNumber(invoiceNumber: Int, withItems: Boolean, auditMode: String)
  case class GetInvoiceByIdOrNumber(invoiceIdOrNumber: String, withItems: Boolean, auditMode: String)
  case class GetInvoicesForAccount(accountId: UUID, withItems: Boolean, unpaidInvoicesOnly: Boolean, auditMode: String)
  case class SearchInvoices(searchKey: String, offset: Long, limit: Long, withItems: Boolean, auditMode: String)
  case class CreateInvoice(accountId: UUID, futureDate: String)
  case class CreateDryRunInvoice(accountId: UUID, futureDate: String, dryRunInfo: InvoiceDryRun)
  case class AdjustInvoiceItem(invoiceId: UUID, requestedDate: String, invoiceItem: InvoiceItem)
  case class CreateExternalCharge(accountId: UUID, requestedDate: String, autoPay: Boolean, externalCharges: List[InvoiceItem])
  case class TriggerInvoiceNotification(invoiceId: UUID)
  case class UploadInvoiceTemplate(invoiceTemplate: String, manualPay: Boolean)
  case class GetInvoiceTemplate(manualPay: Boolean)
  case class UploadInvoiceTranslation(invoiceTemplate: String, locale: String)
  case class GetInvoiceTranslation(locale: String)
  case class UploadCatalogTranslation(invoiceTemplate: String, locale: String)
  case class GetCatalogTranslation(locale: String)
}

case class InvoiceActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import InvoiceActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)
  def sendAndReceive = sendReceive

  def receive = {
    case GetInvoices(offset, limit, withItems, auditLevel) =>
      getInvoices(sender, offset, limit, withItems, auditLevel)
      context.stop(self)

    case GetInvoiceById(invoiceId, withItems, audit) =>
      getInvoiceById(sender, invoiceId, withItems, audit)
      context.stop(self)

    case GetInvoiceByNumber(invoiceNumber, withItems, auditMode) =>
      getInvoiceByNumber(sender, invoiceNumber, withItems, auditMode)
      context.stop(self)

    case GetInvoiceByIdOrNumber(invoiceIdOrNumber, withItems, auditMode) =>
      getInvoiceByIdOrNumber(sender, invoiceIdOrNumber, withItems, auditMode)
      context.stop(self)

    case GetInvoicesForAccount(accountId, withItems, unpaidInvoicesOnly, auditMode) =>
      getInvoicesForAccount(sender, accountId, withItems, unpaidInvoicesOnly, auditMode)
      context.stop(self)

    case SearchInvoices(searchKey, offset, limit, withItems, auditLevel) =>
      searchInvoices(sender, searchKey, offset, limit, withItems, auditLevel)
      context.stop(self)

    case CreateInvoice(accountId, futureDate) =>
      createInvoice(sender, accountId, futureDate)
      context.stop(self)

    case CreateDryRunInvoice(accountId, futureDate, dryRunInfo) =>
      createDryRunInvoice(sender, accountId, futureDate, dryRunInfo)
      context.stop(self)

    case AdjustInvoiceItem(invoiceId, requestedDate, invoiceItem) =>
      adjustInvoiceItem(sender, invoiceId, requestedDate, invoiceItem)
      context.stop(self)

    case CreateExternalCharge(accountId, requestedDate, autoPay, externalCharges) =>
      createExternalCharges(sender, accountId, requestedDate, autoPay, externalCharges)
      context.stop(self)

    case TriggerInvoiceNotification(invoiceId) =>
      triggerInvoiceNotification(sender, invoiceId)
      context.stop(self)

    case UploadInvoiceTemplate(invoiceTemplate, manualPay) =>
      uploadInvoiceTemplate(sender, invoiceTemplate, manualPay)
      context.stop(self)

    case GetInvoiceTemplate(manualPay) =>
      getInvoiceTemplate(sender, manualPay)
      context.stop(self)

    case UploadInvoiceTranslation(invoiceTemplate, locale) =>
      uploadInvoiceTranslation(sender, invoiceTemplate, locale)
      context.stop(self)

    case GetInvoiceTranslation(locale) =>
      getInvoiceTranslation(sender, locale)
      context.stop(self)

    case UploadCatalogTranslation(invoiceTemplate, locale) =>
      uploadCatalogTranslation(sender, invoiceTemplate, locale)
      context.stop(self)

    case GetCatalogTranslation(locale) =>
      getCatalogTranslation(sender, locale)
      context.stop(self)
  }

  def getCatalogTranslation(originalSender: ActorRef, locale: String) = {
    log.info("Getting Catalog Translation...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Get(killBillUrl + s"/invoices/catalogTranslation/$locale") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        if (response.status.toString().contains("200")) {
          originalSender ! response.entity
        }
        else if (response.status.toString().contains("404")) {
          originalSender ! "Catalog Translations not found"
        }
        else {
          originalSender ! response.status.toString()
        }
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def uploadCatalogTranslation(originalSender: ActorRef, invoiceTemplate: String, locale: String) = {
    log.info("Uploading Catalog Translation...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl + s"/invoices/catalogTranslation/$locale", invoiceTemplate) ~> addHeaders(headers)
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

  def getInvoiceTranslation(originalSender: ActorRef, locale: String) = {
    log.info("Getting Invoice Translation...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Get(killBillUrl + s"/invoices/translation/$locale") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        if (response.status.toString().contains("200")) {
          originalSender ! response.entity
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

  def uploadInvoiceTranslation(originalSender: ActorRef, invoiceTemplate: String, locale: String) = {
    log.info("Uploading Invoice Translation...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl + s"/invoices/translation/$locale", invoiceTemplate) ~> addHeaders(headers)
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

  def getInvoiceTemplate(originalSender: ActorRef, manualPay: Boolean) = {
    log.info("Getting Invoice Template...")

    val pipeline = sendAndReceive

    var suffixUrl = ""
    if (manualPay) {
      suffixUrl = "manualPayTemplate"
    }
    else suffixUrl = "template"

    val responseFuture = pipeline {
      Get(killBillUrl + s"/invoices/$suffixUrl") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.entity
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def uploadInvoiceTemplate(originalSender: ActorRef, invoiceTemplate: String, manualPay: Boolean) = {
    log.info("Uploading Invoice Template...")

    val pipeline = sendAndReceive

    var suffixUrl = ""
    if (manualPay) {
      suffixUrl = "manualPayTemplate"
    }
    else suffixUrl = "template"

    val responseFuture = pipeline {
      Post(killBillUrl + s"/invoices/$suffixUrl", invoiceTemplate) ~> addHeaders(headers)
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

  def triggerInvoiceNotification(originalSender: ActorRef, invoiceId: UUID) = {
    log.info("Triggering Invoice Notification Email to Invoice: " + invoiceId.toString)

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl + s"/invoices/$invoiceId/emailNotifications") ~> addHeaders(headers)
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

  def createExternalCharges(originalSender: ActorRef, accountId: UUID, requestedDate: String, autoPay: Boolean, externalCharges: List[InvoiceItem]) = {
    log.info("Creating External Charge(s) for Account=" + accountId.toString)

    import InvoiceItemJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[InvoiceItemResult[InvoiceItem]]]

    var suffixUrl = ""
    if (!requestedDate.equalsIgnoreCase("")) {
      suffixUrl = "?requestedDate=" + requestedDate
    }

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoices/charges/$accountId?payInvoice=$autoPay" + suffixUrl, externalCharges) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def adjustInvoiceItem(originalSender: ActorRef, invoiceId: UUID, requestedDate: String, invoiceItem: InvoiceItem) = {
    log.info("Adjusting Invoice Item: " + invoiceId.toString)

    import InvoiceItemJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive

    var suffixUrl = ""
    if (!requestedDate.equalsIgnoreCase("")) {
      suffixUrl = "?requestedDate=" + requestedDate
    }

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoices/$invoiceId" + suffixUrl, invoiceItem) ~> addHeaders(headers)
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

  def createDryRunInvoice(originalSender: ActorRef, accountId: UUID, futureDate: String, dryRunInfo: InvoiceDryRun) = {
    log.info("Creating new Dry Run Invoice...")

    import InvoiceDryRunJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoices/dryRun?accountId=$accountId&targetDate=$futureDate", dryRunInfo) ~> addHeaders(headers)
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

  def createInvoice(originalSender: ActorRef, accountId: UUID, futureDate: String) = {
    log.info("Creating new Invoice...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/invoices?accountId=$accountId&targetDate=$futureDate") ~> addHeaders(headers)
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

  def searchInvoices(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, withItems: Boolean, auditLevel: String) = {
    log.info("Searching All Invoices with searchKey=" + searchKey)

    import InvoiceJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[InvoiceResult[Invoice]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/invoices/search/$searchKey?offset=$offset&limit=$limit&withItems=$withItems&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getInvoicesForAccount(originalSender: ActorRef, accountId: UUID, withItems: Boolean, unpaidInvoicesOnly: Boolean, auditLevel: String) = {
    log.info("Requesting All Invoices for Account: " + accountId.toString)

    import InvoiceJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[InvoiceResult[Invoice]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/invoices?withItems=$withItems&unpaidInvoicesOnly=$unpaidInvoicesOnly&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getInvoiceByIdOrNumber(originalSender: ActorRef, invoiceIdOrNumber: String, withItems: Boolean, audit: String) = {
    log.info("Requesting Invoice with ID or Number: {}", invoiceIdOrNumber.toString)

    import InvoiceJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[InvoiceResult[Invoice]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/invoices/$invoiceIdOrNumber/?withItems=$withItems&audit=$audit") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val invoice = new Invoice(response.amount, response.currency, response.invoiceId, response.invoiceDate, response.targetDate,
          response.invoiceNumber, response.balance, response.creditAdj, response.refundAdj, response.accountId, response.items,
          response.bundleKeys, response.credits)
        originalSender ! invoice
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
  
  def getInvoiceByNumber(originalSender: ActorRef, invoiceNumber: Int, withItems: Boolean, audit: String) = {
    log.info("Requesting Invoice with Number: {}", invoiceNumber.toString)

    import InvoiceJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[InvoiceResult[Invoice]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/invoices/$invoiceNumber/?withItems=$withItems&audit=$audit") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val invoice = new Invoice(response.amount, response.currency, response.invoiceId, response.invoiceDate, response.targetDate,
          response.invoiceNumber, response.balance, response.creditAdj, response.refundAdj, response.accountId, response.items,
          response.bundleKeys, response.credits)
        originalSender ! invoice
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getInvoiceById(originalSender: ActorRef, invoiceId: UUID, withItems: Boolean, audit: String) = {
    log.info("Requesting Invoice with ID: {}", invoiceId.toString)

    import InvoiceJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[InvoiceResult[Invoice]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/invoices/$invoiceId/?withItems=$withItems&audit=$audit") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val invoice = new Invoice(response.amount, response.currency, response.invoiceId, response.invoiceDate, response.targetDate,
          response.invoiceNumber, response.balance, response.creditAdj, response.refundAdj, response.accountId, response.items,
          response.bundleKeys, response.credits)
        originalSender ! invoice
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getInvoices(originalSender: ActorRef, offset: Long, limit: Long, withItems: Boolean, auditLevel: String) = {
    log.info("Requesting All Invoices")

    import InvoiceJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[InvoiceResult[Invoice]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/invoices/pagination?offset=$offset&limit=$limit&withItems=$withItems&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
