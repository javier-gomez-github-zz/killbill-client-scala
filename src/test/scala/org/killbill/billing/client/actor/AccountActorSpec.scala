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
  * Created by jgomez on 23/11/2015.
  */
class AccountActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import AccountActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Account by ExternalKey Success Response
  def getAccountByExternalKeySuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountStream: InputStream = getClass.getResourceAsStream("/getAccountResponse.json")
    val getAccountJsonContent = Source.fromInputStream(getAccountStream, "UTF-8").getLines.mkString
    val getAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountJsonContent.getBytes())
    mockResponse.entity returns getAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountByExternalKeyActor")

    "GetAccountByExternalKey should" >> {
      "return Account object" in {
        val fut: Future[Any] = ask(accountActor, GetAccountByExternalKey(any[String], any[Boolean], any[Boolean], any[String])).mapTo[Any]
        val getAccountResponse = Await.result(fut, timeout.duration)
        val expected = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
        Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
        Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
        Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        getAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Account by ExternalKey Failure Response
  def getAccountByExternalKeyFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountByExternalKeyFailureActor")

    "GetAccountByExternalKey should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, GetAccountByExternalKey(any[String], any[Boolean], any[Boolean], any[String])).mapTo[Any]
        val getAccountResponse = Await.result(fut, timeout.duration)
        getAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Account by Id Success Response
  def getAccountByIdSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountStream: InputStream = getClass.getResourceAsStream("/getAccountResponse.json")
    val getAccountJsonContent = Source.fromInputStream(getAccountStream, "UTF-8").getLines.mkString
    val getAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountJsonContent.getBytes())
    mockResponse.entity returns getAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountByIdActor")

    "GetAccountById should" >> {
      "return Account object" in {
        val fut: Future[Any] = ask(accountActor, GetAccountById(UUID.randomUUID(), false, false, "anyAuditMode")).mapTo[Any]
        val getAccountResponse = Await.result(fut, timeout.duration)
        val expected = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        getAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Account by Id Failure Response
  def getAccountByIdFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountByIdFailureActor")

    "GetAccountById should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, GetAccountById(UUID.randomUUID(), false, false, "anyAuditMode")).mapTo[Any]
        val getAccountResponse = Await.result(fut, timeout.duration)
        getAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Accounts Success Response
  def getAccountsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountsStream: InputStream = getClass.getResourceAsStream("/getAccountsResponse.json")
    val getAccountsJsonContent = Source.fromInputStream(getAccountsStream, "UTF-8").getLines.mkString
    val getAccountsBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountsJsonContent.getBytes())
    mockResponse.entity returns getAccountsBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountsActor")

    "GetAccounts should" >> {
      "return a List of Account objects" in {
        val fut: Future[List[Any]] = ask(accountActor, GetAccounts(any[Long], any[Long], any[String])).mapTo[List[Any]]
        val getAccountsResponse = Await.result(fut, timeout.duration)

        val expected = List[AccountResult[Account]](AccountResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false)),
          AccountResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(0), Option(0),
            Option("name2"), Option(0), Option("email2"), Option(10), Option("USD"), Option("paymentMethodId2"),
            Option("UTC"), Option("address3"), Option("address4"), Option("postalCode2"), Option("company2"),
            Option("city2"), Option("state2"), Option("country2"), Option("locale2"), Option("phone2"), Option(false), Option(false)))
        getAccountsResponse mustEqual expected
      }
    }
  }

  // Test Get Accounts Failure Response
  def getAccountsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountsFailureActor")

    "GetAccounts should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, GetAccounts(any[Long], any[Long], "anyAuditMode")).mapTo[Any]
        val getAccountsResponse = Await.result(fut, timeout.duration)
        getAccountsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search Accounts Success Response
  def searchAccountsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchAccountsStream: InputStream = getClass.getResourceAsStream("/getAccountsResponse.json")
    val searchAccountsJsonContent = Source.fromInputStream(searchAccountsStream, "UTF-8").getLines.mkString
    val searchAccountsBodyResponse = HttpEntity(MediaTypes.`application/json`, searchAccountsJsonContent.getBytes())
    mockResponse.entity returns searchAccountsBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchAccountsActor")

    "SearchAccounts should" >> {
      "return a List of Account objects" in {
        val fut: Future[List[Any]] = ask(accountActor, SearchAccounts(any[String], any[Long], any[Long], any[String])).mapTo[List[Any]]
        val searchAccountsResponse = Await.result(fut, timeout.duration)

        val expected = List[AccountResult[Account]](AccountResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false)),
          AccountResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("externalKey2"), Option(0), Option(0),
            Option("name2"), Option(0), Option("email2"), Option(10), Option("USD"), Option("paymentMethodId2"),
            Option("UTC"), Option("address3"), Option("address4"), Option("postalCode2"), Option("company2"),
            Option("city2"), Option("state2"), Option("country2"), Option("locale2"), Option("phone2"), Option(false), Option(false)))
        searchAccountsResponse mustEqual expected
      }
    }
  }

  // Test Search Accounts Failure Response
  def searchAccountsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchAccountsFailureActor")

    "SearchAccounts should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, SearchAccounts(any[String], any[Long], any[Long], any[String])).mapTo[Any]
        val searchAccountsResponse = Await.result(fut, timeout.duration)
        searchAccountsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Account Success Response
  def createAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateAccountActor")

    "CreateAccount should" >> {
      "return String response" in {
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val fut: Future[String] = ask(accountActor, CreateAccount(account)).mapTo[String]
        val createAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createAccountResponse mustEqual expected
      }
    }
  }

  // Test Create Account Other Response
  def createAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateAccountOtherResponseActor")

    "CreateAccount should" >> {
      "return a different status" in {
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val fut: Future[String] = ask(accountActor, CreateAccount(account)).mapTo[String]
        val createAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createAccountResponse mustEqual expected
      }
    }
  }

  // Test Create Account Failure Response
  def createAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateAccountFailureActor")

    "CreateAccount should" >> {
      "throw an Exception" in {
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val fut: Future[String] = ask(accountActor, CreateAccount(account)).mapTo[String]
        val createAccountResponse = Await.result(fut, timeout.duration)
        createAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Update Account Success Response
  def updateAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val updateAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns updateAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateAccountActor")

    "UpdateAccount should" >> {
      "return String response" in {
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val fut: Future[String] = ask(accountActor, UpdateAccount(account, UUID.randomUUID())).mapTo[String]
        val updateAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        updateAccountResponse mustEqual expected
      }
    }
  }

  // Test Update Account Other Response
  def updateAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updateAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns updateAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateAccountOtherResponseActor")

    "UpdateAccount should" >> {
      "return a different status" in {
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val fut: Future[String] = ask(accountActor, UpdateAccount(account, UUID.randomUUID())).mapTo[String]
        val updateAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        updateAccountResponse mustEqual expected
      }
    }
  }

  // Test Update Account Failure Response
  def updateAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UpdateAccountFailureActor")

    "UpdateAccount should" >> {
      "throw an Exception" in {
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val fut: Future[String] = ask(accountActor, UpdateAccount(account, UUID.randomUUID())).mapTo[String]
        val updateAccountResponse = Await.result(fut, timeout.duration)
        updateAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Remove Email From Account Success Response
  def removeEmailFromAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val removeEmailFromAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns removeEmailFromAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RemoveEmailFromAccountActor")

    "RemoveEmailFromAccount should" >> {
      "return String response" in {
        val fut: Future[String] = ask(accountActor, RemoveEmailFromAccount(UUID.randomUUID(), any[String])).mapTo[String]
        val removeEmailFromAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        removeEmailFromAccountResponse mustEqual expected
      }
    }
  }

  // Test Remove Email From Account Other Response
  def removeEmailFromAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val removeEmailFromAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns removeEmailFromAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "RemoveEmailFromAccountOtherResponseActor")

    "RemoveEmailFromAccount should" >> {
      "return a different status" in {
       val fut: Future[String] = ask(accountActor, RemoveEmailFromAccount(UUID.randomUUID(), any[String])).mapTo[String]
        val removeEmailFromAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        removeEmailFromAccountResponse mustEqual expected
      }
    }
  }

  // Test Remove Email From Account Failure Response
  def removeEmailFromAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "RemoveEmailFromAccountFailureActor")

    "RemoveEmailFromAccount should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(accountActor, RemoveEmailFromAccount(UUID.randomUUID(), any[String])).mapTo[String]
        val removeEmailFromAccountResponse = Await.result(fut, timeout.duration)
        removeEmailFromAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Add Email To Account Success Response
  def addEmailToAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val addEmailToAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns addEmailToAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AddEmailToAccountActor")

    "AddEmailToAccount should" >> {
      "return String response" in {
        val accountEmail: AccountEmail = AccountEmail(Option(UUID.randomUUID().toString), Option("email"))
        val fut: Future[String] = ask(accountActor, AddEmailToAccount(accountEmail, UUID.randomUUID())).mapTo[String]
        val addEmailToAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        addEmailToAccountResponse mustEqual expected
      }
    }
  }

  // Test Add Email To Account Other Response
  def addEmailToAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val addEmailToAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns addEmailToAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AddEmailToAccountOtherResponseActor")

    "AddEmailToAccount should" >> {
      "return a different status" in {
        val accountEmail: AccountEmail = AccountEmail(Option(UUID.randomUUID().toString), Option("email"))
        val fut: Future[String] = ask(accountActor, AddEmailToAccount(accountEmail, UUID.randomUUID())).mapTo[String]
        val addEmailToAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        addEmailToAccountResponse mustEqual expected
      }
    }
  }

  // Test Add Email To Account Failure Response
  def addEmailToAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "AddEmailToAccountFailureActor")

    "AddEmailToAccount should" >> {
      "throw an Exception" in {
        val accountEmail: AccountEmail = AccountEmail(Option(UUID.randomUUID().toString), Option("email"))
        val fut: Future[String] = ask(accountActor, AddEmailToAccount(accountEmail, UUID.randomUUID())).mapTo[String]
        val addEmailToAccountResponse = Await.result(fut, timeout.duration)
        addEmailToAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Add Email To Account Success Response
  def updateEmailNotificationsForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val updateEmailNotificationsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns updateEmailNotificationsForAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateEmailNotificationsForAccountActor")

    "UpdateEmailNotificationsForAccount should" >> {
      "return String response" in {
        val invoiceEmail: InvoiceEmail = InvoiceEmail(Option(UUID.randomUUID().toString), Option(false))
        val fut: Future[String] = ask(accountActor, UpdateEmailNotificationsForAccount(invoiceEmail, UUID.randomUUID())).mapTo[String]
        val updateEmailNotificationsForAccountResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        updateEmailNotificationsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Add Email To Account Other Response
  def updateEmailNotificationsForAccountOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updateEmailNotificationsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns updateEmailNotificationsForAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateEmailNotificationsForAccountOtherResponseActor")

    "UpdateEmailNotificationsForAccount should" >> {
      "return a different status" in {
        val invoiceEmail: InvoiceEmail = InvoiceEmail(Option(UUID.randomUUID().toString), Option(false))
        val fut: Future[String] = ask(accountActor, UpdateEmailNotificationsForAccount(invoiceEmail, UUID.randomUUID())).mapTo[String]
        val updateEmailNotificationsForAccountResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        updateEmailNotificationsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Add Email To Account Failure Response
  def updateEmailNotificationsForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UpdateEmailNotificationsForAccountFailureActor")

    "UpdateEmailNotificationsForAccount should" >> {
      "throw an Exception" in {
        val invoiceEmail: InvoiceEmail = InvoiceEmail(Option(UUID.randomUUID().toString), Option(false))
        val fut: Future[String] = ask(accountActor, UpdateEmailNotificationsForAccount(invoiceEmail, UUID.randomUUID())).mapTo[String]
        val updateEmailNotificationsForAccountResponse = Await.result(fut, timeout.duration)
        updateEmailNotificationsForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Account Timeline Success Response
  def getAccountTimelineSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountTimelineStream: InputStream = getClass.getResourceAsStream("/getAccountTimelineResponse.json")
    val getAccountTimelineJsonContent = Source.fromInputStream(getAccountTimelineStream, "UTF-8").getLines.mkString
    val getAccountTimelineBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountTimelineJsonContent.getBytes())
    mockResponse.entity returns getAccountTimelineBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountTimelineActor")

    "GetAccountTimeline should" >> {
      "return AccountTimeline object" in {
        val fut: Future[Any] = ask(accountActor, GetAccountTimeline(UUID.randomUUID(), "anyAuditMode")).mapTo[Any]
        val getAccountTimelineResponse = Await.result(fut, timeout.duration)
        val account: Account = Account(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("externalKey"), Option(0), Option(0),
          Option("name"), Option(0), Option("email"), Option(10), Option("USD"), Option("paymentMethodId"),
          Option("UTC"), Option("address1"), Option("address2"), Option("postalCode"), Option("company"),
          Option("city"), Option("state"), Option("country"), Option("locale"), Option("phone"), Option(false), Option(false))
        val expected = AccountTimeline(Option(account), Option(List[Bundle]()), Option(List[Invoice]()),
          Option(List[InvoicePayment]()))
        getAccountTimelineResponse mustEqual expected
      }
    }
  }

  // Test Get Account Timeline Failure Response
  def getAccountTimelineFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountTimelineFailureActor")

    "GetAccountTimeline should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, GetAccountTimeline(UUID.randomUUID(), "anyAuditMode")).mapTo[Any]
        val getAccountTimelineResponse = Await.result(fut, timeout.duration)
        getAccountTimelineResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Emails For Account Success Response
  def getEmailsForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getEmailsForAccountStream: InputStream = getClass.getResourceAsStream("/getAccountEmailsResponse.json")
    val getEmailsForAccountJsonContent = Source.fromInputStream(getEmailsForAccountStream, "UTF-8").getLines.mkString
    val getEmailsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getEmailsForAccountJsonContent.getBytes())
    mockResponse.entity returns getEmailsForAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetEmailsForAccountActor")

    "GetEmailsForAccount should" >> {
      "return a List of AccountEmail objects" in {
        val fut: Future[List[Any]] = ask(accountActor, GetEmailsForAccount(UUID.randomUUID())).mapTo[List[Any]]
        val getEmailsForAccountResponse = Await.result(fut, timeout.duration)
        val expected = List[AccountEmailResult[AccountEmail]](
          AccountEmailResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("email")),
          AccountEmailResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("email2"))
        )
        getEmailsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Emails For Account Failure Response
  def getEmailsForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetEmailsForAccountFailureActor")

    "GetEmailsForAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, GetEmailsForAccount(UUID.randomUUID())).mapTo[Any]
        val getEmailsForAccountResponse = Await.result(fut, timeout.duration)
        getEmailsForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Email Notifications For Account Success Response
  def getEmailNotificationsForAccountSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getEmailNotificationsForAccountStream: InputStream = getClass.getResourceAsStream("/getInvoiceEmailResponse.json")
    val getEmailNotificationsForAccountJsonContent = Source.fromInputStream(getEmailNotificationsForAccountStream, "UTF-8").getLines.mkString
    val getEmailNotificationsForAccountBodyResponse = HttpEntity(MediaTypes.`application/json`, getEmailNotificationsForAccountJsonContent.getBytes())
    mockResponse.entity returns getEmailNotificationsForAccountBodyResponse

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetEmailNotificationsForAccountActor")

    "GetEmailNotificationsForAccount should" >> {
      "return InvoiceEmail object" in {
        val fut: Future[Any] = ask(accountActor, GetEmailNotificationsForAccount(UUID.randomUUID())).mapTo[Any]
        val getEmailNotificationsForAccountResponse = Await.result(fut, timeout.duration)
        val expected = InvoiceEmailResult[InvoiceEmail](Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false))
        getEmailNotificationsForAccountResponse mustEqual expected
      }
    }
  }

  // Test Get Email Notifications For Account Failure Response
  def getEmailNotificationsForAccountFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val accountActor = system.actorOf(Props(new AccountActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetEmailNotificationsForAccountFailureActor")

    "GetEmailNotificationsForAccount should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(accountActor, GetEmailNotificationsForAccount(UUID.randomUUID())).mapTo[Any]
        val getEmailNotificationsForAccountResponse = Await.result(fut, timeout.duration)
        getEmailNotificationsForAccountResponse mustEqual expectedErrorMessage
      }
    }
  }

  getAccountByExternalKeySuccessResponseTest()
  getAccountByExternalKeyFailureResponseTest()
  getAccountByIdSuccessResponseTest()
  getAccountByIdFailureResponseTest()
  getAccountsSuccessResponseTest()
  getAccountsFailureResponseTest()
  searchAccountsSuccessResponseTest()
  searchAccountsFailureResponseTest()
  createAccountSuccessResponseTest()
  createAccountOtherResponseTest()
  createAccountFailureResponseTest()
  updateAccountSuccessResponseTest()
  updateAccountOtherResponseTest()
  updateAccountFailureResponseTest()
  removeEmailFromAccountSuccessResponseTest()
  removeEmailFromAccountOtherResponseTest()
  removeEmailFromAccountFailureResponseTest()
  addEmailToAccountSuccessResponseTest()
  addEmailToAccountOtherResponseTest()
  addEmailToAccountFailureResponseTest()
  updateEmailNotificationsForAccountSuccessResponseTest()
  updateEmailNotificationsForAccountOtherResponseTest()
  updateEmailNotificationsForAccountFailureResponseTest()
  getAccountTimelineSuccessResponseTest()
  getAccountTimelineFailureResponseTest()
  getEmailsForAccountSuccessResponseTest()
  getEmailsForAccountFailureResponseTest()
  getEmailNotificationsForAccountSuccessResponseTest()
  getEmailNotificationsForAccountFailureResponseTest()
}