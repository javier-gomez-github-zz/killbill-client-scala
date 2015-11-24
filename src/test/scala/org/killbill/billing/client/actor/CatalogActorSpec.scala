package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model._
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * Created by jgomez on 24/11/2015.
  */
class CatalogActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import CatalogActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Simple Catalog Success Response
  def getSimpleCatalogSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getSimpleCatalogStream: InputStream = getClass.getResourceAsStream("/getSimpleCatalogResponse.json")
    val getSimpleCatalogJsonContent = Source.fromInputStream(getSimpleCatalogStream, "UTF-8").getLines.mkString
    val getSimpleCatalogBodyResponse = HttpEntity(MediaTypes.`application/json`, getSimpleCatalogJsonContent.getBytes())
    mockResponse.entity returns getSimpleCatalogBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetSimpleCatalogActor")

    "GetSimpleCatalog should" >> {
      "return Catalog object" in {
        val fut: Future[Any] = ask(catalogActor, GetSimpleCatalog()).mapTo[Any]
        val getSimpleCatalogResponse = Await.result(fut, timeout.duration)
        val expected = Catalog(Option("name"), Option(List[Product]()))
        getSimpleCatalogResponse mustEqual expected
      }
    }
  }

  // Test Get Simple Catalog Failure Response
  def getSimpleCatalogFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetSimpleCatalogFailureActor")

    "GetSimpleCatalog should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(catalogActor, GetSimpleCatalog()).mapTo[Any]
        val getSimpleCatalogResponse = Await.result(fut, timeout.duration)
        getSimpleCatalogResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Available Addons Success Response
  def getAvailableAddonsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAvailableAddonsStream: InputStream = getClass.getResourceAsStream("/getAvailableAddonsOrBasePlansResponse.json")
    val getAvailableAddonsJsonContent = Source.fromInputStream(getAvailableAddonsStream, "UTF-8").getLines.mkString
    val getAvailableAddonsBodyResponse = HttpEntity(MediaTypes.`application/json`, getAvailableAddonsJsonContent.getBytes())
    mockResponse.entity returns getAvailableAddonsBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAvailableAddonsActor")

    "GetAvailableAddons should" >> {
      "return a List of PlanDetail objects" in {
        val fut: Future[Any] = ask(catalogActor, GetAvailableAddons("anyBaseProductName")).mapTo[Any]
        val getAvailableAddonsResponse = Await.result(fut, timeout.duration)
        val expected = List[PlanDetailResult[PlanDetail]](
          PlanDetailResult(Option("product"), Option("plan"), Option(BillingPeriod.MONTHLY), Option("priceList"),
          Option(List[Price]())),
          PlanDetailResult(Option("product2"), Option("plan2"), Option(BillingPeriod.QUARTERLY), Option("priceList2"),
            Option(List[Price]()))
        )
        getAvailableAddonsResponse mustEqual expected
      }
    }
  }

  // Test Get Available Addons Failure Response
  def getAvailableAddonsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAvailableAddonsFailureActor")

    "GetAvailableAddons should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(catalogActor, GetAvailableAddons("anyBaseProductName")).mapTo[Any]
        val getAvailableAddonsResponse = Await.result(fut, timeout.duration)
        getAvailableAddonsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Available Base Plans Success Response
  def getAvailableBasePlansSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAvailableBasePlansStream: InputStream = getClass.getResourceAsStream("/getAvailableAddonsOrBasePlansResponse.json")
    val getAvailableBasePlansJsonContent = Source.fromInputStream(getAvailableBasePlansStream, "UTF-8").getLines.mkString
    val getAvailableBasePlansBodyResponse = HttpEntity(MediaTypes.`application/json`, getAvailableBasePlansJsonContent.getBytes())
    mockResponse.entity returns getAvailableBasePlansBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAvailableBasePlansActor")

    "GetAvailableBasePlans should" >> {
      "return a List of PlanDetail objects" in {
        val fut: Future[Any] = ask(catalogActor, GetAvailableBasePlans()).mapTo[Any]
        val getAvailableBasePlansResponse = Await.result(fut, timeout.duration)
        val expected = List[PlanDetailResult[PlanDetail]](
          PlanDetailResult(Option("product"), Option("plan"), Option(BillingPeriod.MONTHLY), Option("priceList"),
            Option(List[Price]())),
          PlanDetailResult(Option("product2"), Option("plan2"), Option(BillingPeriod.QUARTERLY), Option("priceList2"),
            Option(List[Price]()))
        )
        getAvailableBasePlansResponse mustEqual expected
      }
    }
  }

  // Test Get Available Base Plans Failure Response
  def getAvailableBasePlansFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAvailableBasePlansFailureActor")

    "GetAvailableBasePlans should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(catalogActor, GetAvailableBasePlans()).mapTo[Any]
        val getAvailableBasePlansResponse = Await.result(fut, timeout.duration)
        getAvailableBasePlansResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Upload XML Catalog Success Response
  def uploadXMLCatalogSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadXMLCatalogBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns uploadXMLCatalogBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadXMLCatalogActor")

    "UploadXMLCatalog should" >> {
      "return String response" in {
        val fut: Future[String] = ask(catalogActor, UploadXMLCatalog("anyCatalog")).mapTo[String]
        val uploadXMLCatalogResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        uploadXMLCatalogResponse mustEqual expected
      }
    }
  }

  // Test Upload XML Catalog Other Response
  def uploadXMLCatalogOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val uploadXMLCatalogBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns uploadXMLCatalogBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadXMLCatalogOtherResponseActor")

    "UploadXMLCatalog should" >> {
      "return a different status" in {
        val fut: Future[String] = ask(catalogActor, UploadXMLCatalog("anyCatalog")).mapTo[String]
        val uploadXMLCatalogResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        uploadXMLCatalogResponse mustEqual expected
      }
    }
  }

  // Test Upload XML Catalog Failure Response
  def uploadXMLCatalogFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UploadXMLCatalogFailureActor")

    "UploadXMLCatalog should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(catalogActor, UploadXMLCatalog("anyCatalog")).mapTo[String]
        val uploadXMLCatalogResponse = Await.result(fut, timeout.duration)
        uploadXMLCatalogResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get XML Catalog Success Response
  def getXMLCatalogSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getXMLCatalogStream: InputStream = getClass.getResourceAsStream("/getXMLCatalogResponse.xml")
    val getXMLCatalogJsonContent = Source.fromInputStream(getXMLCatalogStream, "UTF-8").getLines.mkString
    val getXMLCatalogBodyResponse = HttpEntity(MediaTypes.`application/xml`, getXMLCatalogJsonContent.getBytes())
    mockResponse.entity returns getXMLCatalogBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetXMLCatalogActor")

    "GetXMLCatalog should" >> {
      "return an XML Catalog object" in {
        val fut: Future[Any] = ask(catalogActor, GetXMLCatalog()).mapTo[Any]
        val getXMLCatalogResponse = Await.result(fut, timeout.duration)
        getXMLCatalogResponse mustEqual getXMLCatalogBodyResponse.asString
      }
    }
  }

  // Test Get XML Catalog Failure Response
  def getXMLCatalogFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetXMLCatalogFailureActor")

    "GetXMLCatalog should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(catalogActor, GetXMLCatalog()).mapTo[Any]
        val getXMLCatalogResponse = Await.result(fut, timeout.duration)
        getXMLCatalogResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get JSON Catalog Success Response
  def getJSONCatalogSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getJSONCatalogStream: InputStream = getClass.getResourceAsStream("/getJSONCatalogResponse.json")
    val getJSONCatalogJsonContent = Source.fromInputStream(getJSONCatalogStream, "UTF-8").getLines.mkString
    val getJSONCatalogBodyResponse = HttpEntity(MediaTypes.`application/json`, getJSONCatalogJsonContent.getBytes())
    mockResponse.entity returns getJSONCatalogBodyResponse

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetJSONCatalogActor")

    "GetJSONCatalog should" >> {
      "return an XML Catalog object" in {
        val fut: Future[Any] = ask(catalogActor, GetJSONCatalog()).mapTo[Any]
        val getJSONCatalogResponse = Await.result(fut, timeout.duration)
        getJSONCatalogResponse mustEqual getJSONCatalogBodyResponse.asString
      }
    }
  }

  // Test Get JSON Catalog Failure Response
  def getJSONCatalogFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val catalogActor = system.actorOf(Props(new CatalogActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetJSONCatalogFailureActor")

    "GetJSONCatalog should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(catalogActor, GetJSONCatalog()).mapTo[Any]
        val getJSONCatalogResponse = Await.result(fut, timeout.duration)
        getJSONCatalogResponse mustEqual expectedErrorMessage
      }
    }
  }

  getSimpleCatalogSuccessResponseTest()
  getSimpleCatalogFailureResponseTest()
  getAvailableAddonsSuccessResponseTest()
  getAvailableAddonsFailureResponseTest()
  getAvailableBasePlansSuccessResponseTest()
  getAvailableBasePlansFailureResponseTest()
  uploadXMLCatalogSuccessResponseTest()
  uploadXMLCatalogOtherResponseTest()
  uploadXMLCatalogFailureResponseTest()
  getXMLCatalogSuccessResponseTest()
  getXMLCatalogFailureResponseTest()
  getJSONCatalogSuccessResponseTest()
  getJSONCatalogFailureResponseTest()
}