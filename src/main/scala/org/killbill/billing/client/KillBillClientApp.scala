package org.killbill.billing.client.actor

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import org.killbill.billing.client.actor.AccountActor.{CreateAccount, GetAccountByExternalKey}
import org.killbill.billing.client.model.Account
import spray.http.{BasicHttpCredentials, HttpHeaders, _}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by jgomez on 20/10/2015.
 */
object KillBillClientApp extends App {
  // KillBill API URL (if not specified in the constructor)
  var killBillUrl = "http://localhost:8080/1.0/kb"

  // KillBill API Headers (if not specified in the constructor)
  var headers = List(
    HttpHeaders.Authorization.apply(BasicHttpCredentials.apply("admin", "password")),
    HttpHeaders.RawHeader.apply("X-Killbill-CreatedBy", "admin"),
    HttpHeaders.RawHeader.apply("X-Killbill-ApiKey", "hootsuite"),
    HttpHeaders.RawHeader.apply("X-Killbill-ApiSecret", "hootsuite")
  )

  // constructor to initialize the KillBill URL and Headers
  def apply(apiUrl: String, newHeaders: List[HttpHeader with Serializable with Product]) {
    killBillUrl = apiUrl
    headers = newHeaders
  }

  // create the system and actor
  val system = ActorSystem("killbill-api-scala-service")
  val log = Logging(system, getClass)

  // Public methods to connect to the KillBill API
  def getAccountByExternalKey(externalKey: String, withBalance: Boolean = false, withCBA: Boolean = false, audit: String = "NONE"): Any = {
    val accountActor = system.actorOf(Props(new AccountActor(killBillUrl, headers)), name = "AccountActor")
    implicit val timeout = Timeout(10 seconds)
    val future: Future[Any] = ask(accountActor, GetAccountByExternalKey(externalKey, withBalance, withCBA, audit)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  // Test method to validate the getAccountByExternalKey functionality
//  val account = getAccountByExternalKey("jgomez1")
//  println(s"Got the Account information: $account")

  def createAccount(account: Account): String = {
    val accountActor = system.actorOf(Props(new AccountActor(killBillUrl, headers)), name = "AccountActor")
    implicit val timeout = Timeout(20 seconds)
    val future: Future[String] = ask(accountActor, CreateAccount(account)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  // Test method to validate the createAccount functionality
  //  val account = Account.apply(None, Option.apply("kbanman"), None, None, Option.apply("Kelly Banman"), None, Option.apply("kbanman@velocitypartners.net"), None, Option.apply("USD"), None, Option.apply("UTC"), None, None, None, None, None, None, None, None, None, None, None)
  //  val response: String = createAccount(account.asInstanceOf[Account])
  //  if (response.contains("201")) {
  //    println(s"Account created succesfully")
  //  }
  //  else {
  //    println(s"An error occurred. Message: " + response)
  //  }
}
