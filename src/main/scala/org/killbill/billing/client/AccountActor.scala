package org.killbill.billing.client

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import org.killbill.billing.client.model.{Account, AccountJsonProtocol, KillbillApiResult}
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport
import spray.routing.RequestContext

import scala.concurrent.Future
import scala.util.{Failure, Success}

object AccountActor {
  case class GetAccountByExternalKey(externalKey: String, withBalance: Boolean, withCBA: Boolean, audit: String)
  case class CreateAccount(account: Account)
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

//    case CreateAccount(account) =>
//      createAccount(account)
//      context.stop(self)
  }

  def getAccountByExternalKey(originalSender: ActorRef, externalKey: String, withBalance: Boolean, withCBA: Boolean, audit: String) = {
    log.info("Requesting Account with externalKey: {}", externalKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[Account]

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
        println("")
    }
  }

//  def createAccount(account: Account) = {
//
//    log.info("Creating new Account...")
//
//    import AccountJsonProtocol._
//    import SprayJsonSupport._
//
//    val pipeline = sendReceive
//
//    val responseFuture = pipeline {
//      Post(killBillUrl+s"/accounts", account) ~> addHeaders(headers)
//    }
//    responseFuture.onComplete {
//      case Success(response) =>
//        requestContext.complete(response)
//      case Failure(error) =>
//        requestContext.complete(error.getMessage())
//    }
//  }
}
