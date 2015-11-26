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
  * Created by jgomez on 26/11/2015.
  */
class SubscriptionActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import SubscriptionActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Create Subscription Success Response
  def createSubscriptionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateSubscriptionActor")

    "CreateSubscription should" >> {
      "return String response" in {
        val subscription: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        val fut: Future[String] = ask(subscriptionActor, CreateSubscription(subscription)).mapTo[String]
        val createSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Create Subscription Other Response
  def createSubscriptionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateOtherSubscriptionActor")

    "CreateSubscription should" >> {
      "return a different status" in {
        val subscription: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        val fut: Future[String] = ask(subscriptionActor, CreateSubscription(subscription)).mapTo[String]
        val createSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Create Subscription Failure Response
  def createSubscriptionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateFailureSubscriptionActor")

    "CreateSubscription should" >> {
      "throw an Exception" in {
        val subscription: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        val fut: Future[String] = ask(subscriptionActor, CreateSubscription(subscription)).mapTo[String]
        val createSubscriptionResponse = Await.result(fut, timeout.duration)
        createSubscriptionResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Subscription by Id Success Response
  def getSubscriptionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getSubscriptionStream: InputStream = getClass.getResourceAsStream("/getSubscriptionResponse.json")
    val getSubscriptionJsonContent = Source.fromInputStream(getSubscriptionStream, "UTF-8").getLines.mkString
    val getSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, getSubscriptionJsonContent.getBytes())
    mockResponse.entity returns getSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetSubscriptionActor")

    "GetSubscription should" >> {
      "return Subscription object" in {
        val fut: Future[Any] = ask(subscriptionActor, GetSubscriptionById(UUID.randomUUID())).mapTo[Any]
        val getSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        getSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Get Subscription by Id Failure Response
  def getSubscriptionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetSubscriptionWithFailureActor")

    "GetSubscription should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(subscriptionActor, GetSubscriptionById(UUID.randomUUID())).mapTo[Any]
        val getSubscriptionResponse = Await.result(fut, timeout.duration)
        getSubscriptionResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Update Subscription by Id Success Response
  def updateSubscriptionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val updateSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns updateSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateSubscriptionActor")

    "UpdateSubscription should" >> {
      "return String response" in {
        val subscription: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        val fut: Future[String] = ask(subscriptionActor, UpdateSubscription(subscription, UUID.randomUUID())).mapTo[String]
        val updateSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        updateSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Update Subscription by Id Other Response
  def updateSubscriptionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val updateSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns updateSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateSubscriptionOtherActor")

    "UpdateSubscription should" >> {
      "return String response" in {
        val subscription: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        val fut: Future[String] = ask(subscriptionActor, UpdateSubscription(subscription, UUID.randomUUID())).mapTo[String]
        val updateSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        updateSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Update Subscription by Id Failure Response
  def updateSubscriptionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UpdateSubscriptionWithFailureActor")

    "UpdateSubscription should" >> {
      "throw an Exception" in {
        val subscription: Subscription = Subscription(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("2015-11-22"), Option("productName"), Option(ProductCategory.BASE),
          Option(BillingPeriod.ANNUAL), Option(PhaseType.EVERGREEN), Option("priceList"),
          Option(EntitlementState.ACTIVE), Option(EntitlementSourceType.NATIVE), Option("2015-11-25"),
          Option("2015-11-25"), Option("2015-11-22"), Option("2015-11-25"), Option(List[EventSubscription]()),
          Option(List[PhasePriceOverride]()))
        val fut: Future[Any] = ask(subscriptionActor, UpdateSubscription(subscription, UUID.randomUUID())).mapTo[Any]
        val updateSubscriptionResponse = Await.result(fut, timeout.duration)
        updateSubscriptionResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Cancel Subscription by Id Success Response
  def cancelSubscriptionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val cancelSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns cancelSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CancelSubscriptionActor")

    "CancelSubscription should" >> {
      "return String response" in {
        val fut: Future[String] = ask(subscriptionActor, CancelSubscription(UUID.randomUUID())).mapTo[String]
        val cancelSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        cancelSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Cancel Subscription by Id Other Response
  def cancelSubscriptionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val cancelSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns cancelSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CancelSubscriptionOtherActor")

    "CancelSubscription should" >> {
      "return String response" in {
        val fut: Future[String] = ask(subscriptionActor, CancelSubscription(UUID.randomUUID())).mapTo[String]
        val cancelSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        cancelSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Cancel Subscription by Id Failure Response
  def cancelSubscriptionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CancelSubscriptionWithFailureActor")

    "CancelSubscription should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(subscriptionActor, CancelSubscription(UUID.randomUUID())).mapTo[Any]
        val cancelSubscriptionResponse = Await.result(fut, timeout.duration)
        cancelSubscriptionResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Uncancel Subscription by Id Success Response
  def unCancelSubscriptionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val unCancelSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns unCancelSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UnCancelSubscriptionActor")

    "UnCancelSubscription should" >> {
      "return String response" in {
        val fut: Future[String] = ask(subscriptionActor, UnCancelSubscription(UUID.randomUUID())).mapTo[String]
        val unCancelSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        unCancelSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Uncancel Subscription by Id Other Response
  def unCancelSubscriptionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val unCancelSubscriptionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns unCancelSubscriptionBodyResponse

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UnCancelSubscriptionOtherActor")

    "UnCancelSubscription should" >> {
      "return String response" in {
        val fut: Future[String] = ask(subscriptionActor, UnCancelSubscription(UUID.randomUUID())).mapTo[String]
        val unCancelSubscriptionResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        unCancelSubscriptionResponse mustEqual expected
      }
    }
  }

  // Test Uncancel Subscription by Id Failure Response
  def unCancelSubscriptionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val subscriptionActor = system.actorOf(Props(new SubscriptionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UnCancelSubscriptionWithFailureActor")

    "UnCancelSubscription should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(subscriptionActor, UnCancelSubscription(UUID.randomUUID())).mapTo[Any]
        val unCancelSubscriptionResponse = Await.result(fut, timeout.duration)
        unCancelSubscriptionResponse mustEqual expectedErrorMessage
      }
    }
  }

  createSubscriptionSuccessResponseTest()
  createSubscriptionOtherResponseTest()
  createSubscriptionFailureResponseTest()
  getSubscriptionSuccessResponseTest()
  getSubscriptionFailureResponseTest()
  updateSubscriptionSuccessResponseTest()
  updateSubscriptionOtherResponseTest()
  updateSubscriptionFailureResponseTest()
  cancelSubscriptionSuccessResponseTest()
  cancelSubscriptionOtherResponseTest()
  cancelSubscriptionFailureResponseTest()
  unCancelSubscriptionSuccessResponseTest()
  unCancelSubscriptionOtherResponseTest()
  unCancelSubscriptionFailureResponseTest()
}