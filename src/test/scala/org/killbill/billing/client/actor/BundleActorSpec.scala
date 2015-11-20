package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model.BillingActionPolicy.BillingActionPolicy
import org.killbill.billing.client.model._
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * Created by jgomez on 20/11/2015.
  */
class BundleActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import BundleActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Bundles Success Response
  def getBundlesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getBundlesStream: InputStream = getClass.getResourceAsStream("/getBundlesResponse.json")
    val getBundlesJsonContent = Source.fromInputStream(getBundlesStream, "UTF-8").getLines.mkString
    val getBundlesBodyResponse = HttpEntity(MediaTypes.`application/json`, getBundlesJsonContent.getBytes())
    mockResponse.entity returns getBundlesBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetBundlesActor")

    "GetBundles should" >> {
      "return a List of Bundle objects" in {
        val fut: Future[List[Any]] = ask(bundleActor, GetBundles(any[Long], any[Long], any[String])).mapTo[List[Any]]
        val getBundlesResponse = Await.result(fut, timeout.duration)

        val expected = List[BundleResult[Bundle]](BundleResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))),
          BundleResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("externalKey2"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
              Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(List[EventSubscription]())))))
        getBundlesResponse mustEqual expected
      }
    }
  }

  // Test Get Bundles Failure Response
  def getBundlesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetBundlesFailureActor")

    "GetBundles should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(bundleActor, GetBundles(any[Long], any[Long], "anyAuditMode")).mapTo[Any]
        val getBundlesResponse = Await.result(fut, timeout.duration)
        getBundlesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Bundle by Id Success Response
  def getBundleByIdSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getBundleStream: InputStream = getClass.getResourceAsStream("/getBundleResponse.json")
    val getBundleJsonContent = Source.fromInputStream(getBundleStream, "UTF-8").getLines.mkString
    val getBundleBodyResponse = HttpEntity(MediaTypes.`application/json`, getBundleJsonContent.getBytes())
    mockResponse.entity returns getBundleBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetBundleByIdActor")

    "GetBundleById should" >> {
      "return Bundle object" in {
        val fut: Future[Any] = ask(bundleActor, GetBundleById(UUID.randomUUID())).mapTo[Any]
        val getBundleResponse = Await.result(fut, timeout.duration)
        val expected = Bundle(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]()))))
        getBundleResponse mustEqual expected
      }
    }
  }

  // Test Get Bundle by Id Failure Response
  def getBundleByIdFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetBundleByIdFailureActor")

    "GetBundleById should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(bundleActor, GetBundleById(UUID.randomUUID())).mapTo[Any]
        val getBundleResponse = Await.result(fut, timeout.duration)
        getBundleResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Bundle by ExternalKey Success Response
  def getBundleByExternalKeySuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getBundleStream: InputStream = getClass.getResourceAsStream("/getBundleResponse.json")
    val getBundleJsonContent = Source.fromInputStream(getBundleStream, "UTF-8").getLines.mkString
    val getBundleBodyResponse = HttpEntity(MediaTypes.`application/json`, getBundleJsonContent.getBytes())
    mockResponse.entity returns getBundleBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetBundleByExternalKeyActor")

    "GetBundleByExternalKey should" >> {
      "return Bundle object" in {
        val fut: Future[Any] = ask(bundleActor, GetBundleByExternalKey(any[String])).mapTo[Any]
        val getBundleResponse = Await.result(fut, timeout.duration)
        val expected = Bundle(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]()))))
        getBundleResponse mustEqual expected
      }
    }
  }

  // Test Get Bundle by ExternalKey Failure Response
  def getBundleByExternalKeyFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetBundleByExternalKeyFailureActor")

    "GetBundleByExternalKey should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(bundleActor, GetBundleByExternalKey(any[String])).mapTo[Any]
        val getBundleResponse = Await.result(fut, timeout.duration)
        getBundleResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search Bundles Success Response
  def searchBundlesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchBundlesStream: InputStream = getClass.getResourceAsStream("/getBundlesResponse.json")
    val searchBundlesJsonContent = Source.fromInputStream(searchBundlesStream, "UTF-8").getLines.mkString
    val searchBundlesBodyResponse = HttpEntity(MediaTypes.`application/json`, searchBundlesJsonContent.getBytes())
    mockResponse.entity returns searchBundlesBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchBundlesActor")

    "SearchBundles should" >> {
      "return a List of Bundle objects" in {
        val fut: Future[List[Any]] = ask(bundleActor, SearchBundles(any[String], any[Long], any[Long], any[String])).mapTo[List[Any]]
        val searchBundlesResponse = Await.result(fut, timeout.duration)

        val expected = List[BundleResult[Bundle]](BundleResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))),
          BundleResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("externalKey2"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
              Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(List[EventSubscription]())))))
        searchBundlesResponse mustEqual expected
      }
    }
  }

  // Test Search Bundles Failure Response
  def searchBundlesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchBundlesFailureActor")

    "SearchBundles should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(bundleActor, SearchBundles(any[String], any[Long], any[Long], any[String])).mapTo[Any]
        val searchBundlesResponse = Await.result(fut, timeout.duration)
        searchBundlesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Account Bundles Success Response
  def getAccountBundlesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountBundlesStream: InputStream = getClass.getResourceAsStream("/getBundlesResponse.json")
    val getAccountBundlesJsonContent = Source.fromInputStream(getAccountBundlesStream, "UTF-8").getLines.mkString
    val getAccountBundlesBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountBundlesJsonContent.getBytes())
    mockResponse.entity returns getAccountBundlesBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountBundlesActor")

    "GetAccountBundles should" >> {
      "return a List of Bundle objects" in {
        val fut: Future[List[Any]] = ask(bundleActor, GetAccountBundles(UUID.randomUUID(), "anyExternalKey")).mapTo[List[Any]]
        val getAccountBundlesResponse = Await.result(fut, timeout.duration)

        val expected = List[BundleResult[Bundle]](BundleResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))),
          BundleResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("externalKey2"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
              Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(List[EventSubscription]())))))
        getAccountBundlesResponse mustEqual expected
      }
    }
  }

  // Test Get Account Bundles Failure Response
  def getAccountBundlesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountBundlesFailureActor")

    "GetAccountBundles should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(bundleActor, GetAccountBundles(UUID.randomUUID(), "anyExternalKey")).mapTo[Any]
        val getAccountBundlesResponse = Await.result(fut, timeout.duration)
        getAccountBundlesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Transfer Bundle to Account Success Response
  def transferBundleToAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val transferBundleToAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns transferBundleToAccountBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "TransferBundleAccountActor")

    "TransferBundleAccount should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(bundleActor, TransferBundleToAccount(Bundle(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))), UUID.randomUUID(), BillingActionPolicy.END_OF_TERM)).mapTo[String]
        val transferBundleAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        transferBundleAccountResponse mustEqual expected
      }
    }
  }

  // Test Transfer Bundle to Account Other Response Response
  def transferBundleToAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val transferBundleToAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns transferBundleToAccountBodyResponse

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "TransferBundleAccountOtherResponseActor")

    "TransferBundleAccount should" >> {
      "return a 200 status" in {
        val fut: Future[String] = ask(bundleActor, TransferBundleToAccount(Bundle(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))), UUID.randomUUID(), BillingActionPolicy.END_OF_TERM)).mapTo[String]
        val transferBundleAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        transferBundleAccountResponse mustEqual expected
      }
    }
  }

  // Test Transfer Bundles to Account Failure Response
  def transferBundleToAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val bundleActor = system.actorOf(Props(new BundleActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "TransferBundleToAccountFailureActor")

    "TransferBundleToAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(bundleActor, TransferBundleToAccount(Bundle(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(BundleTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))), UUID.randomUUID(), BillingActionPolicy.END_OF_TERM)).mapTo[Any]
        val transferBundleToAccountResponse = Await.result(fut, timeout.duration)
        transferBundleToAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  getBundlesSuccessResponseTest()
  getBundlesFailureResponseTest()
  getBundleByIdSuccessResponseTest()
  getBundleByIdFailureResponseTest()
  getBundleByExternalKeySuccessResponseTest()
  getBundleByExternalKeyFailureResponseTest()
  searchBundlesSuccessResponseTest()
  searchBundlesFailureResponseTest()
  getAccountBundlesSuccessResponseTest()
  getAccountBundlesFailureResponseTest()
  transferBundleToAccountSuccessResponseTest()
  transferBundleToAccountOtherResponseTest()
  transferBundleToAccountFailureResponseTest()
}