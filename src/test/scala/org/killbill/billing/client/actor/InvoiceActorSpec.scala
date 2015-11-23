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
  * Created by jgomez on 20/11/2015.
  */
class InvoiceActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import InvoiceActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Invoices Success Response
  def getInvoicesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoicesStream: InputStream = getClass.getResourceAsStream("/getInvoicesResponse.json")
    val getInvoicesJsonContent = Source.fromInputStream(getInvoicesStream, "UTF-8").getLines.mkString
    val getInvoicesBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoicesJsonContent.getBytes())
    mockResponse.entity returns getInvoicesBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoicesActor")

    "GetInvoices should" >> {
      "return a List of Invoice objects" in {
        val fut: Future[List[Any]] = ask(invoiceActor, GetInvoices(any[Long], any[Long], any[Boolean], any[String])).mapTo[List[Any]]
        val getInvoicesResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoiceResult[Invoice]](InvoiceResult.apply(Option(45), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("2015-11-20"), Option("2015-11-25"), Option("2"), Option(10), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(List[InvoiceItem]()), Option("bundleKey"), Option(List[Credit]())),
          InvoiceResult.apply(Option(30), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("2015-11-10"), Option("2015-11-15"), Option("3"), Option(0), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(List[InvoiceItem]()), Option("bundleKey2"), Option(List[Credit]())))
        getInvoicesResponse mustEqual expected
      }
    }
  }

  // Test Get Invoices Failure Response
  def getInvoicesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoicesFailureActor")

    "GetInvoices should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoices(any[Long], any[Long], any[Boolean], any[String])).mapTo[Any]
        val getInvoicesResponse = Await.result(fut, timeout.duration)
        getInvoicesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice by Id Success Response
  def getInvoiceByIdSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoiceStream: InputStream = getClass.getResourceAsStream("/getInvoiceResponse.json")
    val getInvoiceJsonContent = Source.fromInputStream(getInvoiceStream, "UTF-8").getLines.mkString
    val getInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoiceJsonContent.getBytes())
    mockResponse.entity returns getInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceByIdActor")

    "GetInvoiceById should" >> {
      "return Invoice object" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceById(UUID.randomUUID(), false, "anyAuditMode")).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = Invoice.apply(Option(45), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("2015-11-20"), Option("2015-11-25"), Option("2"), Option(10), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(List[InvoiceItem]()), Option("bundleKey"), Option(List[Credit]()))
        getInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice by Id Failure Response
  def getInvoiceByIdFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceByIdFailureActor")

    "GetInvoiceById should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceById(UUID.randomUUID(), false, "anyAuditMode")).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        getInvoiceResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice by Number Success Response
  def getInvoiceByNumberSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoiceStream: InputStream = getClass.getResourceAsStream("/getInvoiceResponse.json")
    val getInvoiceJsonContent = Source.fromInputStream(getInvoiceStream, "UTF-8").getLines.mkString
    val getInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoiceJsonContent.getBytes())
    mockResponse.entity returns getInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceByNumberActor")

    "GetInvoiceByNumber should" >> {
      "return Invoice object" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceByNumber(1, false, "anyAudit")).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = Invoice.apply(Option(45), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("2015-11-20"), Option("2015-11-25"), Option("2"), Option(10), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(List[InvoiceItem]()), Option("bundleKey"), Option(List[Credit]()))
        getInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice by Number Failure Response
  def getInvoiceByNumberFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceByIdOrNumberFailureActor")

    "GetInvoiceByIdOrNumber should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceByNumber(1, false, "anyAudit")).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        getInvoiceResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice by Id or Number Success Response
  def getInvoiceByIdOrNumberSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoiceStream: InputStream = getClass.getResourceAsStream("/getInvoiceResponse.json")
    val getInvoiceJsonContent = Source.fromInputStream(getInvoiceStream, "UTF-8").getLines.mkString
    val getInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoiceJsonContent.getBytes())
    mockResponse.entity returns getInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceByIdOrNumberActor")

    "GetInvoiceByIdOrNumber should" >> {
      "return Invoice object" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceByIdOrNumber("anyIdOrNumber", false, "anyAudit")).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = Invoice.apply(Option(45), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("2015-11-20"), Option("2015-11-25"), Option("2"), Option(10), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(List[InvoiceItem]()), Option("bundleKey"), Option(List[Credit]()))
        getInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice by Id or Number Failure Response
  def getInvoiceByIdOrNumberFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceByIrOrNumberFailureActor")

    "GetInvoiceByIdOrNumber should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceByIdOrNumber("anyIdOrNumber", false, "anyAudit")).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        getInvoiceResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoices for Account Success Response
  def getInvoicesForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoicesForAccountStream: InputStream = getClass.getResourceAsStream("/getInvoicesResponse.json")
    val getInvoicesForAccountJsonContent = Source.fromInputStream(getInvoicesForAccountStream, "UTF-8").getLines.mkString
    val getInvoicesForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoicesForAccountJsonContent.getBytes())
    mockResponse.entity returns getInvoicesForAccountBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoicesForAccountActor")

    "GetInvoicesForAccount should" >> {
      "return a List of Invoice objects" in {
        val fut: Future[List[Any]] = ask(invoiceActor, GetInvoicesForAccount(UUID.randomUUID(), false, false, "anyAuditMode")).mapTo[List[Any]]
        val getInvoicesForAccountResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoiceResult[Invoice]](InvoiceResult.apply(Option(45), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("2015-11-20"), Option("2015-11-25"), Option("2"), Option(10), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(List[InvoiceItem]()), Option("bundleKey"), Option(List[Credit]())),
          InvoiceResult.apply(Option(30), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("2015-11-10"), Option("2015-11-15"), Option("3"), Option(0), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(List[InvoiceItem]()), Option("bundleKey2"), Option(List[Credit]())))
        getInvoicesForAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Invoices for Account Failure Response
  def getInvoicesForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoicesForAccountFailureActor")

    "GetInvoicesForAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoicesForAccount(UUID.randomUUID(), false, false, "anyAuditMode")).mapTo[Any]
        val getInvoicesForAccountResponse = Await.result(fut, timeout.duration)
        getInvoicesForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search Invoices Success Response
  def searchInvoicesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchInvoicesStream: InputStream = getClass.getResourceAsStream("/getInvoicesResponse.json")
    val searchInvoicesJsonContent = Source.fromInputStream(searchInvoicesStream, "UTF-8").getLines.mkString
    val searchInvoicesBodyResponse = HttpEntity(MediaTypes.`application/json`, searchInvoicesJsonContent.getBytes())
    mockResponse.entity returns searchInvoicesBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchInvoicesActor")

    "SearchInvoices should" >> {
      "return a List of Invoice objects" in {
        val fut: Future[List[Any]] = ask(invoiceActor, SearchInvoices(any[String], any[Long], any[Long], any[Boolean], any[String])).mapTo[List[Any]]
        val searchInvoicesResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoiceResult[Invoice]](InvoiceResult.apply(Option(45), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("2015-11-20"), Option("2015-11-25"), Option("2"), Option(10), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(List[InvoiceItem]()), Option("bundleKey"), Option(List[Credit]())),
          InvoiceResult.apply(Option(30), Option(Currency.USD), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("2015-11-10"), Option("2015-11-15"), Option("3"), Option(0), Option(0), Option(0), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(List[InvoiceItem]()), Option("bundleKey2"), Option(List[Credit]())))
        searchInvoicesResponse mustEqual expected
      }
    }
  }

  // Test Search Invoices Failure Response
  def searchInvoicesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchInvoicesFailureActor")

    "SearchInvoices should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, SearchInvoices(any[String], any[Long], any[Long], any[Boolean], any[String])).mapTo[Any]
        val searchInvoicesResponse = Await.result(fut, timeout.duration)
        searchInvoicesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Invoice Success Response
  def createInvoiceSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoiceActor")

    "CreateInvoice should" >> {
      "return String response" in {
        val fut: Future[String] = ask(invoiceActor, CreateInvoice(UUID.randomUUID(), "2088-11-23")).mapTo[String]
        val createInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Other Response
  def createInvoiceOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoiceOtherResponseActor")

    "CreateInvoice should" >> {
      "return a different status" in {
        val fut: Future[String] = ask(invoiceActor, CreateInvoice(UUID.randomUUID(), "2088-11-23")).mapTo[String]
        val createInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Failure Response
  def createInvoiceFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateInvoiceFailureActor")

    "CreateInvoice should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, CreateInvoice(UUID.randomUUID(), "2088-11-23")).mapTo[String]
        val createInvoiceResponse = Await.result(fut, timeout.duration)
        createInvoiceResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create DryRun Invoice Success Response
  def createDryRunInvoiceSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createDryRunInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createDryRunInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateDryRunInvoiceActor")

    "CreateDryRunInvoice should" >> {
      "return String response" in {
        val dryRunInvoice: InvoiceDryRun = InvoiceDryRun(
          Option(DryRunType.UPCOMING_INVOICE),
          Option(SubscriptionEventType.CHANGE),
          Option(PhaseType.EVERGREEN),
          Option("productName"),
          Option(ProductCategory.ADD_ON),
          Option(BillingPeriod.ANNUAL),
          Option("priceListName"),
          Option("2015-11-10"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(BillingActionPolicy.END_OF_TERM),
          Option(List[PhasePriceOverride]())
        )
        val fut: Future[String] = ask(invoiceActor, CreateDryRunInvoice(UUID.randomUUID(), "2088-11-23", dryRunInvoice)).mapTo[String]
        val createDryRunInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createDryRunInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Create DryRun Invoice Other Response
  def createDryRunInvoiceOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createDryRunInvoiceBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createDryRunInvoiceBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateDryRunInvoiceOtherResponseActor")

    "CreateDryRunInvoice should" >> {
      "return a different status" in {
        val dryRunInvoice: InvoiceDryRun = InvoiceDryRun(
          Option(DryRunType.UPCOMING_INVOICE),
          Option(SubscriptionEventType.CHANGE),
          Option(PhaseType.EVERGREEN),
          Option("productName"),
          Option(ProductCategory.ADD_ON),
          Option(BillingPeriod.ANNUAL),
          Option("priceListName"),
          Option("2015-11-10"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(BillingActionPolicy.END_OF_TERM),
          Option(List[PhasePriceOverride]())
        )
        val fut: Future[String] = ask(invoiceActor, CreateDryRunInvoice(UUID.randomUUID(), "2088-11-23", dryRunInvoice)).mapTo[String]
        val createDryRunInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createDryRunInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Create DryRun Invoice Failure Response
  def createDryRunInvoiceFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateDryRunInvoiceFailureActor")

    "CreateDryRunInvoice should" >> {
      "throw an Exception" in {
        val dryRunInvoice: InvoiceDryRun = InvoiceDryRun(
          Option(DryRunType.UPCOMING_INVOICE),
          Option(SubscriptionEventType.CHANGE),
          Option(PhaseType.EVERGREEN),
          Option("productName"),
          Option(ProductCategory.ADD_ON),
          Option(BillingPeriod.ANNUAL),
          Option("priceListName"),
          Option("2015-11-10"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(BillingActionPolicy.END_OF_TERM),
          Option(List[PhasePriceOverride]())
        )
        val fut: Future[String] = ask(invoiceActor, CreateDryRunInvoice(UUID.randomUUID(), "2088-11-23", dryRunInvoice)).mapTo[String]
        val createDryRunInvoiceResponse = Await.result(fut, timeout.duration)
        createDryRunInvoiceResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Adjust Invoice Item Success Response
  def adjustInvoiceItemSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val adjustInvoiceItemBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns adjustInvoiceItemBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AdjustInvoiceItemActor")

    "AdjustInvoiceItem should" >> {
      "return String response" in {
        val invoiceItem: InvoiceItem = InvoiceItem(
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("planName"),
          Option("phaseName"),
          Option("usageName"),
          Option("itemType"),
          Option("description"),
          Option("2015-11-10"),
          Option("2077-11-10"),
          Option(100),
          Option(Currency.USD)
        )
        val fut: Future[String] = ask(invoiceActor, AdjustInvoiceItem(UUID.randomUUID(), "2088-11-23", invoiceItem)).mapTo[String]
        val adjustInvoiceItemResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        adjustInvoiceItemResponse mustEqual expected
      }
    }
  }

  // Test Adjust Invoice Item Other Response
  def adjustInvoiceItemOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val adjustInvoiceItemBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns adjustInvoiceItemBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AdjustInvoiceItemOtherResponseActor")

    "AdjustInvoiceItem should" >> {
      "return other String response" in {
        val invoiceItem: InvoiceItem = InvoiceItem(
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("planName"),
          Option("phaseName"),
          Option("usageName"),
          Option("itemType"),
          Option("description"),
          Option("2015-11-10"),
          Option("2077-11-10"),
          Option(100),
          Option(Currency.USD)
        )
        val fut: Future[String] = ask(invoiceActor, AdjustInvoiceItem(UUID.randomUUID(), "2088-11-23", invoiceItem)).mapTo[String]
        val adjustInvoiceItemResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        adjustInvoiceItemResponse mustEqual expected
      }
    }
  }

  // Test Adjust Invoice Item Failure Response
  def adjustInvoiceItemFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "AdjustInvoiceItemFailureActor")

    "AdjustInvoiceItem should" >> {
      "throw an Exception" in {
        val invoiceItem: InvoiceItem = InvoiceItem(
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("planName"),
          Option("phaseName"),
          Option("usageName"),
          Option("itemType"),
          Option("description"),
          Option("2015-11-10"),
          Option("2077-11-10"),
          Option(100),
          Option(Currency.USD)
        )
        val fut: Future[String] = ask(invoiceActor, AdjustInvoiceItem(UUID.randomUUID(), "2088-11-23", invoiceItem)).mapTo[String]
        val adjustInvoiceItemResponse = Await.result(fut, timeout.duration)
        adjustInvoiceItemResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create External Charges Success Response
  def createExternalChargesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val createExternalChargesStream: InputStream = getClass.getResourceAsStream("/invoiceItemsResponse.json")
    val createExternalChargesJsonContent = Source.fromInputStream(createExternalChargesStream, "UTF-8").getLines.mkString
    val createExternalChargesBodyResponse = HttpEntity(MediaTypes.`application/json`, createExternalChargesJsonContent.getBytes())
    mockResponse.entity returns createExternalChargesBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateExternalChargesActor")

    "CreateExternalCharges should" >> {
      "return a List of Invoice Item objects" in {
        val externalCharges: List[InvoiceItem] = List[InvoiceItem]()
        val fut: Future[List[Any]] = ask(invoiceActor, CreateExternalCharge(UUID.randomUUID(), "", any[Boolean], externalCharges)).mapTo[List[Any]]
        val createExternalChargesResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoiceItemResult[InvoiceItem]](InvoiceItemResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("planName"), Option("phaseName"), Option("usageName"),
          Option("itemType"), Option("description"), Option("2015-11-10"), Option("2077-11-10"), Option(100), Option(Currency.USD)
          ),
          InvoiceItemResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("planName2"), Option("phaseName2"), Option("usageName2"),
            Option("itemType2"), Option("description2"), Option("2015-11-10"), Option("2077-11-10"), Option(200), Option(Currency.USD)
          ))
        createExternalChargesResponse mustEqual expected
      }
    }
  }

  // Create External Charges Failure Response
  def createExternalChargesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateExternalChargesFailureActor")

    "CreateExternalCharges should" >> {
      "throw an Exception" in {
        val externalCharges: List[InvoiceItem] = List[InvoiceItem]()
        val fut: Future[Any] = ask(invoiceActor, CreateExternalCharge(UUID.randomUUID(), "", any[Boolean], externalCharges)).mapTo[Any]
        val createExternalChargesResponse = Await.result(fut, timeout.duration)
        createExternalChargesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Trigger Invoice Notification Success Response
  def triggerInvoiceNotificationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val triggerInvoiceNotificationBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns triggerInvoiceNotificationBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "TriggerInvoiceNotificationActor")

    "TriggerInvoiceNotification should" >> {
      "return String response" in {
        val fut: Future[String] = ask(invoiceActor, TriggerInvoiceNotification(UUID.randomUUID())).mapTo[String]
        val triggerInvoiceNotificationResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        triggerInvoiceNotificationResponse mustEqual expected
      }
    }
  }

  // Test Trigger Invoice Notification Other Response
  def triggerInvoiceNotificationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val triggerInvoiceNotificationBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns triggerInvoiceNotificationBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "TriggerInvoiceNotificationOtherResponseActor")

    "TriggerInvoiceNotification should" >> {
      "return other String response" in {
        val fut: Future[String] = ask(invoiceActor, TriggerInvoiceNotification(UUID.randomUUID())).mapTo[String]
        val triggerInvoiceNotificationResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        triggerInvoiceNotificationResponse mustEqual expected
      }
    }
  }

  // Test Trigger Invoice Notification Failure Response
  def triggerInvoiceNotificationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "TriggerInvoiceNotificationFailureActor")

    "TriggerInvoiceNotification should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, TriggerInvoiceNotification(UUID.randomUUID())).mapTo[String]
        val triggerInvoiceNotificationResponse = Await.result(fut, timeout.duration)
        triggerInvoiceNotificationResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Upload Invoice Template Success Response
  def uploadInvoiceTemplateSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadInvoiceTemplateResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns uploadInvoiceTemplateResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadInvoiceTemplateActor")

    "UploadInvoiceTemplate should" >> {
      "return String response" in {
        val fut: Future[String] = ask(invoiceActor, UploadInvoiceTemplate("invoiceTemplate", any[Boolean])).mapTo[String]
        val uploadInvoiceTemplateResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        uploadInvoiceTemplateResponse mustEqual expected
      }
    }
  }

  // Test Upload Invoice Template Other Response
  def uploadInvoiceTemplateOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadInvoiceTemplateBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns uploadInvoiceTemplateBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadInvoiceTemplateOtherResponseActor")

    "UploadInvoiceTemplate should" >> {
      "return other String response" in {
        val fut: Future[String] = ask(invoiceActor, UploadInvoiceTemplate("invoiceTemplate", any[Boolean])).mapTo[String]
        val uploadInvoiceTemplateResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        uploadInvoiceTemplateResponse mustEqual expected
      }
    }
  }

  // Test Upload Invoice Template Failure Response
  def uploadInvoiceTemplateFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UploadInvoiceTemplateFailureActor")

    "UploadInvoiceTemplate should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, UploadInvoiceTemplate("invoiceTemplate", any[Boolean])).mapTo[String]
        val uploadInvoiceTemplateResponse = Await.result(fut, timeout.duration)
        uploadInvoiceTemplateResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice Template Success Response
  def getInvoiceTemplateSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoiceTemplateStream: InputStream = getClass.getResourceAsStream("/getInvoiceTemplateResponse.json")
    val getInvoiceTemplateJsonContent = Source.fromInputStream(getInvoiceTemplateStream, "UTF-8").getLines.mkString
    val getInvoiceTemplateBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoiceTemplateJsonContent.getBytes())
    mockResponse.entity returns getInvoiceTemplateBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceTemplateActor")

    "GetInvoiceTemplate should" >> {
      "return Template object" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceTemplate(false)).mapTo[Any]
        val getInvoiceTemplateResponse = Await.result(fut, timeout.duration)
        getInvoiceTemplateResponse mustEqual getInvoiceTemplateBodyResponse
      }
    }
  }

  // Test Get Invoice Template Failure Response
  def getInvoiceTemplateFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceTemplateFailureActor")

    "GetInvoiceTemplateFailure should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceTemplate(false)).mapTo[Any]
        val getInvoiceTemplateResponse = Await.result(fut, timeout.duration)
        getInvoiceTemplateResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Upload Invoice Translation Success Response
  def uploadInvoiceTranslationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadInvoiceTranslationResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns uploadInvoiceTranslationResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadInvoiceTranslationActor")

    "UploadInvoiceTranslation should" >> {
      "return String response" in {
        val fut: Future[String] = ask(invoiceActor, UploadInvoiceTranslation("invoiceTemplate", "")).mapTo[String]
        val uploadInvoiceTranslationResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        uploadInvoiceTranslationResponse mustEqual expected
      }
    }
  }

  // Test Upload Invoice Translation Other Response
  def uploadInvoiceTranslationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadInvoiceTranslationBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns uploadInvoiceTranslationBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadInvoiceTranslationOtherResponseActor")

    "UploadInvoiceTranslation should" >> {
      "return other String response" in {
        val fut: Future[String] = ask(invoiceActor, UploadInvoiceTranslation("invoiceTemplate", "")).mapTo[String]
        val uploadInvoiceTranslationResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        uploadInvoiceTranslationResponse mustEqual expected
      }
    }
  }

  // Test Upload Invoice Translation Failure Response
  def uploadInvoiceTranslationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UploadInvoiceTranslationFailureActor")

    "UploadInvoiceTranslation should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, UploadInvoiceTranslation("invoiceTemplate", "")).mapTo[String]
        val uploadInvoiceTranslationResponse = Await.result(fut, timeout.duration)
        uploadInvoiceTranslationResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice Translation Success Response
  def getInvoiceTranslationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val getInvoiceTranslationResponseEntity = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns getInvoiceTranslationResponseEntity

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceTranslationActor")

    "GetInvoiceTranslation should" >> {
      "return String response" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceTranslation("")).mapTo[Any]
        val getInvoiceTranslationResponse = Await.result(fut, timeout.duration)
        getInvoiceTranslationResponse mustEqual getInvoiceTranslationResponseEntity
      }
    }
  }

  // Test Get Invoice Translation Other Response
  def getInvoiceTranslationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "404"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val getInvoiceTranslationBodyResponse = HttpEntity(MediaTypes.`application/json`, "404")
    mockResponse.entity returns getInvoiceTranslationBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceTranslationOtherResponseActor")

    "GetInvoiceTranslation should" >> {
      "return other String response" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceTranslation("")).mapTo[Any]
        val getInvoiceTranslationResponse = Await.result(fut, timeout.duration)
        val expected = "Invoice Translations not found"
        getInvoiceTranslationResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice Translation Failure Response
  def getInvoiceTranslationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceTranslationFailureActor")

    "GetInvoiceTranslation should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, GetInvoiceTranslation("")).mapTo[String]
        val getInvoiceTranslationResponse = Await.result(fut, timeout.duration)
        getInvoiceTranslationResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Upload Catalog Translation Success Response
  def uploadCatalogTranslationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadCatalogTranslationResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns uploadCatalogTranslationResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadCatalogTranslationActor")

    "UploadCatalogTranslation should" >> {
      "return String response" in {
        val fut: Future[String] = ask(invoiceActor, UploadCatalogTranslation("invoiceTemplate", "")).mapTo[String]
        val uploadCatalogTranslationResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        uploadCatalogTranslationResponse mustEqual expected
      }
    }
  }

  // Test Upload Catalog Translation Other Response
  def uploadCatalogTranslationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val uploadCatalogTranslationBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns uploadCatalogTranslationBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UploadCatalogTranslationOtherResponseActor")

    "UploadCatalogTranslation should" >> {
      "return other String response" in {
        val fut: Future[String] = ask(invoiceActor, UploadCatalogTranslation("invoiceTemplate", "")).mapTo[String]
        val uploadCatalogTranslationResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        uploadCatalogTranslationResponse mustEqual expected
      }
    }
  }

  // Test Upload Catalog Translation Failure Response
  def uploadCatalogTranslationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UploadCatalogTranslationFailureActor")

    "UploadCatalogTranslation should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, UploadCatalogTranslation("invoiceTemplate", "")).mapTo[String]
        val uploadCatalogTranslationResponse = Await.result(fut, timeout.duration)
        uploadCatalogTranslationResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Catalog Translation Success Response
  def getCatalogTranslationSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val getCatalogTranslationResponseEntity = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns getCatalogTranslationResponseEntity

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetCatalogTranslationActor")

    "GetCatalogTranslation should" >> {
      "return String response" in {
        val fut: Future[Any] = ask(invoiceActor, GetCatalogTranslation("")).mapTo[Any]
        val getCatalogTranslationResponse = Await.result(fut, timeout.duration)
        getCatalogTranslationResponse mustEqual getCatalogTranslationResponseEntity
      }
    }
  }

  // Test Get Catalog Translation Other Response
  def getCatalogTranslationOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "404"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val getCatalogTranslationBodyResponse = HttpEntity(MediaTypes.`application/json`, "404")
    mockResponse.entity returns getCatalogTranslationBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetCatalogTranslationOtherResponseActor")

    "GetCatalogTranslation should" >> {
      "return other String response" in {
        val fut: Future[Any] = ask(invoiceActor, GetCatalogTranslation("")).mapTo[Any]
        val getCatalogTranslationResponse = Await.result(fut, timeout.duration)
        val expected = "Catalog Translations not found"
        getCatalogTranslationResponse mustEqual expected
      }
    }
  }

  // Test Get Catalog Translation Failure Response
  def getCatalogTranslationFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetCatalogTranslationFailureActor")

    "GetCatalogTranslation should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(invoiceActor, GetCatalogTranslation("")).mapTo[String]
        val getCatalogTranslationResponse = Await.result(fut, timeout.duration)
        getCatalogTranslationResponse mustEqual expectedErrorMessage
      }
    }
  }

  getInvoicesSuccessResponseTest()
  getInvoicesFailureResponseTest()
  getInvoiceByIdSuccessResponseTest()
  getInvoiceByIdFailureResponseTest()
  getInvoiceByNumberSuccessResponseTest()
  getInvoiceByNumberFailureResponseTest()
  getInvoiceByIdOrNumberSuccessResponseTest()
  getInvoiceByIdOrNumberFailureResponseTest()
  getInvoicesForAccountSuccessResponseTest()
  getInvoicesForAccountFailureResponseTest()
  searchInvoicesSuccessResponseTest()
  searchInvoicesFailureResponseTest()
  createInvoiceSuccessResponseTest()
  createInvoiceOtherResponseTest()
  createInvoiceFailureResponseTest()
  createDryRunInvoiceSuccessResponseTest()
  createDryRunInvoiceOtherResponseTest()
  createDryRunInvoiceFailureResponseTest()
  adjustInvoiceItemSuccessResponseTest()
  adjustInvoiceItemOtherResponseTest()
  adjustInvoiceItemFailureResponseTest()
  createExternalChargesSuccessResponseTest()
  createExternalChargesFailureResponseTest()
  triggerInvoiceNotificationSuccessResponseTest()
  triggerInvoiceNotificationOtherResponseTest()
  triggerInvoiceNotificationFailureResponseTest()
  uploadInvoiceTemplateSuccessResponseTest()
  uploadInvoiceTemplateOtherResponseTest()
  uploadInvoiceTemplateFailureResponseTest()
  getInvoiceTemplateSuccessResponseTest()
  getInvoiceTemplateFailureResponseTest()
  uploadInvoiceTranslationSuccessResponseTest()
  uploadInvoiceTranslationOtherResponseTest()
  uploadInvoiceTranslationFailureResponseTest()
  getInvoiceTranslationSuccessResponseTest()
  getInvoiceTranslationOtherResponseTest()
  getInvoiceTranslationFailureResponseTest()
  uploadCatalogTranslationSuccessResponseTest()
  uploadCatalogTranslationOtherResponseTest()
  uploadCatalogTranslationFailureResponseTest()
  getCatalogTranslationSuccessResponseTest()
  getCatalogTranslationOtherResponseTest()
  getCatalogTranslationFailureResponseTest()
}