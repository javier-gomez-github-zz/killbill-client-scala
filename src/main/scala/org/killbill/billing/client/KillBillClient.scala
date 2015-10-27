package org.killbill.billing.client.actor

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import org.killbill.billing.client.actor.AccountActor.{CreateAccount, GetAccountByExternalKey}
import org.killbill.billing.client.model.Account
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by jgomez on 20/10/2015.
 */
class KillBillClient(killBillUrl: String, headers: List[HttpHeader with Serializable with Product]) {

  // create the system, log and other shared features
  val system = ActorSystem("killbill-api-scala-client")
  val log = Logging(system, getClass)
  implicit val timeout = Timeout(10 seconds)

  // create the actors
  val accountActor = system.actorOf(Props(new AccountActor(killBillUrl, headers)), name = "AccountActor")

  // Public methods to connect to the KillBill API
  def getAccountByExternalKey(externalKey: String, withBalance: Boolean = false, withCBA: Boolean = false, audit: String = "NONE"): Any = {
    val future: Future[Any] = ask(accountActor, GetAccountByExternalKey(externalKey, withBalance, withCBA, audit)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def createAccount(account: Account): String = {
    val future: Future[String] = ask(accountActor, CreateAccount(account)).mapTo[String]
    Await.result(future, timeout.duration)
  }
}
