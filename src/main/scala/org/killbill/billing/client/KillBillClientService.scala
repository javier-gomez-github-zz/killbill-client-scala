package org.killbill.billing.client

import akka.actor.{Actor, Props}
import org.killbill.billing.client.model.{Account, AccountJsonProtocol}
import spray.http.{BasicHttpCredentials, HttpHeaders}
import spray.routing._

class KillBillClientServiceActor extends Actor with KillBillClientService {
  
  def actorRefFactory = context

  def receive = {
    case "test" =>
      println("something")
//    runRoute(killBillClientRoute)
  }

}

trait KillBillClientService extends HttpService {
  import AccountJsonProtocol._
  import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller

  val killbillUrl = "http://localhost:8080/1.0/kb"

  val headers = List(
    HttpHeaders.Authorization.apply(BasicHttpCredentials.apply("admin", "password")),
    HttpHeaders.RawHeader.apply("X-Killbill-CreatedBy", "admin"),
    HttpHeaders.RawHeader.apply("X-Killbill-ApiKey", "hootsuite"),
    HttpHeaders.RawHeader.apply("X-Killbill-ApiSecret", "hootsuite")
  )

//  val killBillClientRoute =
//    pathPrefix("api") {
//      get {
//        path("getAccount" / Segment / Segment / Segment / Segment) { (externalKey, withBalance, withCBA, audit) =>
//          requestContext =>
//            val accountService = actorRefFactory.actorOf(Props(new AccountActor(requestContext, killbillUrl, headers)))
//            accountService ! AccountActor.GetAccountByExternalKey(externalKey, withBalance.toBoolean, withCBA.toBoolean, audit)
//        }
//      } ~
//      post {
//        path("createAccount") {
//          entity(as[Account]) { account =>
//            requestContext =>
//              val accountService = actorRefFactory.actorOf(Props(new AccountActor(requestContext, killbillUrl, headers)))
//              accountService ! AccountActor.CreateAccount(account)
//          }
//        }
//      }
//    }
}
