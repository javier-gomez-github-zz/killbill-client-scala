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
  * Created by jgomez on 25/11/2015.
  */
class PaymentGatewayActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import PaymentGatewayActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Build Form Descriptor Success Response
  def buildFormDescriptorSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val buildFormDescriptorStream: InputStream = getClass.getResourceAsStream("/getHostedPaymentPageFormDescriptorResponse.json")
    val buildFormDescriptorJsonContent = Source.fromInputStream(buildFormDescriptorStream, "UTF-8").getLines.mkString
    val buildFormDescriptorBodyResponse = HttpEntity(MediaTypes.`application/json`, buildFormDescriptorJsonContent.getBytes())
    mockResponse.entity returns buildFormDescriptorBodyResponse

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "BuildFormDescriptorActor")

    "BuildFormDescriptor should" >> {
      "return HostedPaymentPageFormDescriptor object" in {
        val fields: HostedPaymentPageFields = HostedPaymentPageFields(Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(paymentGatewayActor, BuildFormDescriptor(fields, UUID.randomUUID(), UUID.randomUUID(), any[Map[String, String]])).mapTo[Any]
        val buildFormDescriptorResponse = Await.result(fut, timeout.duration)
        val expected = HostedPaymentPageFormDescriptorResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("formMethod"), Option("formUrl"), Option(Map[String, String]()), Option(Map[String, String]()))
        buildFormDescriptorResponse mustEqual expected
      }
    }
  }

  // Test Build Form Descriptor Other Response
  def buildFormDescriptorOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val buildFormDescriptorBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns buildFormDescriptorBodyResponse

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "BuildFormDescriptorOtherActor")

    "BuildFormDescriptor should" >> {
      "return a different status" in {
        val fields: HostedPaymentPageFields = HostedPaymentPageFields(Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentGatewayActor, BuildFormDescriptor(fields, UUID.randomUUID(), UUID.randomUUID(), any[Map[String, String]])).mapTo[String]
        val buildFormDescriptorResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        buildFormDescriptorResponse mustEqual expected
      }
    }
  }

  // Test Build Form Descriptor Failure Response
  def buildFormDescriptorFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "BuildFormDescriptorFailureActor")

    "BuildFormDescriptor should" >> {
      "throw an Exception" in {
        val fields: HostedPaymentPageFields = HostedPaymentPageFields(Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentGatewayActor, BuildFormDescriptor(fields, UUID.randomUUID(), UUID.randomUUID(), any[Map[String, String]])).mapTo[String]
        val buildFormDescriptorResponse = Await.result(fut, timeout.duration)
        buildFormDescriptorResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Build Combo Form Descriptor Success Response
  def buildComboFormDescriptorSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val buildComboFormDescriptorStream: InputStream = getClass.getResourceAsStream("/getHostedPaymentPageFormDescriptorResponse.json")
    val buildComboFormDescriptorJsonContent = Source.fromInputStream(buildComboFormDescriptorStream, "UTF-8").getLines.mkString
    val buildComboFormDescriptorBodyResponse = HttpEntity(MediaTypes.`application/json`, buildComboFormDescriptorJsonContent.getBytes())
    mockResponse.entity returns buildComboFormDescriptorBodyResponse

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "BuildComboFormDescriptorActor")

    "BuildComboFormDescriptor should" >> {
      "return HostedPaymentPageFormDescriptor object" in {
        val hostedPaymentPageFields: HostedPaymentPageFields = HostedPaymentPageFields(Option(List[PluginProperty]()))
        val account: Account = Account(None, None, None, None, None, None, None, None, None, None, None, None, None, None,
          None, None, None, None, None, None, None, None)
        val paymentMethod: PaymentMethod = PaymentMethod(None, None, None, None, None, None)
        val comboHostedPaymentPage: ComboHostedPaymentPage = ComboHostedPaymentPage(Option(hostedPaymentPageFields),
          Option(account), Option(paymentMethod), Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(paymentGatewayActor, BuildComboFormDescriptor(comboHostedPaymentPage, any[List[String]], any[Map[String, String]])).mapTo[Any]
        val buildFormDescriptorResponse = Await.result(fut, timeout.duration)
        val expected = HostedPaymentPageFormDescriptorResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
        Option("formMethod"), Option("formUrl"), Option(Map[String, String]()), Option(Map[String, String]()))
        buildFormDescriptorResponse mustEqual expected
      }
    }
  }

  // Test Build Combo Form Descriptor Other Response
  def buildComboFormDescriptorOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val buildComboFormDescriptorBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns buildComboFormDescriptorBodyResponse

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "BuildComboFormDescriptorOtherActor")

    "BuildComboFormDescriptor should" >> {
      "return a different status" in {
        val hostedPaymentPageFields: HostedPaymentPageFields = HostedPaymentPageFields(Option(List[PluginProperty]()))
        val account: Account = Account(None, None, None, None, None, None, None, None, None, None, None, None, None, None,
          None, None, None, None, None, None, None, None)
        val paymentMethod: PaymentMethod = PaymentMethod(None, None, None, None, None, None)
        val comboHostedPaymentPage: ComboHostedPaymentPage = ComboHostedPaymentPage(Option(hostedPaymentPageFields),
          Option(account), Option(paymentMethod), Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(paymentGatewayActor, BuildComboFormDescriptor(comboHostedPaymentPage, any[List[String]], any[Map[String, String]])).mapTo[String]
        val buildComboFormDescriptorResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        buildComboFormDescriptorResponse mustEqual expected
      }
    }
  }

  // Test Build Combo Form Descriptor Failure Response
  def buildComboFormDescriptorFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "BuildComboFormDescriptorFailureActor")

    "BuildComboFormDescriptor should" >> {
      "throw an Exception" in {
        val hostedPaymentPageFields: HostedPaymentPageFields = HostedPaymentPageFields(Option(List[PluginProperty]()))
        val account: Account = Account(None, None, None, None, None, None, None, None, None, None, None, None, None, None,
          None, None, None, None, None, None, None, None)
        val paymentMethod: PaymentMethod = PaymentMethod(None, None, None, None, None, None)
        val comboHostedPaymentPage: ComboHostedPaymentPage = ComboHostedPaymentPage(Option(hostedPaymentPageFields),
          Option(account), Option(paymentMethod), Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(paymentGatewayActor, BuildComboFormDescriptor(comboHostedPaymentPage, any[List[String]], any[Map[String, String]])).mapTo[String]
        val buildComboFormDescriptorResponse = Await.result(fut, timeout.duration)
        buildComboFormDescriptorResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Process Notification Success Response
  def processNotificationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val processNotificationStream: InputStream = getClass.getResourceAsStream("/getHostedPaymentPageFormDescriptorResponse.json")
    val processNotificationJsonContent = Source.fromInputStream(processNotificationStream, "UTF-8").getLines.mkString
    val processNotificationBodyResponse = HttpEntity(MediaTypes.`application/json`, processNotificationJsonContent.getBytes())
    mockResponse.entity returns processNotificationBodyResponse

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "ProcessNotificationActor")

    "ProcessNotification should" >> {
      "return String response" in {
        val fut: Future[Any] = ask(paymentGatewayActor, ProcessNotification("notification", any[String], any[Map[String, String]])).mapTo[Any]
        val processNotificationResponse = Await.result(fut, timeout.duration)
        processNotificationResponse mustEqual processNotificationBodyResponse
      }
    }
  }

  // Test Process Notification Other Response
  def processNotificationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val processNotificationBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns processNotificationBodyResponse

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "ProcessNotificationOtherActor")

    "ProcessNotification should" >> {
      "return a different status" in {
        val fut: Future[String] = ask(paymentGatewayActor, ProcessNotification("notification", any[String], any[Map[String, String]])).mapTo[String]
        val processNotificationResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        processNotificationResponse mustEqual expected
      }
    }
  }

  // Test Process Notification Failure Response
  def processNotificationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "ProcessNotificationFailureActor")

    "ProcessNotification should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(paymentGatewayActor, ProcessNotification("notification", any[String], any[Map[String, String]])).mapTo[String]
        val processNotificationResponse = Await.result(fut, timeout.duration)
        processNotificationResponse mustEqual expectedErrorMessage
      }
    }
  }

  buildFormDescriptorSuccessResponseTest()
  buildFormDescriptorOtherResponseTest()
  buildFormDescriptorFailureResponseTest()
  buildComboFormDescriptorSuccessResponseTest()
  buildComboFormDescriptorOtherResponseTest()
  buildComboFormDescriptorFailureResponseTest()
  processNotificationSuccessResponseTest()
  processNotificationOtherResponseTest()
  processNotificationFailureResponseTest()
}