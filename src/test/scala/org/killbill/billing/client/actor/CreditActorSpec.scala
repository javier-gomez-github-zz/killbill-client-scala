package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model.Credit
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.io.Source

/**
  * Created by jgomez on 16/11/2015.
  */
class CreditActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {
  import CreditActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
  val mockResponse = mock[HttpResponse]
  val mockStatus = mock[StatusCode]
  mockResponse.status returns mockStatus
  mockStatus.isSuccess returns true

  // Test Get Credit by Id method
  def getCreditTest() = {

    val getCreditStream: InputStream = getClass.getResourceAsStream("/getCreditResponse.json")
    val getCreditJsonContent = Source.fromInputStream(getCreditStream, "UTF-8").getLines.mkString
    val getCreditBodyResponse = HttpEntity(MediaTypes.`application/json`, getCreditJsonContent.getBytes())
    mockResponse.entity returns getCreditBodyResponse

    val creditActor = system.actorOf(Props(new CreditActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Promise.successful(mockResponse).future
      }
    }), name = "CreditActor")

    "GetCredit should" >> {
      "return Credit object" in {
        val fut: Future[Any] = ask(creditActor, GetCredit(UUID.randomUUID())).mapTo[Any]
        val creditResponse = Await.result(fut, timeout.duration)
        val expected = Credit(Option(60), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option.apply("2015-11-15"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"))
        creditResponse mustEqual expected
      }
    }
  }

  getCreditTest()
}