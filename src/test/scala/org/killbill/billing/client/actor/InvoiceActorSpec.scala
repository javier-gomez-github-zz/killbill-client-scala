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

        val expected = List[InvoiceResult[Invoice]](InvoiceResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))),
          InvoiceResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("externalKey2"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
              Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(List[EventSubscription]())))))
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
        val fut: Future[Any] = ask(invoiceActor, GetInvoices(any[Long], any[Long], "anyAuditMode")).mapTo[Any]
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
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceById(UUID.randomUUID())).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = Invoice(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]()))))
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
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceById(UUID.randomUUID())).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        getInvoiceResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice by ExternalKey Success Response
  def getInvoiceByExternalKeySuccessResponseTest() = {
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
    }), name = "GetInvoiceByExternalKeyActor")

    "GetInvoiceByExternalKey should" >> {
      "return Invoice object" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceByExternalKey(any[String])).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        val expected = Invoice(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]()))))
        getInvoiceResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice by ExternalKey Failure Response
  def getInvoiceByExternalKeyFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceByExternalKeyFailureActor")

    "GetInvoiceByExternalKey should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetInvoiceByExternalKey(any[String])).mapTo[Any]
        val getInvoiceResponse = Await.result(fut, timeout.duration)
        getInvoiceResponse mustEqual expectedErrorMessage
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
        val fut: Future[List[Any]] = ask(invoiceActor, SearchInvoices(any[String], any[Long], any[Long], any[String])).mapTo[List[Any]]
        val searchInvoicesResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoiceResult[Invoice]](InvoiceResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))),
          InvoiceResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("externalKey2"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
              Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(List[EventSubscription]())))))
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
        val fut: Future[Any] = ask(invoiceActor, SearchInvoices(any[String], any[Long], any[Long], any[String])).mapTo[Any]
        val searchInvoicesResponse = Await.result(fut, timeout.duration)
        searchInvoicesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Account Invoices Success Response
  def getAccountInvoicesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountInvoicesStream: InputStream = getClass.getResourceAsStream("/getInvoicesResponse.json")
    val getAccountInvoicesJsonContent = Source.fromInputStream(getAccountInvoicesStream, "UTF-8").getLines.mkString
    val getAccountInvoicesBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountInvoicesJsonContent.getBytes())
    mockResponse.entity returns getAccountInvoicesBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountInvoicesActor")

    "GetAccountInvoices should" >> {
      "return a List of Invoice objects" in {
        val fut: Future[List[Any]] = ask(invoiceActor, GetAccountInvoices(UUID.randomUUID(), "anyExternalKey")).mapTo[List[Any]]
        val getAccountInvoicesResponse = Await.result(fut, timeout.duration)

        val expected = List[InvoiceResult[Invoice]](InvoiceResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))),
          InvoiceResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("externalKey2"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
              Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(List[EventSubscription]())))))
        getAccountInvoicesResponse mustEqual expected
      }
    }
  }

  // Test Get Account Invoices Failure Response
  def getAccountInvoicesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountInvoicesFailureActor")

    "GetAccountInvoices should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, GetAccountInvoices(UUID.randomUUID(), "anyExternalKey")).mapTo[Any]
        val getAccountInvoicesResponse = Await.result(fut, timeout.duration)
        getAccountInvoicesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Transfer Invoice to Account Success Response
  def transferInvoiceToAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val transferInvoiceToAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns transferInvoiceToAccountBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "TransferInvoiceAccountActor")

    "TransferInvoiceAccount should" >> {
      "return a 201 status" in {
        val fut: Future[String] = ask(invoiceActor, TransferInvoiceToAccount(Invoice(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))), UUID.randomUUID(), BillingActionPolicy.END_OF_TERM)).mapTo[String]
        val transferInvoiceAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        transferInvoiceAccountResponse mustEqual expected
      }
    }
  }

  // Test Transfer Invoice to Account Other Response Response
  def transferInvoiceToAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val transferInvoiceToAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns transferInvoiceToAccountBodyResponse

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "TransferInvoiceAccountOtherResponseActor")

    "TransferInvoiceAccount should" >> {
      "return a 200 status" in {
        val fut: Future[String] = ask(invoiceActor, TransferInvoiceToAccount(Invoice(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))), UUID.randomUUID(), BillingActionPolicy.END_OF_TERM)).mapTo[String]
        val transferInvoiceAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        transferInvoiceAccountResponse mustEqual expected
      }
    }
  }

  // Test Transfer Invoices to Account Failure Response
  def transferInvoiceToAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val invoiceActor = system.actorOf(Props(new InvoiceActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "TransferInvoiceToAccountFailureActor")

    "TransferInvoiceToAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(invoiceActor, TransferInvoiceToAccount(Invoice(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("externalKey"), Option(List[Subscription]()), Option(InvoiceTimeline(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(List[EventSubscription]())))), UUID.randomUUID(), BillingActionPolicy.END_OF_TERM)).mapTo[Any]
        val transferInvoiceToAccountResponse = Await.result(fut, timeout.duration)
        transferInvoiceToAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  getInvoicesSuccessResponseTest()
  getInvoicesFailureResponseTest()
  getInvoiceByIdSuccessResponseTest()
  getInvoiceByIdFailureResponseTest()
  getInvoiceByExternalKeySuccessResponseTest()
  getInvoiceByExternalKeyFailureResponseTest()
  searchInvoicesSuccessResponseTest()
  searchInvoicesFailureResponseTest()
  getAccountInvoicesSuccessResponseTest()
  getAccountInvoicesFailureResponseTest()
  transferInvoiceToAccountSuccessResponseTest()
  transferInvoiceToAccountOtherResponseTest()
  transferInvoiceToAccountFailureResponseTest()
}