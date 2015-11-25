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
class PaymentActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import PaymentActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Payments Success Response
  def getPaymentsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentsStream: InputStream = getClass.getResourceAsStream("/getPaymentsResponse.json")
    val getPaymentsJsonContent = Source.fromInputStream(getPaymentsStream, "UTF-8").getLines.mkString
    val getPaymentsBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentsJsonContent.getBytes())
    mockResponse.entity returns getPaymentsBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentsActor")

    "GetPayments should" >> {
      "return a List of Payment objects" in {
        val fut: Future[List[Any]] = ask(paymentActor, GetPayments(any[Long], any[Long], any[String],
          any[Map[String, String]], any[String], any[Boolean])).mapTo[List[Any]]
        val getPaymentsResponse = Await.result(fut, timeout.duration)

        val expected = List[PaymentResult[Payment]](
          PaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("paymentNumber"), Option("paymentExternalKey"), Option(100), Option(0), Option(0), Option(0), Option(0),
          Option("USD"), Option("paymentMethodId"), Option(List[PaymentTransaction]())),
          PaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("paymentNumber2"), Option("paymentExternalKey2"), Option(100), Option(0), Option(0), Option(0), Option(0),
            Option("USD"), Option("paymentMethodId2"), Option(List[PaymentTransaction]())))
        getPaymentsResponse mustEqual expected
      }
    }
  }

  // Test Get Payments Failure Response
  def getPaymentsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentsFailureActor")

    "GetPayments should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(paymentActor, GetPayments(any[Long], any[Long], any[String],
          any[Map[String, String]], any[String], any[Boolean])).mapTo[Any]
        val getPaymentsResponse = Await.result(fut, timeout.duration)
        getPaymentsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Payment by Id Success Response
  def getPaymentByIdSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentStream: InputStream = getClass.getResourceAsStream("/getPaymentResponse.json")
    val getPaymentJsonContent = Source.fromInputStream(getPaymentStream, "UTF-8").getLines.mkString
    val getPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentJsonContent.getBytes())
    mockResponse.entity returns getPaymentBodyResponse

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentByIdActor")

    "GetPaymentById should" >> {
      "return Payment object" in {
        val fut: Future[Any] = ask(PaymentActor, GetPaymentById(UUID.randomUUID(), any[Boolean],
          any[Map[String, String]], any[String])).mapTo[Any]
        val getPaymentResponse = Await.result(fut, timeout.duration)
        val expected = Payment(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("paymentNumber"), Option("paymentExternalKey"), Option(100), Option(0), Option(0), Option(0), Option(0),
          Option("USD"), Option("paymentMethodId"), Option(List[PaymentTransaction]()))
        getPaymentResponse mustEqual expected
      }
    }
  }

  // Test Get Payment by Id Failure Response
  def getPaymentByIdFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentByIdFailureActor")

    "GetPaymentById should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(PaymentActor, GetPaymentById(UUID.randomUUID(), any[Boolean],
          any[Map[String, String]], any[String])).mapTo[Any]
        val getPaymentResponse = Await.result(fut, timeout.duration)
        getPaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search Payments Success Response
  def searchPaymentsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchPaymentsStream: InputStream = getClass.getResourceAsStream("/getPaymentsResponse.json")
    val searchPaymentsJsonContent = Source.fromInputStream(searchPaymentsStream, "UTF-8").getLines.mkString
    val searchPaymentsBodyResponse = HttpEntity(MediaTypes.`application/json`, searchPaymentsJsonContent.getBytes())
    mockResponse.entity returns searchPaymentsBodyResponse

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchPaymentsActor")

    "SearchPayments should" >> {
      "return a List of Payment objects" in {
        val fut: Future[List[Any]] = ask(PaymentActor, SearchPayments(any[String], any[Long], any[Long], any[String],
          any[Boolean])).mapTo[List[Any]]
        val searchPaymentsResponse = Await.result(fut, timeout.duration)

        val expected = List[PaymentResult[Payment]](
          PaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("paymentNumber"), Option("paymentExternalKey"), Option(100), Option(0), Option(0), Option(0), Option(0),
            Option("USD"), Option("paymentMethodId"), Option(List[PaymentTransaction]())),
          PaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("paymentNumber2"), Option("paymentExternalKey2"), Option(100), Option(0), Option(0), Option(0), Option(0),
            Option("USD"), Option("paymentMethodId2"), Option(List[PaymentTransaction]())))
        searchPaymentsResponse mustEqual expected
      }
    }
  }

  // Test Search Payments Failure Response
  def searchPaymentsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchPaymentsFailureActor")

    "SearchPayments should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(PaymentActor, SearchPayments(any[String], any[Long], any[Long], any[String],
          any[Boolean])).mapTo[Any]
        val searchPaymentsResponse = Await.result(fut, timeout.duration)
        searchPaymentsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Payments For Account Success Response
  def getPaymentsForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentsForAccountStream: InputStream = getClass.getResourceAsStream("/getPaymentsResponse.json")
    val getPaymentsForAccountJsonContent = Source.fromInputStream(getPaymentsForAccountStream, "UTF-8").getLines.mkString
    val getPaymentsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentsForAccountJsonContent.getBytes())
    mockResponse.entity returns getPaymentsForAccountBodyResponse

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentsForAccountActor")

    "GetPaymentsForAccount should" >> {
      "return a List of Payment objects" in {
        val fut: Future[List[Any]] = ask(PaymentActor, GetPaymentsForAccount(UUID.randomUUID(), "anyAuditMode")).mapTo[List[Any]]
        val getPaymentsForAccountResponse = Await.result(fut, timeout.duration)

        val expected = List[PaymentResult[Payment]](
          PaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("paymentNumber"), Option("paymentExternalKey"), Option(100), Option(0), Option(0), Option(0), Option(0),
            Option("USD"), Option("paymentMethodId"), Option(List[PaymentTransaction]())),
          PaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("paymentNumber2"), Option("paymentExternalKey2"), Option(100), Option(0), Option(0), Option(0), Option(0),
            Option("USD"), Option("paymentMethodId2"), Option(List[PaymentTransaction]())))
        getPaymentsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Payments For Account Failure Response
  def getPaymentsForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentsForAccountFailureActor")

    "GetPaymentsForAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(PaymentActor, GetPaymentsForAccount(UUID.randomUUID(), "anyAuditMode")).mapTo[Any]
        val getPaymentsForAccountResponse = Await.result(fut, timeout.duration)
        getPaymentsForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Payment Success Response
  def createPaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val createPaymentStream: InputStream = getClass.getResourceAsStream("/createPaymentResponse.json")
    val createPaymentJsonContent = Source.fromInputStream(createPaymentStream, "UTF-8").getLines.mkString
    val createPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, createPaymentJsonContent.getBytes())
    mockResponse.entity returns createPaymentBodyResponse

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreatePaymentActor")

    "CreatePayment should" >> {
      "return Payment object" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(PaymentActor, CreatePayment(UUID.randomUUID(), UUID.randomUUID(), paymentTransaction,
          any[Map[String, String]])).mapTo[Any]
        val createPaymentResponse = Await.result(fut, timeout.duration)
        val expected = Payment(None, None, None, None, None, None, None, None, None, None, None, None)
        createPaymentResponse mustEqual expected
      }
    }
  }

  // Test Create Payment Failure Response
  def createPaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreatePaymentFailureActor")

    "CreatePayment should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(PaymentActor, CreatePayment(UUID.randomUUID(), UUID.randomUUID(), paymentTransaction,
          any[Map[String, String]])).mapTo[Any]
        val createPaymentResponse = Await.result(fut, timeout.duration)
        createPaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Combo Payment Success Response
  def createComboPaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val createComboPaymentStream: InputStream = getClass.getResourceAsStream("/getPaymentResponse.json")
    val createComboPaymentJsonContent = Source.fromInputStream(createComboPaymentStream, "UTF-8").getLines.mkString
    val createComboPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, createComboPaymentJsonContent.getBytes())
    mockResponse.entity returns createComboPaymentBodyResponse

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateComboPaymentByIdActor")

    "CreateComboPaymentById should" >> {
      "return Payment object" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val comboPaymentTransaction: ComboPaymentTransaction = ComboPaymentTransaction(Option(paymentTransaction), Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(PaymentActor, CreateComboPayment(comboPaymentTransaction,
          any[List[String]], any[Map[String, String]])).mapTo[Any]
        val createComboPaymentResponse = Await.result(fut, timeout.duration)
        val expected = Payment(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("paymentNumber"), Option("paymentExternalKey"), Option(100), Option(0), Option(0), Option(0), Option(0),
          Option("USD"), Option("paymentMethodId"), Option(List[PaymentTransaction]()))
        createComboPaymentResponse mustEqual expected
      }
    }
  }

  // Test Create Combo Payment Failure Response
  def createComboPaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val PaymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateComboPaymentFailureActor")

    "CreateComboPayment should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val comboPaymentTransaction: ComboPaymentTransaction = ComboPaymentTransaction(Option(paymentTransaction), Option(List[PluginProperty]()))
        val fut: Future[Any] = ask(PaymentActor, CreateComboPayment(comboPaymentTransaction,
          any[List[String]], any[Map[String, String]])).mapTo[Any]
        val createComboPaymentResponse = Await.result(fut, timeout.duration)
        createComboPaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Complete Payment Success Response
  def completePaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val completePaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns completePaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CompletePaymentActor")

    "CompletePayment should" >> {
      "return a 201 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, CompletePayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val completePaymentResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        completePaymentResponse mustEqual expected
      }
    }
  }

  // Test Complete Payment Other Response
  def completePaymentOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val completePaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns completePaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CompletePaymentOtherActor")

    "CompletePayment should" >> {
      "return a 200 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, CompletePayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val completePaymentResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        completePaymentResponse mustEqual expected
      }
    }
  }

  // Test Complete Payment Failure Response
  def completePaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CompletePaymentFailureActor")

    "CompletePayment should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, CompletePayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val completePaymentResponse = Await.result(fut, timeout.duration)
        completePaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Capture Authorization Success Response
  def captureAuthorizationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val captureAuthorizationBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns captureAuthorizationBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CaptureAuthorizationActor")

    "CaptureAuthorization should" >> {
      "return a 201 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, CaptureAuthorization(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val captureAuthorizationResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        captureAuthorizationResponse mustEqual expected
      }
    }
  }

  // Test Capture Authorization Other Response
  def captureAuthorizationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val captureAuthorizationBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns captureAuthorizationBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CaptureAuthorizationOtherActor")

    "CaptureAuthorization should" >> {
      "return a 200 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, CaptureAuthorization(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val captureAuthorizationResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        captureAuthorizationResponse mustEqual expected
      }
    }
  }

  // Test Capture Authorization Failure Response
  def captureAuthorizationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CaptureAuthorizationFailureActor")

    "CaptureAuthorization should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, CaptureAuthorization(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val captureAuthorizationResponse = Await.result(fut, timeout.duration)
        captureAuthorizationResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Refund Payment Success Response
  def refundPaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val refundPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns refundPaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RefundPaymentActor")

    "RefundPayment should" >> {
      "return a 201 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, RefundPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val refundPaymentResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        refundPaymentResponse mustEqual expected
      }
    }
  }

  // Test Refund Payment Other Response
  def refundPaymentOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val refundPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns refundPaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RefundPaymentOtherActor")

    "RefundPayment should" >> {
      "return a 200 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, RefundPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val refundPaymentResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        refundPaymentResponse mustEqual expected
      }
    }
  }

  // Test Refund Payment Failure Response
  def refundPaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "RefundPaymentFailureActor")

    "RefundPayment should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, RefundPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val refundPaymentResponse = Await.result(fut, timeout.duration)
        refundPaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Chargeback Payment Success Response
  def chargebackPaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val chargebackPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns chargebackPaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "ChargebackPaymentActor")

    "ChargebackPayment should" >> {
      "return a 201 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, ChargebackPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val chargebackPaymentResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        chargebackPaymentResponse mustEqual expected
      }
    }
  }

  // Test Chargeback Payment Other Response
  def chargebackPaymentOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val chargebackPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns chargebackPaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "ChargebackPaymentOtherActor")

    "ChargebackPayment should" >> {
      "return a 200 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, ChargebackPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val chargebackPaymentResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        chargebackPaymentResponse mustEqual expected
      }
    }
  }

  // Test Chargeback Payment Failure Response
  def chargebackPaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "ChargebackPaymentFailureActor")

    "ChargebackPayment should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, ChargebackPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val chargebackPaymentResponse = Await.result(fut, timeout.duration)
        chargebackPaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Void Payment Success Response
  def voidPaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val voidPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns voidPaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "VoidPaymentActor")

    "VoidPayment should" >> {
      "return a 201 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, VoidPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val voidPaymentResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        voidPaymentResponse mustEqual expected
      }
    }
  }

  // Test Void Payment Other Response
  def voidPaymentOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val voidPaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns voidPaymentBodyResponse

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "VoidPaymentOtherActor")

    "VoidPayment should" >> {
      "return a 200 status" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, VoidPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val voidPaymentResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        voidPaymentResponse mustEqual expected
      }
    }
  }

  // Test Void Payment Failure Response
  def voidPaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val paymentActor = system.actorOf(Props(new PaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "VoidPaymentFailureActor")

    "VoidPayment should" >> {
      "throw an Exception" in {
        val paymentTransaction: PaymentTransaction = PaymentTransaction(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("transactionExternalKey"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-25"), Option("status"), Option(100), Option("USD"),
          Option("gatewayErrorCode"), Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"),
          Option(List[PluginProperty]()))
        val fut: Future[String] = ask(paymentActor, VoidPayment(paymentTransaction, any[Map[String, String]])).mapTo[String]
        val voidPaymentResponse = Await.result(fut, timeout.duration)
        voidPaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  getPaymentsSuccessResponseTest()
  getPaymentsFailureResponseTest()
  getPaymentByIdSuccessResponseTest()
  getPaymentByIdFailureResponseTest()
  searchPaymentsSuccessResponseTest()
  searchPaymentsFailureResponseTest()
  getPaymentsForAccountSuccessResponseTest()
  getPaymentsForAccountFailureResponseTest()
  createPaymentSuccessResponseTest()
  createPaymentFailureResponseTest()
  createComboPaymentSuccessResponseTest()
  createComboPaymentFailureResponseTest()
  completePaymentSuccessResponseTest()
  completePaymentOtherResponseTest()
  completePaymentFailureResponseTest()
  captureAuthorizationSuccessResponseTest()
  captureAuthorizationOtherResponseTest()
  captureAuthorizationFailureResponseTest()
  refundPaymentSuccessResponseTest()
  refundPaymentOtherResponseTest()
  refundPaymentFailureResponseTest()
  chargebackPaymentSuccessResponseTest()
  chargebackPaymentOtherResponseTest()
  chargebackPaymentFailureResponseTest()
  voidPaymentSuccessResponseTest()
  voidPaymentOtherResponseTest()
  voidPaymentFailureResponseTest()
}