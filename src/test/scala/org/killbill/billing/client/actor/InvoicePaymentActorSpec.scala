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
class InvoicePaymentActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import InvoicePaymentActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Invoice Payments For Account Success Response
  def getInvoicePaymentsForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoicePaymentsForAccountStream: InputStream = getClass.getResourceAsStream("/getInvoicePaymentsResponse.json")
    val getInvoicePaymentsForAccountJsonContent = Source.fromInputStream(getInvoicePaymentsForAccountStream, "UTF-8").getLines.mkString
    val getInvoicePaymentsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoicePaymentsForAccountJsonContent.getBytes())
    mockResponse.entity returns getInvoicePaymentsForAccountBodyResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoicePaymentsForAccountActor")

    "GetInvoicePaymentsForAccount should" >> {
      "return a List of InvoicePayment objects" in {
        val fut: Future[List[Any]] = ask(invoicePaymentActor, GetInvoicePaymentsForAccount(UUID.randomUUID(), any[String], any[String])).mapTo[List[Any]]
        val getInvoicePaymentsForAccountResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoicePaymentResult[InvoicePayment]](InvoicePaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("1"),
          Option("externalKey"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId"),
          Option(List[PaymentTransaction]())),
          InvoicePaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("2"),
            Option("externalKey2"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId2"),
            Option(List[PaymentTransaction]())))
        getInvoicePaymentsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice Payments For Account Failure Response
  def getInvoicePaymentsForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoicePaymentsForAccountFailureActor")

    "GetInvoicePaymentsForAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoicePaymentActor, GetInvoicePaymentsForAccount(UUID.randomUUID(), any[String], any[String])).mapTo[Any]
        val getInvoicePaymentsForAccountResponse = Await.result(fut, timeout.duration)
        getInvoicePaymentsForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice Payment Success Response
  def getInvoicePaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoicePaymentStream: InputStream = getClass.getResourceAsStream("/getInvoicePaymentsResponse.json")
    val getInvoicePaymentJsonContent = Source.fromInputStream(getInvoicePaymentStream, "UTF-8").getLines.mkString
    val getInvoicePaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoicePaymentJsonContent.getBytes())
    mockResponse.entity returns getInvoicePaymentBodyResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoicePaymentActor")

    "GetInvoicePayment should" >> {
      "return a List of InvoicePayment objects" in {
        val fut: Future[List[Any]] = ask(invoicePaymentActor, GetInvoicePayment(UUID.randomUUID())).mapTo[List[Any]]
        val getInvoicePaymentResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoicePaymentResult[InvoicePayment]](InvoicePaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("1"),
          Option("externalKey"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId"),
          Option(List[PaymentTransaction]())),
          InvoicePaymentResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("2"),
            Option("externalKey2"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId2"),
            Option(List[PaymentTransaction]())))
        getInvoicePaymentResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice Payment Failure Response
  def getInvoicePaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoicePaymentFailureActor")

    "GetInvoicePayment should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoicePaymentActor, GetInvoicePayment(UUID.randomUUID())).mapTo[Any]
        val getInvoicePaymentResponse = Await.result(fut, timeout.duration)
        getInvoicePaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Pay All Invoices Success Response
  def payAllInvoicesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val payAllInvoicesResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns payAllInvoicesResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "PayAllInvoicesActor")

    "PayAllInvoices should" >> {
      "return String response" in {
        val fut: Future[String] = ask(invoicePaymentActor, PayAllInvoices(UUID.randomUUID(), any[Boolean], any[BigDecimal])).mapTo[String]
        val payAllInvoicesResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        payAllInvoicesResponse mustEqual expected
      }
    }
  }

  // Test Pay All Invoices Other Response
  def payAllInvoicesOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val payAllInvoicesBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns payAllInvoicesBodyResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "PayAllInvoicesOtherResponseActor")

    "PayAllInvoices should" >> {
      "return other String response" in {
        val fut: Future[String] = ask(invoicePaymentActor, PayAllInvoices(UUID.randomUUID(), any[Boolean], any[BigDecimal])).mapTo[String]
        val payAllInvoicesResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        payAllInvoicesResponse mustEqual expected
      }
    }
  }

  // Test Pay All Invoices Failure Response
  def payAllInvoicesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "PayAllInvoicesFailureActor")

    "PayAllInvoices should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoicePaymentActor, PayAllInvoices(UUID.randomUUID(), any[Boolean], any[BigDecimal])).mapTo[String]
        val payAllInvoicesResponse = Await.result(fut, timeout.duration)
        payAllInvoicesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Invoice Payment Success Response
  def createInvoicePaymentSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoicePaymentResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createInvoicePaymentResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoicePaymentActor")

    "CreateInvoicePayment should" >> {
      "return String response" in {
        val invoicePayment: InvoicePayment = InvoicePayment(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("1"),
          Option("externalKey"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId"),
          Option(List[PaymentTransaction]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePayment(UUID.randomUUID(), invoicePayment, any[Boolean])).mapTo[String]
        val createInvoicePaymentResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createInvoicePaymentResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Payment Other Response
  def createInvoicePaymentOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoicePaymentBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createInvoicePaymentBodyResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoicePaymentOtherResponseActor")

    "CreateInvoicePayment should" >> {
      "return other String response" in {
        val invoicePayment: InvoicePayment = InvoicePayment(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("1"),
          Option("externalKey"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId"),
          Option(List[PaymentTransaction]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePayment(UUID.randomUUID(), invoicePayment, any[Boolean])).mapTo[String]
        val createInvoicePaymentResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createInvoicePaymentResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Payment Failure Response
  def createInvoicePaymentFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateInvoicePaymentFailureActor")

    "CreateInvoicePayment should" >> {
      "throw an Exception" in {
        val invoicePayment: InvoicePayment = InvoicePayment(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("1"),
          Option("externalKey"), Option(100), Option(0), Option(10), Option(0), Option(0), Option("USD"), Option("paymentMethodId"),
          Option(List[PaymentTransaction]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePayment(UUID.randomUUID(), invoicePayment, any[Boolean])).mapTo[String]
        val createInvoicePaymentResponse = Await.result(fut, timeout.duration)
        createInvoicePaymentResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Invoice Payment Refund Success Response
  def createInvoicePaymentRefundSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoicePaymentRefundResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createInvoicePaymentRefundResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoicePaymentRefundActor")

    "CreateInvoicePaymentRefund should" >> {
      "return String response" in {
        val invoicePaymentTransaction: InvoicePaymentTransaction = InvoicePaymentTransaction(Option(false), Option(List[InvoiceItem]()),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("transactionExternalKey"), Option("paymentId"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-10"), Option("status"), Option(10), Option("USD"), Option("gatewayErrorCode"),
          Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"), Option(List[PluginProperty]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentRefund(UUID.randomUUID(), invoicePaymentTransaction)).mapTo[String]
        val createInvoicePaymentRefundResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createInvoicePaymentRefundResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Payment Refund Other Response
  def createInvoicePaymentRefundOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoicePaymentRefundBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createInvoicePaymentRefundBodyResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoicePaymentRefundOtherResponseActor")

    "CreateInvoicePaymentRefund should" >> {
      "return other String response" in {
        val invoicePaymentTransaction: InvoicePaymentTransaction = InvoicePaymentTransaction(Option(false), Option(List[InvoiceItem]()),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("transactionExternalKey"), Option("paymentId"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-10"), Option("status"), Option(10), Option("USD"), Option("gatewayErrorCode"),
          Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"), Option(List[PluginProperty]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentRefund(UUID.randomUUID(), invoicePaymentTransaction)).mapTo[String]
        val createInvoicePaymentRefundResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createInvoicePaymentRefundResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Payment Refund Failure Response
  def createInvoicePaymentRefundFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateInvoicePaymentRefundFailureActor")

    "CreateInvoiceRefundPayment should" >> {
      "throw an Exception" in {
        val invoicePaymentTransaction: InvoicePaymentTransaction = InvoicePaymentTransaction(Option(false), Option(List[InvoiceItem]()),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("transactionExternalKey"), Option("paymentId"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-10"), Option("status"), Option(10), Option("USD"), Option("gatewayErrorCode"),
          Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"), Option(List[PluginProperty]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentRefund(UUID.randomUUID(), invoicePaymentTransaction)).mapTo[String]
        val createInvoicePaymentRefundResponse = Await.result(fut, timeout.duration)
        createInvoicePaymentRefundResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Invoice Payment Chargeback Success Response
  def createInvoicePaymentChargebackSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoicePaymentChargebackResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createInvoicePaymentChargebackResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoicePaymentChargebackActor")

    "CreateInvoicePaymentChargeback should" >> {
      "return String response" in {
        val invoicePaymentTransaction: InvoicePaymentTransaction = InvoicePaymentTransaction(Option(false), Option(List[InvoiceItem]()),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("transactionExternalKey"), Option("paymentId"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-10"), Option("status"), Option(10), Option("USD"), Option("gatewayErrorCode"),
          Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"), Option(List[PluginProperty]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentChargeback(UUID.randomUUID(), invoicePaymentTransaction)).mapTo[String]
        val createInvoicePaymentChargebackResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createInvoicePaymentChargebackResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Payment Chargeback Other Response
  def createInvoicePaymentChargebackOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoicePaymentChargebackBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createInvoicePaymentChargebackBodyResponse

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoicePaymentChargebackOtherResponseActor")

    "CreateInvoicePaymentChargeback should" >> {
      "return other String response" in {
        val chargebackTransaction: InvoicePaymentTransaction = InvoicePaymentTransaction(Option(false), Option(List[InvoiceItem]()),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("transactionExternalKey"), Option("paymentId"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-10"), Option("status"), Option(10), Option("USD"), Option("gatewayErrorCode"),
          Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"), Option(List[PluginProperty]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentChargeback(UUID.randomUUID(), chargebackTransaction)).mapTo[String]
        val createInvoicePaymentChargebackResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createInvoicePaymentChargebackResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Payment Chargeback Failure Response
  def createInvoicePaymentChargebackFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateInvoicePaymentChargebackFailureActor")

    "CreateInvoiceChargebackPayment should" >> {
      "throw an Exception" in {
        val chargebackTransaction: InvoicePaymentTransaction = InvoicePaymentTransaction(Option(false), Option(List[InvoiceItem]()),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("transactionExternalKey"), Option("paymentId"), Option("paymentExternalKey"),
          Option("transactionType"), Option("2015-11-10"), Option("status"), Option(10), Option("USD"), Option("gatewayErrorCode"),
          Option("gatewayErrorMsg"), Option("firstPaymentReferenceId"), Option("secondPaymentReferenceId"), Option(List[PluginProperty]()))
        val fut: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentChargeback(UUID.randomUUID(), chargebackTransaction)).mapTo[String]
        val createInvoicePaymentChargebackResponse = Await.result(fut, timeout.duration)
        createInvoicePaymentChargebackResponse mustEqual expectedErrorMessage
      }
    }
  }

  getInvoicePaymentsForAccountSuccessResponseTest()
  getInvoicePaymentsForAccountFailureResponseTest()
  getInvoicePaymentSuccessResponseTest()
  getInvoicePaymentFailureResponseTest()
  payAllInvoicesSuccessResponseTest()
  payAllInvoicesOtherResponseTest()
  payAllInvoicesFailureResponseTest()
  createInvoicePaymentSuccessResponseTest()
  createInvoicePaymentOtherResponseTest()
  createInvoicePaymentFailureResponseTest()
  createInvoicePaymentRefundSuccessResponseTest()
  createInvoicePaymentRefundOtherResponseTest()
  createInvoicePaymentRefundFailureResponseTest()
  createInvoicePaymentChargebackSuccessResponseTest()
  createInvoicePaymentChargebackOtherResponseTest()
  createInvoicePaymentChargebackFailureResponseTest()
}