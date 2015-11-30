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
  * Created by jgomez on 30/11/2015.
  */
class TenantActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import TenantActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Create Tenant Success Response
  def createTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateTenantActor")

    "CreateTenant should" >> {
      "return a 201 status" in {
        val tenant: Tenant = Tenant(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
          Option("apiKey"), Option("apiSecret"))
        val fut: Future[String] = ask(tenantActor, CreateTenant(tenant)).mapTo[String]
        val createTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createTenantResponse mustEqual expected
      }
    }
  }

  // Test Create Tenant Other Response Response
  def createTenantOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateTenantOtherResponseActor")

    "CreateTenant should" >> {
      "return a 200 status" in {
        val tenant: Tenant = Tenant(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
          Option("apiKey"), Option("apiSecret"))
        val fut: Future[String] = ask(tenantActor, CreateTenant(tenant)).mapTo[String]
        val createTenantResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createTenantResponse mustEqual expected
      }
    }
  }

  // Test Create Tenant Failure Response
  def createTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateTenantFailureActor")

    "CreateTenant should" >> {
      "throw an Exception" in {
        val tenant: Tenant = Tenant(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
          Option("apiKey"), Option("apiSecret"))
        val fut: Future[String] = ask(tenantActor, CreateTenant(tenant)).mapTo[String]
        val createTenantResponse = Await.result(fut, timeout.duration)
        createTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Register Callback Notification For Tenant Success Response
  def registerCallbackNotificationForTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val registerCallbackNotificationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns registerCallbackNotificationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RegisterCallbackNotificationForTenantActor")

    "RegisterCallbackNotificationForTenant should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(tenantActor, RegisterCallbackNotificationForTenant("anyCallback")).mapTo[String]
        val registerCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        registerCallbackNotificationForTenantResponse mustEqual expected
      }
    }
  }

  // Test Register Callback Notification For Tenant Other Response Response
  def registerCallbackNotificationForTenantOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val registerCallbackNotificationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns registerCallbackNotificationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RegisterCallbackNotificationForTenantOtherResponseActor")

    "RegisterCallbackNotificationForTenant should" >> {
      "return a 200 status" in {
        val tenant: Tenant = Tenant(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
          Option("apiKey"), Option("apiSecret"))
        val fut: Future[String] = ask(tenantActor, RegisterCallbackNotificationForTenant("anyCallback")).mapTo[String]
        val registerCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        registerCallbackNotificationForTenantResponse mustEqual expected
      }
    }
  }

  // Test Register Callback Notification For Tenant Failure Response
  def registerCallbackNotificationForTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "RegisterCallbackNotificationForTenantFailureActor")

    "RegisterCallbackNotificationForTenant should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tenantActor, RegisterCallbackNotificationForTenant("anyCallback")).mapTo[String]
        val registerCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        registerCallbackNotificationForTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Register Plugin Configuration For Tenant Success Response
  def registerPluginConfigurationForTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val registerPluginConfigurationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns registerPluginConfigurationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RegisterPluginConfigurationForTenantActor")

    "RegisterPluginConfigurationForTenant should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(tenantActor, RegisterPluginConfigurationForTenant("anyPluginName", "anyPluginConfig")).mapTo[String]
        val registerPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        registerPluginConfigurationForTenantResponse mustEqual expected
      }
    }
  }

  // Test Register Plugin Configuration For Tenant Other Response Response
  def registerPluginConfigurationForTenantOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val registerPluginConfigurationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns registerPluginConfigurationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RegisterPluginConfigurationForTenantOtherResponseActor")

    "RegisterPluginConfigurationForTenant should" >> {
      "return a 200 status" in {
        val fut: Future[String] = ask(tenantActor, RegisterPluginConfigurationForTenant("anyPluginName", "anyPluginConfig")).mapTo[String]
        val registerPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        registerPluginConfigurationForTenantResponse mustEqual expected
      }
    }
  }

  // Test Register Plugin Configuration For Tenant Failure Response
  def registerPluginConfigurationForTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "RegisterPluginConfigurationForTenantFailureActor")

    "RegisterPluginConfigurationForTenant should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tenantActor, RegisterPluginConfigurationForTenant("anyPluginName", "anyPluginConfig")).mapTo[String]
        val registerPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        registerPluginConfigurationForTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Callback Notification For Tenant Success Response
  def getCallbackNotificationForTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val getCallbackNotificationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns getCallbackNotificationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetCallbackNotificationForTenantActor")

    "GetCallbackNotificationForTenant should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(tenantActor, GetCallbackNotificationForTenant()).mapTo[String]
        val getCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        getCallbackNotificationForTenantResponse mustEqual expected
      }
    }
  }

  // Test Get Callback Notification For Tenant Failure Response
  def getCallbackNotificationForTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetCallbackNotificationForTenantFailureActor")

    "GetCallbackNotificationForTenant should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tenantActor, GetCallbackNotificationForTenant()).mapTo[String]
        val getCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        getCallbackNotificationForTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Plugin Configuration For Tenant Success Response
  def getPluginConfigurationForTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val getPluginConfigurationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns getPluginConfigurationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPluginConfigurationForTenantActor")

    "GetPluginConfigurationForTenant should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(tenantActor, GetPluginConfigurationForTenant("anyPluginName")).mapTo[String]
        val getPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        getPluginConfigurationForTenantResponse mustEqual expected
      }
    }
  }

  // Test Get Plugin Configuration For Tenant Failure Response
  def getPluginConfigurationForTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPluginConfigurationForTenantFailureActor")

    "GetPluginConfigurationForTenant should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tenantActor, GetPluginConfigurationForTenant("anyPluginName")).mapTo[String]
        val getPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        getPluginConfigurationForTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test UnRegister Callback Notification For Tenant Success Response
  def unRegisterCallbackNotificationForTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val unRegisterCallbackNotificationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns unRegisterCallbackNotificationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UnRegisterCallbackNotificationForTenantActor")

    "UnRegisterCallbackNotificationForTenant should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(tenantActor, UnRegisterCallbackNotificationForTenant()).mapTo[String]
        val unRegisterCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        unRegisterCallbackNotificationForTenantResponse mustEqual expected
      }
    }
  }

  // Test UnRegister Callback Notification For Tenant Failure Response
  def unRegisterCallbackNotificationForTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UnRegisterCallbackNotificationForTenantFailureActor")

    "UnRegisterCallbackNotificationForTenant should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tenantActor, UnRegisterCallbackNotificationForTenant()).mapTo[String]
        val unRegisterCallbackNotificationForTenantResponse = Await.result(fut, timeout.duration)
        unRegisterCallbackNotificationForTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test UnRegister Plugin Configuration For Tenant Success Response
  def unRegisterPluginConfigurationForTenantSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val unRegisterPluginConfigurationForTenantBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns unRegisterPluginConfigurationForTenantBodyResponse

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UnRegisterPluginConfigurationForTenantActor")

    "UnRegisterPluginConfigurationForTenant should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(tenantActor, UnRegisterPluginConfigurationForTenant("anyPluginName")).mapTo[String]
        val unRegisterPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        unRegisterPluginConfigurationForTenantResponse mustEqual expected
      }
    }
  }

  // Test UnRegister Plugin Configuration For Tenant Failure Response
  def unRegisterPluginConfigurationForTenantFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tenantActor = system.actorOf(Props(new TenantActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UnRegisterPluginConfigurationForTenantFailureActor")

    "UnRegisterPluginConfigurationForTenant should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tenantActor, UnRegisterPluginConfigurationForTenant("anyPluginName")).mapTo[String]
        val unRegisterPluginConfigurationForTenantResponse = Await.result(fut, timeout.duration)
        unRegisterPluginConfigurationForTenantResponse mustEqual expectedErrorMessage
      }
    }
  }

  createTenantSuccessResponseTest()
  createTenantOtherResponseTest()
  createTenantFailureResponseTest()
  registerCallbackNotificationForTenantSuccessResponseTest()
  registerCallbackNotificationForTenantOtherResponseTest()
  registerCallbackNotificationForTenantFailureResponseTest()
  registerPluginConfigurationForTenantSuccessResponseTest()
  registerPluginConfigurationForTenantOtherResponseTest()
  registerPluginConfigurationForTenantFailureResponseTest()
  getCallbackNotificationForTenantSuccessResponseTest()
  getCallbackNotificationForTenantFailureResponseTest()
  getPluginConfigurationForTenantSuccessResponseTest()
  getPluginConfigurationForTenantFailureResponseTest()
  unRegisterCallbackNotificationForTenantSuccessResponseTest()
  unRegisterCallbackNotificationForTenantFailureResponseTest()
  unRegisterPluginConfigurationForTenantSuccessResponseTest()
  unRegisterPluginConfigurationForTenantFailureResponseTest()
}