package org.killbill.api.client.scala

import akka.actor.Actor
import akka.event.Logging
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport
import spray.routing.RequestContext

import scala.util.{Failure, Success}

object AccountService {
  case class Process(externalKey: String)
}

class AccountService(requestContext: RequestContext) extends Actor {

  import AccountService._

  implicit val system = context.system
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case Process(externalKey) =>
      process(externalKey)
      context.stop(self)
  }

  def process(externalKey: String) = {

    log.info("Requesting Account with externalKey:: {}", externalKey)

    import AccountJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[KillbillApiResult[Account]]

    val responseFuture = pipeline {
      Get(s"http://localhost:8080/1.0/kb/accounts?externalKey=$externalKey&accountWithBalance=false&accountWithBalanceAndCBA=false&audit=NONE") ~> addHeader("Authorization", "Basic YWRtaW46cGFzc3dvcmQ=") ~> addHeader("X-Killbill-CreatedBy", "admin") ~> addHeader("X-Killbill-ApiKey", "hootsuite") ~> addHeader("X-Killbill-ApiSecret", "hootsuite")
    }
    responseFuture onComplete {
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
