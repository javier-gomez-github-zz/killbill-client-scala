package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model.OverdueState
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * Created by jgomez on 19/11/2015.
  */
class OverdueActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import OverdueActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Upload XML Overdue Config Success Response
  def uploadXMLOverdueConfigSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val uploadXMLOverdueConfigBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns uploadXMLOverdueConfigBodyResponse

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "OverdueUploadActor")

    "UploadXMLOverdueConfig should" >> {
      "return a 200 status" in {
        val fut: Future[String] = ask(overdueActor, UploadXMLOverdueConfig("anyPath")).mapTo[String]
        val uploadXMLOverdueConfig = Await.result(fut, timeout.duration)
        val expected = "200"
        uploadXMLOverdueConfig mustEqual expected
      }
    }
  }

  // Test Upload XML Overdue Config Other Response
  def uploadXMLOverdueConfigOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "500"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val uploadXMLOverdueConfigBodyResponse = HttpEntity(MediaTypes.`application/json`, "500")
    mockResponse.entity returns uploadXMLOverdueConfigBodyResponse

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "OverdueUploadOtherActor")

    "UploadXMLOverdueConfig should" >> {
      "return a 500 status" in {
        val fut: Future[String] = ask(overdueActor, UploadXMLOverdueConfig("anyPath")).mapTo[String]
        val uploadXMLOverdueConfig = Await.result(fut, timeout.duration)
        val expected = "500"
        uploadXMLOverdueConfig mustEqual expected
      }
    }
  }

  // Test Upload XML Overdue Config Failure Response
  def uploadXMLOverdueConfigFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "OverdueUploadFailureActor")

    "UploadXMLOverdueConfig should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(overdueActor, UploadXMLOverdueConfig("anyPath")).mapTo[String]
        val uploadXMLOverdueConfig = Await.result(fut, timeout.duration)
        uploadXMLOverdueConfig mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get XML Overdue Config Success Response
  def getXMLOverdueConfigSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val getXMLOverdueConfigBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns getXMLOverdueConfigBodyResponse

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetSuccessOverdueActor")

    "GetXMLOverdueConfig should" >> {
      "return a 200 status" in {
        val fut: Future[String] = ask(overdueActor, GetXMLOverdueConfig()).mapTo[String]
        val getXMLOverdueConfig = Await.result(fut, timeout.duration)
        val expected = "200"
        getXMLOverdueConfig mustEqual expected
      }
    }
  }

  // Test Get XML Overdue Config Invoice Translations Not Found Response
  def getXMLOverdueConfigInvoiceTranslationsNotFoundTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "404"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val getXMLOverdueConfigBodyResponse = HttpEntity(MediaTypes.`application/json`, "404")
    mockResponse.entity returns getXMLOverdueConfigBodyResponse

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceTranslationsNotFoundOverdueActor")

    "GetXMLOverdueConfig should" >> {
      "return a 404 status" in {
        val fut: Future[String] = ask(overdueActor, GetXMLOverdueConfig()).mapTo[String]
        val getXMLOverdueConfig = Await.result(fut, timeout.duration)
        val expected = "Invoice Translations not found"
        getXMLOverdueConfig mustEqual expected
      }
    }
  }

  // Test Get XML Overdue Config Other Response
  def getXMLOverdueConfigOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "500"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val getXMLOverdueConfigBodyResponse = HttpEntity(MediaTypes.`application/json`, "500")
    mockResponse.entity returns getXMLOverdueConfigBodyResponse

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetOtherResponseOverdueActor")

    "GetXMLOverdueConfig should" >> {
      "return a 500 status" in {
        val fut: Future[String] = ask(overdueActor, GetXMLOverdueConfig()).mapTo[String]
        val getXMLOverdueConfig = Await.result(fut, timeout.duration)
        val expected = "500"
        getXMLOverdueConfig mustEqual expected
      }
    }
  }

  // Test Get XML Overdue Config Failure Response
  def getXMLOverdueConfigFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "OverdueGetFailureActor")

    "GetXMLOverdueConfig should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(overdueActor, GetXMLOverdueConfig()).mapTo[String]
        val getXMLOverdueConfig = Await.result(fut, timeout.duration)
        getXMLOverdueConfig mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Overdue State for Account Success Response
  def getOverdueStateForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getOverdueStateStream: InputStream = getClass.getResourceAsStream("/getOverdueStateResponse.json")
    val getOverdueStateJsonContent = Source.fromInputStream(getOverdueStateStream, "UTF-8").getLines.mkString
    val getOverdueStateBodyResponse = HttpEntity(MediaTypes.`application/json`, getOverdueStateJsonContent.getBytes())
    mockResponse.entity returns getOverdueStateBodyResponse

    val overdueStateActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetOverdueStateActor")

    "Get OverdueState for Account should" >> {
      "return OverdueState object" in {
        val fut: Future[Any] = ask(overdueStateActor, GetOverdueStateForAccount(UUID.randomUUID())).mapTo[Any]
        val getOverdueStateResponse = Await.result(fut, timeout.duration)

        val expected = new OverdueState(Option("fakeName"), Option("externalMessage"), Option(List[Int](1, 2)),
          Option(false), Option(false), Option(true), Option(5))
        getOverdueStateResponse mustEqual expected
      }
    }
  }

  // Test Get Overdue State for Account Failure Response
  def getOverdueStateForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val overdueActor = system.actorOf(Props(new OverdueActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetOverdueStateFailureActor")

    "Get Overdue State for Account should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(overdueActor, GetOverdueStateForAccount(UUID.randomUUID())).mapTo[Any]
        val getOverdueStateResponse = Await.result(fut, timeout.duration)
        getOverdueStateResponse mustEqual expectedErrorMessage
      }
    }
  }

  uploadXMLOverdueConfigSuccessResponseTest()
  uploadXMLOverdueConfigOtherResponseTest()
  uploadXMLOverdueConfigFailureResponseTest()
  getXMLOverdueConfigSuccessResponseTest()
  getXMLOverdueConfigInvoiceTranslationsNotFoundTest()
  getXMLOverdueConfigOtherResponseTest()
  getXMLOverdueConfigFailureResponseTest()
  getOverdueStateForAccountSuccessResponseTest()
  getOverdueStateForAccountFailureResponseTest()
}