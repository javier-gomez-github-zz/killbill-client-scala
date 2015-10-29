package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

object AccountActor {
  case class GetAccountByExternalKey(externalKey: String, withBalance: Boolean, withCBA: Boolean, audit: String)
  case class GetAccountById(accountId: UUID, withBalance: Boolean, withCBA: Boolean, audit: String)
  case class CreateAccount(account: Account)
  case class UpdateAccount(account: Account, accountId: UUID)
  case class GetAccounts(offset: Long, limit: Long, auditMode: String)
  case class SearchAccounts(searchKey: String, offset: Long, limit: Long, auditMode: String)
  case class GetEmailsForAccount(accountId: UUID)
  case class AddEmailToAccount(accountEmail: AccountEmail, accountId: UUID)
  case class RemoveEmailFromAccount(accountId: UUID, email: String)
  case class GetEmailNotificationsForAccount(accountId: UUID)
  case class UpdateEmailNotificationsForAccount(invoiceEmail: InvoiceEmail, accountId: UUID)
  case class GetAccountTimeline(accountId: UUID, audit: String)
}

class AccountActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import AccountActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetAccountByExternalKey(externalKey, withBalance, withCBA, audit) => {
      getAccountByExternalKey(sender, externalKey, withBalance, withCBA, audit)
      context.stop(self)
    }

    case GetAccountById(accountId, withBalance, withCBA, audit) => {
      getAccountById(sender, accountId, withBalance, withCBA, audit)
      context.stop(self)
    }

    case CreateAccount(account) =>
      createAccount(sender, account)
      context.stop(self)

    case UpdateAccount(account, accountId) =>
      updateAccount(sender, account, accountId)
      context.stop(self)

    case GetAccounts(offset, limit, auditLevel) => {
      getAccounts(sender, offset, limit, auditLevel)
      context.stop(self)
    }

    case SearchAccounts(searchKey, offset, limit, auditLevel) => {
      searchAccounts(sender, searchKey, offset, limit, auditLevel)
      context.stop(self)
    }

    case GetEmailsForAccount(accountId) => {
      getEmailsForAccount(sender, accountId)
      context.stop(self)
    }

    case AddEmailToAccount(accountEmail, accountId) => {
      addEmailToAccount(sender, accountEmail, accountId)
      context.stop(self)
    }

    case RemoveEmailFromAccount(accountId, email) => {
      removeEmailFromAccount(sender, accountId, email)
      context.stop(self)
    }

    case GetEmailNotificationsForAccount(accountId) => {
      getEmailNotificationsForAccount(sender, accountId)
      context.stop(self)
    }

    case UpdateEmailNotificationsForAccount(invoiceEmail, accountId) => {
      updateEmailNotificationsForAccount(sender, invoiceEmail, accountId)
      context.stop(self)
    }


    case GetAccountTimeline(accountId: UUID, audit: String) => {
      getAccountTimeline(sender, accountId, audit)
      context.stop(self)
    }
  }

  def getEmailNotificationsForAccount(originalSender: ActorRef, accountId: UUID) = {
    log.info("Requesting Email Notifications for Account ID: " + accountId.toString)

    import InvoiceEmailJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[InvoiceEmailResult[InvoiceEmail]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/emailNotifications") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def updateEmailNotificationsForAccount(originalSender: ActorRef, invoiceEmail: InvoiceEmail, accountId: UUID) = {
    log.info("Updating Email Notifications For Account " + accountId)

    import InvoiceEmailJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/accounts/$accountId/emailNotifications", invoiceEmail) ~> addHeaders(headers)
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

  def searchAccounts(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Searching All Accounts with searchKey=" + searchKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[AccountResult[Account]]]

    val suffixUrl = "&accountWithBalance=false&accountWithBalanceAndCBA=false&audit=" + auditLevel

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/search/$searchKey?offset=$offset&limit=$limit"+suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getEmailsForAccount(originalSender: ActorRef, accountId: UUID) = {
    log.info("Requesting Emails for Account ID: " + accountId.toString)

    import AccountEmailJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[AccountEmailResult[AccountEmail]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/emails") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def addEmailToAccount(originalSender: ActorRef, accountEmail: AccountEmail, accountId: UUID) = {
    log.info("Adding new Email to Account " + accountId.toString)

    import AccountEmailJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts/$accountId/emails", accountEmail) ~> addHeaders(headers)
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

  def removeEmailFromAccount(originalSender: ActorRef, accountId: UUID, email: String) = {
    log.info("Deleting Email " + email + " from Account " + accountId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/accounts/$accountId/emails/$email") ~> addHeaders(headers)
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

  def getAccounts(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Accounts")

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[AccountResult[Account]]]

    val suffixUrl = "&accountWithBalance=false&accountWithBalanceAndCBA=false&audit=" + auditLevel

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/pagination?offset=$offset&limit=$limit"+suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getAccountById(originalSender: ActorRef, accountId: UUID, withBalance: Boolean, withCBA: Boolean, audit: String) = {
    log.info("Requesting Account with ID: {}", accountId.toString)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[AccountResult[Account]]

    val suffixUrl = "?accountWithBalance=" + withBalance.toString + "&accountWithBalanceAndCBA=" + withCBA.toString + "&audit=" + audit

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId"+suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val account = new Account(response.accountId, response.externalKey, response.accountCBA, response.accountBalance, response.name,
          response.firstNameLength, response.email, response.billCycleDayLocal, response.currency, response.paymentMethodId, response.timeZone,
          response.address1, response.address2, response.postalCode, response.company, response.city, response.state, response.country,
          response.locale, response.phone, response.isMigrated, response.isNotifiedForInvoices)
        originalSender ! account
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getAccountByExternalKey(originalSender: ActorRef, externalKey: String, withBalance: Boolean, withCBA: Boolean, audit: String) = {
    log.info("Requesting Account with externalKey: {}", externalKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[AccountResult[Account]]

    val suffixUrl = "&accountWithBalance=" + withBalance.toString + "&accountWithBalanceAndCBA=" + withCBA.toString + "&audit=" + audit

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts?externalKey=$externalKey"+suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val account = new Account(response.accountId, response.externalKey, response.accountCBA, response.accountBalance, response.name,
          response.firstNameLength, response.email, response.billCycleDayLocal, response.currency, response.paymentMethodId, response.timeZone,
          response.address1, response.address2, response.postalCode, response.company, response.city, response.state, response.country,
          response.locale, response.phone, response.isMigrated, response.isNotifiedForInvoices)
        originalSender ! account
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def createAccount(originalSender: ActorRef, account: Account) = {
    log.info("Creating new Account...")

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts", account) ~> addHeaders(headers)
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

  def updateAccount(originalSender: ActorRef, account: Account, accountId: UUID) = {
    log.info("Updating Account..." + account.externalKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/accounts/$accountId", account) ~> addHeaders(headers)
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

  def getAccountTimeline(originalSender: ActorRef, accountId: UUID, audit: String) = {
    log.info("Requesting Account Timeline for Account with ID: {}", accountId.toString)

    import AccountTimelineJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[AccountTimelineResult[AccountTimeline]]

    val suffixUrl = "?audit=" + audit + "&parallel=false"

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/timeline" + suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val accountTimeline = new AccountTimeline(response.account, response.bundles, response.invoices, response.payments)
        originalSender ! accountTimeline
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
