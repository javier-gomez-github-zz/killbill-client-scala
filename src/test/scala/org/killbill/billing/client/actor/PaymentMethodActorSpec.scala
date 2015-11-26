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
class PaymentMethodActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import PaymentMethodActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Payment Methods Success Response
  def getPaymentMethodsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentMethodsStream: InputStream = getClass.getResourceAsStream("/getPaymentMethodsResponse.json")
    val getPaymentMethodsJsonContent = Source.fromInputStream(getPaymentMethodsStream, "UTF-8").getLines.mkString
    val getPaymentMethodsBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentMethodsJsonContent.getBytes())
    mockResponse.entity returns getPaymentMethodsBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentMethodsActor")

    "GetPaymentMethods should" >> {
      "return a List of PaymentMethod objects" in {
        val fut: Future[List[Any]] = ask(paymentMethodActor, GetPaymentMethods(any[Long], any[Long], any[String])).mapTo[List[Any]]
        val getPaymentMethodsResponse = Await.result(fut, timeout.duration)

        val expected = List[PaymentMethodResult[PaymentMethod]](
          PaymentMethodResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("pluginName"),
            Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]())))),
          PaymentMethodResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(false), Option("pluginName2"),
            Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]())))))
        getPaymentMethodsResponse mustEqual expected
      }
    }
  }

  // Test Get Payment Methods Failure Response
  def getPaymentMethodsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentMethodsFailureActor")

    "GetPaymentMethods should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(paymentMethodActor, GetPaymentMethods(any[Long], any[Long], any[String])).mapTo[Any]
        val getPaymentMethodsResponse = Await.result(fut, timeout.duration)
        getPaymentMethodsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search Payment Methods Success Response
  def searchPaymentMethodsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchPaymentMethodsStream: InputStream = getClass.getResourceAsStream("/getPaymentMethodsResponse.json")
    val searchPaymentMethodsJsonContent = Source.fromInputStream(searchPaymentMethodsStream, "UTF-8").getLines.mkString
    val searchPaymentMethodsBodyResponse = HttpEntity(MediaTypes.`application/json`, searchPaymentMethodsJsonContent.getBytes())
    mockResponse.entity returns searchPaymentMethodsBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchPaymentMethodsActor")

    "SearchPaymentMethods should" >> {
      "return a List of PaymentMethod objects" in {
        val fut: Future[List[Any]] = ask(paymentMethodActor, SearchPaymentMethods(any[String], any[Long], any[Long], any[String],
          any[Boolean], "")).mapTo[List[Any]]
        val searchPaymentMethodsResponse = Await.result(fut, timeout.duration)

        val expected = List[PaymentMethodResult[PaymentMethod]](
          PaymentMethodResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("pluginName"),
            Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]())))),
          PaymentMethodResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(false), Option("pluginName2"),
            Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]())))))
        searchPaymentMethodsResponse mustEqual expected
      }
    }
  }

  // Test Search Payment Methods Failure Response
  def searchPaymentMethodsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchPaymentMethodsFailureActor")

    "SearchPaymentMethods should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(paymentMethodActor, SearchPaymentMethods(any[String], any[Long], any[Long], any[String],
        any[Boolean], "")).mapTo[Any]
        val searchPaymentMethodsResponse = Await.result(fut, timeout.duration)
        searchPaymentMethodsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Payment Method by Id Success Response
  def getPaymentMethodByIdSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentMethodStream: InputStream = getClass.getResourceAsStream("/getPaymentMethodResponse.json")
    val getPaymentMethodJsonContent = Source.fromInputStream(getPaymentMethodStream, "UTF-8").getLines.mkString
    val getPaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentMethodJsonContent.getBytes())
    mockResponse.entity returns getPaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentMethodByIdActor")

    "GetPaymentMethodById should" >> {
      "return PaymentMethod object" in {
        val fut: Future[Any] = ask(paymentMethodActor, GetPaymentMethodById(UUID.randomUUID(), any[Boolean], any[String])).mapTo[Any]
        val getPaymentMethodResponse = Await.result(fut, timeout.duration)
        val expected = PaymentMethod(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("pluginName"),
          Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]()))))
        getPaymentMethodResponse mustEqual expected
      }
    }
  }

  // Test Get Payment Method by Id Failure Response
  def getPaymentMethodByIdFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentMethodByIdFailureActor")

    "GetPaymentMethodById should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(paymentMethodActor, GetPaymentMethodById(UUID.randomUUID(), any[Boolean],
          any[String])).mapTo[Any]
        val getPaymentMethodResponse = Await.result(fut, timeout.duration)
        getPaymentMethodResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Payment Method by ExternalKey Success Response
  def getPaymentMethodByExternalKeySuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentMethodStream: InputStream = getClass.getResourceAsStream("/getPaymentMethodResponse.json")
    val getPaymentMethodJsonContent = Source.fromInputStream(getPaymentMethodStream, "UTF-8").getLines.mkString
    val getPaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentMethodJsonContent.getBytes())
    mockResponse.entity returns getPaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentMethodByExternalKeyActor")

    "GetPaymentMethodByExternalKey should" >> {
      "return PaymentMethod object" in {
        val fut: Future[Any] = ask(paymentMethodActor, GetPaymentMethodByExternalKey("anyExternalKey", any[Boolean], any[String])).mapTo[Any]
        val getPaymentMethodResponse = Await.result(fut, timeout.duration)
        val expected = PaymentMethod(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("pluginName"),
          Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]()))))
        getPaymentMethodResponse mustEqual expected
      }
    }
  }

  // Test Get Payment Method by ExternalKey Failure Response
  def getPaymentMethodByExternalKeyFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentMethodByExternalKeyFailureActor")

    "GetPaymentMethodByExternalKey should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(paymentMethodActor, GetPaymentMethodByExternalKey("anyExternalKey", any[Boolean],
          any[String])).mapTo[Any]
        val getPaymentMethodResponse = Await.result(fut, timeout.duration)
        getPaymentMethodResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Payment Methods For Account Success Response
  def getPaymentMethodsForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentMethodsForAccountStream: InputStream = getClass.getResourceAsStream("/getPaymentMethodsResponse.json")
    val getPaymentMethodsForAccountJsonContent = Source.fromInputStream(getPaymentMethodsForAccountStream, "UTF-8").getLines.mkString
    val getPaymentMethodsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentMethodsForAccountJsonContent.getBytes())
    mockResponse.entity returns getPaymentMethodsForAccountBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentMethodsForAccountActor")

    "GetPaymentMethodsForAccount should" >> {
      "return a List of PaymentMethod objects" in {
        val fut: Future[List[Any]] = ask(paymentMethodActor, GetPaymentMethodsForAccount(UUID.randomUUID(), "anyAuditMode")).mapTo[List[Any]]
        val getPaymentMethodsForAccountResponse = Await.result(fut, timeout.duration)

        val expected = List[PaymentMethodResult[PaymentMethod]](
          PaymentMethodResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("pluginName"),
            Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]())))),
          PaymentMethodResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(false), Option("pluginName2"),
            Option(PaymentMethodPluginDetail(Option("2"), Option(false), Option(List[PluginProperty]())))))
        getPaymentMethodsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Payment Methods For Account Failure Response
  def getPaymentMethodsForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentMethodsForAccountFailureActor")

    "GetPaymentMethodsForAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(paymentMethodActor, GetPaymentMethodsForAccount(UUID.randomUUID(), "anyAuditMode")).mapTo[Any]
        val getPaymentMethodsForAccountResponse = Await.result(fut, timeout.duration)
        getPaymentMethodsForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Payment Method Success Response
  def createPaymentMethodSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val createPaymentMethodStream: InputStream = getClass.getResourceAsStream("/createPaymentMethodResponse.json")
    val createPaymentMethodJsonContent = Source.fromInputStream(createPaymentMethodStream, "UTF-8").getLines.mkString
    val createPaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, createPaymentMethodJsonContent.getBytes())
    mockResponse.entity returns createPaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreatePaymentMethodActor")

    "CreatePaymentMethod should" >> {
      "return PaymentMethod object" in {
        val paymentMethod: PaymentMethod = PaymentMethod(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false),
          Option("pluginName"), Option(PaymentMethodPluginDetail(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(false), Option(List[PluginProperty]()))))
        val fut: Future[Any] = ask(paymentMethodActor, CreatePaymentMethod(UUID.randomUUID(), paymentMethod, any[Boolean],
          any[Boolean])).mapTo[Any]
        val createPaymentResponse = Await.result(fut, timeout.duration)
        val expected = PaymentMethod(None, None, None, None, None, None)
        createPaymentResponse mustEqual expected
      }
    }
  }

  // Test Create Payment Failure Response
  def createPaymentMethodFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreatePaymentMethodFailureActor")

    "CreatePaymentMethod should" >> {
      "throw an Exception" in {
        val paymentMethod: PaymentMethod = PaymentMethod(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false),
          Option("pluginName"), Option(PaymentMethodPluginDetail(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option(false), Option(List[PluginProperty]()))))
        val fut: Future[Any] = ask(paymentMethodActor, CreatePaymentMethod(UUID.randomUUID(), paymentMethod, any[Boolean],
          any[Boolean])).mapTo[Any]
        val createPaymentMethodResponse = Await.result(fut, timeout.duration)
        createPaymentMethodResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Update Payment Method Success Response
  def updatePaymentMethodSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val updatePaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns updatePaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdatePaymentMethodActor")

    "UpdatePaymentMethod should" >> {
      "return String response" in {
        val fut: Future[String] = ask(paymentMethodActor, UpdatePaymentMethod(UUID.randomUUID(),
          UUID.randomUUID(), any[Map[String, String]], any[Boolean])).mapTo[String]
        val updatePaymentMethodResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        updatePaymentMethodResponse mustEqual expected
      }
    }
  }

  // Test Update Payment Method Other Response
  def updatePaymentMethodOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updatePaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns updatePaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdatePaymentMethodOtherResponseActor")

    "UpdatePaymentMethod should" >> {
      "return a different status" in {
        val fut: Future[String] = ask(paymentMethodActor, UpdatePaymentMethod(UUID.randomUUID(),
          UUID.randomUUID(), any[Map[String, String]], any[Boolean])).mapTo[String]
        val updatePaymentMethodResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        updatePaymentMethodResponse mustEqual expected
      }
    }
  }

  // Test Update Payment Method Failure Response
  def updatePaymentMethodFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UpdatePaymentMethodFailureActor")

    "UpdatePaymentMethod should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(paymentMethodActor, UpdatePaymentMethod(UUID.randomUUID(),
          UUID.randomUUID(), any[Map[String, String]], any[Boolean])).mapTo[String]
        val updatePaymentMethodResponse = Await.result(fut, timeout.duration)
        updatePaymentMethodResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Payment Method Success Response
  def deletePaymentMethodSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val deletePaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deletePaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeletePaymentMethodActor")

    "DeletePaymentMethod should" >> {
      "return String response" in {
        val fut: Future[String] = ask(paymentMethodActor, DeletePaymentMethod(UUID.randomUUID(),
          any[Boolean], any[Map[String, String]])).mapTo[String]
        val deletePaymentMethodResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deletePaymentMethodResponse mustEqual expected
      }
    }
  }

  // Test Delete Payment Method Other Response
  def deletePaymentMethodOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deletePaymentMethodBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns deletePaymentMethodBodyResponse

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeletePaymentMethodOtherResponseActor")

    "DeletePaymentMethod should" >> {
      "return a different status" in {
        val fut: Future[String] = ask(paymentMethodActor, DeletePaymentMethod(UUID.randomUUID(),
          any[Boolean], any[Map[String, String]])).mapTo[String]
        val deletePaymentMethodResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        deletePaymentMethodResponse mustEqual expected
      }
    }
  }

  // Test Delete Payment Method Failure Response
  def deletePaymentMethodFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeletePaymentMethodFailureActor")

    "DeletePaymentMethod should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(paymentMethodActor, DeletePaymentMethod(UUID.randomUUID(),
          any[Boolean], any[Map[String, String]])).mapTo[String]
        val deletePaymentMethodResponse = Await.result(fut, timeout.duration)
        deletePaymentMethodResponse mustEqual expectedErrorMessage
      }
    }
  }

  getPaymentMethodsSuccessResponseTest()
  getPaymentMethodsFailureResponseTest()
  searchPaymentMethodsSuccessResponseTest()
  searchPaymentMethodsFailureResponseTest()
  getPaymentMethodByIdSuccessResponseTest()
  getPaymentMethodByIdFailureResponseTest()
  getPaymentMethodByExternalKeySuccessResponseTest()
  getPaymentMethodByExternalKeyFailureResponseTest()
  getPaymentMethodsForAccountSuccessResponseTest()
  getPaymentMethodsForAccountFailureResponseTest()
  createPaymentMethodSuccessResponseTest()
  createPaymentMethodFailureResponseTest()
  updatePaymentMethodSuccessResponseTest()
  updatePaymentMethodOtherResponseTest()
  updatePaymentMethodFailureResponseTest()
  deletePaymentMethodSuccessResponseTest()
  deletePaymentMethodOtherResponseTest()
  deletePaymentMethodFailureResponseTest()
}