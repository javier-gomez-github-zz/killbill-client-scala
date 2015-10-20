package org.killbill.billing.client

import akka.actor.Actor
import akka.event.Logging
import org.killbill.billing.client.model.{Account, AccountJsonProtocol, KillbillApiResult}
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport
import spray.routing.RequestContext

import scala.util.{Failure, Success}

object AccountService {
  case class GetAccountByExternalKey(externalKey: String, withBalance: Boolean, withCBA: Boolean, audit: String)
  case class CreateAccount(account: Account)
}

class AccountService(requestContext: RequestContext, killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import AccountService._

  implicit val system = context.system
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetAccountByExternalKey(externalKey, withBalance, withCBA, audit) =>
      getAccountByExternalKey(externalKey, withBalance, withCBA, audit)
      context.stop(self)
    case CreateAccount(account) =>
      createAccount(account)
      context.stop(self)
  }

  def createAccount(account: Account) = {

    log.info("Creating new Account...")

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts", account) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        requestContext.complete(response)
      case Failure(error) =>
        requestContext.complete(error.getMessage())
    }
  }

  def getAccountByExternalKey(externalKey: String, withBalance: Boolean = false, withCBA: Boolean = false, audit: String = "NONE") = {

    log.info("Requesting Account with externalKey: {}", externalKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[KillbillApiResult[Account]]

    val suffixUrl = "&accountWithBalance=" + withBalance.toString + "&accountWithBalanceAndCBA=" + withCBA.toString + "&audit=" + audit

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts?externalKey=$externalKey"+suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(KillbillApiResult(accountId, externalKey, accountCBA, accountBalance, name,
      firstNameLength, email, billCycleDayLocal, currency, paymentMethodId, timeZone,
      address1, address2, postalCode, company, city, state, country, locale, phone,
      isMigrated, isNotifiedForInvoices)) =>
        val account = new Account(accountId, externalKey, accountCBA, accountBalance, name,
          firstNameLength, email, billCycleDayLocal, currency, paymentMethodId, timeZone,
          address1, address2, postalCode, company, city, state, country, locale, phone,
          isMigrated, isNotifiedForInvoices)
        requestContext.complete(account)
      case Failure(error) =>
        requestContext.complete(error.getMessage())
    }
  }
}
