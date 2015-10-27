package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model.{Account, AccountJsonProtocol, KillbillApiResult}
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

object AccountActor {
  case class GetAccountByExternalKey(externalKey: String, withBalance: Boolean, withCBA: Boolean, audit: String)
  case class GetAccountById(accountId: UUID, withBalance: Boolean, withCBA: Boolean, audit: String)
  case class CreateAccount(account: Account)
  case class GetAccounts(offset: Long, limit: Long, auditMode: String)
  case class SearchAccounts(searchKey: String, offset: Long, limit: Long, auditMode: String)
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

    case GetAccounts(offset, limit, auditLevel) => {
      getAccounts(sender, offset, limit, auditLevel)
      context.stop(self)
    }

    case SearchAccounts(searchKey, offset, limit, auditLevel) => {
      searchAccounts(sender, searchKey, offset, limit, auditLevel)
      context.stop(self)
    }
  }

  def searchAccounts(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Searching All Accounts with searchKey=" + searchKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[KillbillApiResult[Account]]]

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

  def getAccounts(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Accounts")

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[KillbillApiResult[Account]]]

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

    val pipeline = sendReceive ~> unmarshal[KillbillApiResult[Account]]

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

    val pipeline = sendReceive ~> unmarshal[KillbillApiResult[Account]]

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
}
