package org.killbill.billing.client

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import akka.util.Timeout
import akka.pattern.ask
import org.killbill.billing.client.AccountActor.GetAccountByExternalKey
import org.killbill.billing.client.model.{KillbillApiResult, Account}
import org.killbill.billing.client.model.AccountJsonProtocol._
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

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

//  val timeout = 5.seconds

  // create the system and actor
  val system = ActorSystem("killbill-api-scala-service")
  val log = Logging(system, getClass)
  import system.dispatcher

  // the dispatcher member of the variable system will be available in the scope

  // Public methods to connect to the KillBill API
  def getAccountByExternalKey(externalKey: String, withBalance: Boolean = false, withCBA: Boolean = false, audit: String = "NONE"): Account = {

    val accountActor = system.actorOf(Props(new AccountActor(killBillUrl, headers)), name = "AccountActor")
    implicit val timeout = Timeout(10 seconds)
    val future: Future[Account] = ask(accountActor, GetAccountByExternalKey(externalKey, withBalance, withCBA, audit)).mapTo[Account]
    val result = Await.result(future, timeout.duration)
    //TODO: why is adding a Some(value) in the JSON? Check JSON unmarshaller!
    result

//    val suffixUrl = "&accountWithBalance=" + withBalance.toString + "&accountWithBalanceAndCBA=" + withCBA.toString + "&audit=" + audit
//
//    val pipeline: HttpRequest => Future[Account] = sendReceive ~> unmarshal[Account]
//    val f: Future[Account] = pipeline {
//      Get(killBillUrl+s"/accounts?externalKey=$externalKey"+suffixUrl) ~> addHeaders(headers)
//    }
//    f.onComplete {
//      case Success(response) =>
//        println("Got Account information for externalKey=" + response.externalKey)
//      case Failure(error) =>
//        println(error.getMessage())
//    }
//    Await.result(f, timeout)
  }

  // Test method to validate that the functionality is working as expected (must be deleted)
  val account = getAccountByExternalKey("jgomez")
  println(s"Got the Account information: $account")

  // Shutdown the app
  system.shutdown()
}
