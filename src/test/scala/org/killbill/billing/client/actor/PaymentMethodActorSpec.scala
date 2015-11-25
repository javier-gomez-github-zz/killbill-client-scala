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

//  // Test Create Payment Success Response
//  def createPaymentSuccessResponseTest() = {
//    val mockResponse = mock[HttpResponse]
//    val mockStatus = mock[StatusCode]
//    mockResponse.status returns mockStatus
//    mockStatus.isSuccess returns true
//    val createPaymentStream: InputStream = getClass.getResourceAsStream("/createPaymentResponse.json")
//    val createPaymentJsonContent = Source.fromInputStream(createPaymentStream, "UTF-8").getLines.mkString
//    val createPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, createPaymentJsonContent.getBytes())
//    mockResponse.entity returns createPaymentBodyResponse
//
//    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
//      override def sendAndReceive = {
//        (req:HttpRequest) => Future.apply(mockResponse)
//      }
//    }), name = "CreatePaymentActor")
//
//    "CreatePayment should" >> {
//      "return Payment object" in {
//        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
//          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
//          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
//          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
//          Option(List[PluginProperty]()))
//        val fut: Future[Any] = ask(PaymentActor, CreatePayment(UUID.randomUUID(), UUID.randomUUID(), paymentTransaction,
//          any[Map[String, String]])).mapTo[Any]
//        val createPaymentResponse = Await.result(fut, timeout.duration)
//        val expected = Payment(None, None, None, None, None, None, None, None, None, None, None, None)
//        createPaymentResponse mustEqual expected
//      }
//    }
//  }
//
//  // Test Create Payment Failure Response
//  def createPaymentFailureResponseTest() = {
//    val mockFailureResponse = mock[UnsuccessfulResponseException]
//    val expectedErrorMessage = "Error"
//
//    mockFailureResponse.getMessage returns expectedErrorMessage
//
//    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
//      override def sendAndReceive = {
//        (req:HttpRequest) => Future.failed(mockFailureResponse)
//      }
//    }), name = "CreatePaymentFailureActor")
//
//    "CreatePayment should" >> {
//      "throw an Exception" in {
//        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
//          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
//          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
//          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
//          Option(List[PluginProperty]()))
//        val fut: Future[Any] = ask(PaymentActor, CreatePayment(UUID.randomUUID(), UUID.randomUUID(), paymentTransaction,
//          any[Map[String, String]])).mapTo[Any]
//        val createPaymentResponse = Await.result(fut, timeout.duration)
//        createPaymentResponse mustEqual expectedErrorMessage
//      }
//    }
//  }


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
//  createPaymentSuccessResponseTest()
//  createPaymentFailureResponseTest()
//  updateAccountSuccessResponseTest()
//  updateAccountOtherResponseTest()
//  removeEmailFromAccountSuccessResponseTest()
//  removeEmailFromAccountOtherResponseTest()
//  removeEmailFromAccountFailureResponseTest()
}