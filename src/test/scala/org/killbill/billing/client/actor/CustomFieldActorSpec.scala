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
class CustomFieldActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import CustomFieldActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get CustomFields Success Response
  def getCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getCustomFieldsStream: InputStream = getClass.getResourceAsStream("/getCustomFieldsResponse.json")
    val getCustomFieldsJsonContent = Source.fromInputStream(getCustomFieldsStream, "UTF-8").getLines.mkString
    val getCustomFieldsBodyResponse = HttpEntity(MediaTypes.`application/json`, getCustomFieldsJsonContent.getBytes())
    mockResponse.entity returns getCustomFieldsBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetCustomFieldsActor")

    "GetCustomFields should" >> {
      "return a List of CustomField objects" in {
        val fut: Future[List[Any]] = ask(customFieldActor, GetCustomFields(any[Long], any[Long], any[String])).mapTo[List[Any]]
        val getCustomFieldsResponse = Await.result(fut, timeout.duration)

        val expected = List[CustomFieldResult[CustomField]](
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(ObjectType.CUSTOM_FIELD), Option("name"), Option("value")),
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(ObjectType.CUSTOM_FIELD), Option("name2"), Option("value2")))
        getCustomFieldsResponse mustEqual expected
      }
    }
  }

  // Test Get CustomFields Failure Response
  def getCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetCustomFieldsFailureActor")

    "GetCustomFields should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(customFieldActor, GetCustomFields(any[Long], any[Long], "anyAuditMode")).mapTo[Any]
        val getCustomFieldsResponse = Await.result(fut, timeout.duration)
        getCustomFieldsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search CustomFields Success Response
  def searchCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchCustomFieldsStream: InputStream = getClass.getResourceAsStream("/getCustomFieldsResponse.json")
    val searchCustomFieldsJsonContent = Source.fromInputStream(searchCustomFieldsStream, "UTF-8").getLines.mkString
    val searchCustomFieldsBodyResponse = HttpEntity(MediaTypes.`application/json`, searchCustomFieldsJsonContent.getBytes())
    mockResponse.entity returns searchCustomFieldsBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchCustomFieldsActor")

    "SearchCustomFields should" >> {
      "return a List of CustomField objects" in {
        val fut: Future[List[Any]] = ask(customFieldActor, SearchCustomFields(any[String], any[Long], any[Long], any[String])).mapTo[List[Any]]
        val searchCustomFieldsResponse = Await.result(fut, timeout.duration)

        val expected = List[CustomFieldResult[CustomField]](
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(ObjectType.CUSTOM_FIELD), Option("name"), Option("value")),
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(ObjectType.CUSTOM_FIELD), Option("name2"), Option("value2")))
        searchCustomFieldsResponse mustEqual expected
      }
    }
  }

  // Test Search CustomFields Failure Response
  def searchCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchCustomFieldsFailureActor")

    "SearchCustomFields should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(customFieldActor, SearchCustomFields(any[String], any[Long], any[Long], any[String])).mapTo[Any]
        val searchCustomFieldsResponse = Await.result(fut, timeout.duration)
        searchCustomFieldsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Account CustomFields Success Response
  def getAccountCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountCustomFieldsStream: InputStream = getClass.getResourceAsStream("/getCustomFieldsResponse.json")
    val getAccountCustomFieldsJsonContent = Source.fromInputStream(getAccountCustomFieldsStream, "UTF-8").getLines.mkString
    val getAccountCustomFieldsBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountCustomFieldsJsonContent.getBytes())
    mockResponse.entity returns getAccountCustomFieldsBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountCustomFieldsActor")

    "GetAccountCustomFields should" >> {
      "return a List of CustomField objects" in {
        val fut: Future[List[Any]] = ask(customFieldActor, GetAccountCustomFields(UUID.randomUUID(), "anyAuditMode")).mapTo[List[Any]]
        val getAccountCustomFieldsResponse = Await.result(fut, timeout.duration)

        val expected = List[CustomFieldResult[CustomField]](
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(ObjectType.CUSTOM_FIELD), Option("name"), Option("value")),
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(ObjectType.CUSTOM_FIELD), Option("name2"), Option("value2")))
        getAccountCustomFieldsResponse mustEqual expected
      }
    }
  }

  // Test Get Account CustomFields Failure Response
  def getAccountCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountCustomFieldsFailureActor")

    "GetAccountCustomFields should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(customFieldActor, GetAccountCustomFields(UUID.randomUUID(), "anyAuditMode")).mapTo[Any]
        val getAccountCustomFieldsResponse = Await.result(fut, timeout.duration)
        getAccountCustomFieldsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get PaymentMethod CustomFields Success Response
  def getPaymentMethodCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentMethodCustomFieldsStream: InputStream = getClass.getResourceAsStream("/getCustomFieldsResponse.json")
    val getPaymentMethodCustomFieldsJsonContent = Source.fromInputStream(getPaymentMethodCustomFieldsStream, "UTF-8").getLines.mkString
    val getPaymentMethodCustomFieldsBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentMethodCustomFieldsJsonContent.getBytes())
    mockResponse.entity returns getPaymentMethodCustomFieldsBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentMethodCustomFieldsActor")

    "GetPaymentMethodCustomFields should" >> {
      "return a List of CustomField objects" in {
        val fut: Future[List[Any]] = ask(customFieldActor, GetPaymentMethodCustomFields(UUID.randomUUID(), "anyAuditMode")).mapTo[List[Any]]
        val getPaymentMethodCustomFieldsResponse = Await.result(fut, timeout.duration)

        val expected = List[CustomFieldResult[CustomField]](
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(ObjectType.CUSTOM_FIELD), Option("name"), Option("value")),
          CustomFieldResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(ObjectType.CUSTOM_FIELD), Option("name2"), Option("value2")))
        getPaymentMethodCustomFieldsResponse mustEqual expected
      }
    }
  }

  // Test Get PaymentMethod CustomFields Failure Response
  def getPaymentMethodCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentMethodCustomFieldsFailureActor")

    "GetPaymentMethodCustomFields should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(customFieldActor, GetPaymentMethodCustomFields(UUID.randomUUID(), "anyAuditMode")).mapTo[Any]
        val getPaymentMethodCustomFieldsResponse = Await.result(fut, timeout.duration)
        getPaymentMethodCustomFieldsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Account CustomFields Success Response
  def createAccountCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateAccountCustomFieldActor")

    "CreateAccountCustomField should" >> {
      "return String response" in {
        val customFields: List[CustomField] = List[CustomField]()
        val fut: Future[String] = ask(customFieldActor, CreateAccountCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val createCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Create Account Custom Fields Other Response
  def createAccountCustomFieldsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateAccountCustomFieldOtherResponseActor")

    "CreateAccountCustomField should" >> {
      "return a different status" in {
        val customFields: List[CustomField] = List[CustomField]()
        val fut: Future[String] = ask(customFieldActor, CreateAccountCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val createCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Create Account Custom Fields Failure Response
  def createAccountCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateAccountCustomFieldFailureActor")

    "CreateAccountCustomField should" >> {
      "throw an Exception" in {
        val customFields: List[CustomField] = List[CustomField]()
        val fut: Future[String] = ask(customFieldActor, CreateAccountCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val createCustomFieldResponse = Await.result(fut, timeout.duration)
        createCustomFieldResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Account CustomFields Success Response
  def deleteAccountCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val deleteCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deleteCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteAccountCustomFieldActor")

    "DeleteAccountCustomField should" >> {
      "return String response" in {
        val customFields: List[UUID] = List[UUID]()
        val fut: Future[String] = ask(customFieldActor, DeleteAccountCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val deleteCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deleteCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Delete Account Custom Fields Other Response
  def deleteAccountCustomFieldsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns deleteCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteAccountCustomFieldOtherResponseActor")

    "DeleteAccountCustomField should" >> {
      "return a different status" in {
        val customFields: List[UUID] = List[UUID]()
        val fut: Future[String] = ask(customFieldActor, DeleteAccountCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val deleteCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        deleteCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Delete Account Custom Fields Failure Response
  def deleteAccountCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeleteAccountCustomFieldFailureActor")

    "DeleteAccountCustomField should" >> {
      "throw an Exception" in {
        val customFields: List[UUID] = List[UUID]()
        val fut: Future[String] = ask(customFieldActor, DeleteAccountCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val deleteCustomFieldResponse = Await.result(fut, timeout.duration)
        deleteCustomFieldResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create PaymentMethod CustomFields Success Response
  def createPaymentMethodCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreatePaymentMethodCustomFieldActor")

    "CreatePaymentMethodCustomField should" >> {
      "return String response" in {
        val customFields: List[CustomField] = List[CustomField]()
        val fut: Future[String] = ask(customFieldActor, CreatePaymentMethodCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val createCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Create PaymentMethod Custom Fields Other Response
  def createPaymentMethodCustomFieldsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreatePaymentMethodCustomFieldOtherResponseActor")

    "CreatePaymentMethodCustomField should" >> {
      "return a different status" in {
        val customFields: List[CustomField] = List[CustomField]()
        val fut: Future[String] = ask(customFieldActor, CreatePaymentMethodCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val createCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Create PaymentMethod Custom Fields Failure Response
  def createPaymentMethodCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreatePaymentMethodCustomFieldFailureActor")

    "CreatePaymentMethodCustomField should" >> {
      "throw an Exception" in {
        val customFields: List[CustomField] = List[CustomField]()
        val fut: Future[String] = ask(customFieldActor, CreatePaymentMethodCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val createCustomFieldResponse = Await.result(fut, timeout.duration)
        createCustomFieldResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete PaymentMethod CustomFields Success Response
  def deletePaymentMethodCustomFieldsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val deleteCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deleteCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeletePaymentMethodCustomFieldActor")

    "DeletePaymentMethodCustomField should" >> {
      "return String response" in {
        val customFields: List[UUID] = List[UUID]()
        val fut: Future[String] = ask(customFieldActor, DeletePaymentMethodCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val deleteCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deleteCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Delete PaymentMethod Custom Fields Other Response
  def deletePaymentMethodCustomFieldsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteCustomFieldBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns deleteCustomFieldBodyResponse

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeletePaymentMethodCustomFieldOtherResponseActor")

    "DeletePaymentMethodCustomField should" >> {
      "return a different status" in {
        val customFields: List[UUID] = List[UUID]()
        val fut: Future[String] = ask(customFieldActor, DeletePaymentMethodCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val deleteCustomFieldResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        deleteCustomFieldResponse mustEqual expected
      }
    }
  }

  // Test Delete PaymentMethod Custom Fields Failure Response
  def deletePaymentMethodCustomFieldsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val customFieldActor = system.actorOf(Props(new CustomFieldActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeletePaymentMethodCustomFieldFailureActor")

    "DeletePaymentMethodCustomField should" >> {
      "throw an Exception" in {
        val customFields: List[UUID] = List[UUID]()
        val fut: Future[String] = ask(customFieldActor, DeletePaymentMethodCustomFields(UUID.randomUUID(), customFields)).mapTo[String]
        val deleteCustomFieldResponse = Await.result(fut, timeout.duration)
        deleteCustomFieldResponse mustEqual expectedErrorMessage
      }
    }
  }

  getCustomFieldsSuccessResponseTest()
  getCustomFieldsFailureResponseTest()
  searchCustomFieldsSuccessResponseTest()
  searchCustomFieldsFailureResponseTest()
  getAccountCustomFieldsSuccessResponseTest()
  getAccountCustomFieldsFailureResponseTest()
  getPaymentMethodCustomFieldsSuccessResponseTest()
  getPaymentMethodCustomFieldsFailureResponseTest()
  createAccountCustomFieldsSuccessResponseTest()
  createAccountCustomFieldsOtherResponseTest()
  createAccountCustomFieldsFailureResponseTest()
  deleteAccountCustomFieldsSuccessResponseTest()
  deleteAccountCustomFieldsOtherResponseTest()
  deleteAccountCustomFieldsFailureResponseTest()
  createPaymentMethodCustomFieldsSuccessResponseTest()
  createPaymentMethodCustomFieldsOtherResponseTest()
  createPaymentMethodCustomFieldsFailureResponseTest()
  deletePaymentMethodCustomFieldsSuccessResponseTest()
  deletePaymentMethodCustomFieldsOtherResponseTest()
  deletePaymentMethodCustomFieldsFailureResponseTest()
}