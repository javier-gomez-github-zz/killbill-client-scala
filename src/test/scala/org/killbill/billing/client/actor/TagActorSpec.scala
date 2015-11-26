package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model.{ObjectType, Tag, TagResult, OverdueState}
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * Created by jgomez on 26/11/2015.
  */
class TagActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import TagActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Tags Success Response
  def getTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getTagsJsonContent = Source.fromInputStream(getTagsStream, "UTF-8").getLines.mkString
    val getTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getTagsJsonContent.getBytes())
    mockResponse.entity returns getTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetTagsActor")

    "GetTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetTags(any[Long], any[Long], any[String])).mapTo[List[Any]]
        val getTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getTagsResponse mustEqual expected
      }
    }
  }

  // Test Get Tags Failure Response
  def getTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetTagsFailureActor")

    "GetTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetTags(any[Long], any[Long], "anyAuditMode")).mapTo[Any]
        val getTagsResponse = Await.result(fut, timeout.duration)
        getTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Search Tags Success Response
  def searchTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val searchTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val searchTagsJsonContent = Source.fromInputStream(searchTagsStream, "UTF-8").getLines.mkString
    val searchTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, searchTagsJsonContent.getBytes())
    mockResponse.entity returns searchTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "SearchTagsActor")

    "SearchTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, SearchTags("anySearchKey", any[Long], any[Long], any[String])).mapTo[List[Any]]
        val searchTagsResponse = Await.result(fut, timeout.duration)
        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        searchTagsResponse mustEqual expected
      }
    }
  }

  // Test Search Tags Failure Response
  def searchTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "SearchTagsFailureActor")

    "SearchTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, SearchTags("anySearchKey", any[Long], any[Long], "anyAuditMode")).mapTo[Any]
        val searchTagsResponse = Await.result(fut, timeout.duration)
        searchTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get All Account Tags Success Response
  def getAllAccountTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAllAccountTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getAllAccountTagsJsonContent = Source.fromInputStream(getAllAccountTagsStream, "UTF-8").getLines.mkString
    val getAllAccountTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getAllAccountTagsJsonContent.getBytes())
    mockResponse.entity returns getAllAccountTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAllAccountTagsActor")

    "GetAllAccountTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetAllAccountTags(UUID.randomUUID(), "anyObjectType", any[String],
          any[Boolean])).mapTo[List[Any]]
        val getAllAccountTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getAllAccountTagsResponse mustEqual expected
      }
    }
  }

  // Test Get All Account Tags Failure Response
  def getAllAccountTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAllAccountTagsFailureActor")

    "GetAllAccountTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetAllAccountTags(UUID.randomUUID(), "anyObjectType",
          any[String], any[Boolean])).mapTo[Any]
        val getAllAccountTagsResponse = Await.result(fut, timeout.duration)
        getAllAccountTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Account Success Response
  def createAccountTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createAccountTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createAccountTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateAccountTagsActor")

    "CreateAccountTags should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(tagActor, CreateAccountTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createAccountTagsResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createAccountTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Account Other Response Response
  def createAccountTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createAccountTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createAccountTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateAccountTagsOtherResponseActor")

    "CreateAccountTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, CreateAccountTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createAccountTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createAccountTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Account Failure Response
  def createAccountTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateAccountTagsFailureActor")

    "CreateAccountTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, CreateAccountTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createAccountTagsResponse = Await.result(fut, timeout.duration)
        createAccountTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Account Success Response
  def deleteAccountTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "204"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteAccountTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns deleteAccountTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteAccountTagsActor")

    "DeleteAccountTags should" >> {
      "return a 204 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteAccountTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteAccountTagsResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        deleteAccountTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Account Other Response Response
  def deleteAccountTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteAccountTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deleteAccountTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteAccountTagsOtherResponseActor")

    "DeleteAccountTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteAccountTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteAccountTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deleteAccountTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Account Failure Response
  def deleteAccountTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeleteAccountTagsFailureActor")

    "DeleteAccountTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, DeleteAccountTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteAccountTagsResponse = Await.result(fut, timeout.duration)
        deleteAccountTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Account Tags Success Response
  def getAccountTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getAccountTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getAccountTagsJsonContent = Source.fromInputStream(getAccountTagsStream, "UTF-8").getLines.mkString
    val getAccountTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getAccountTagsJsonContent.getBytes())
    mockResponse.entity returns getAccountTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetAccountTagsActor")

    "GetAccountTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetAccountTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[List[Any]]
        val getAccountTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getAccountTagsResponse mustEqual expected
      }
    }
  }

  // Test Get Account Tags Failure Response
  def getAccountTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetAccountTagsFailureActor")

    "GetAccountTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetAccountTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[Any]
        val getAccountTagsResponse = Await.result(fut, timeout.duration)
        getAccountTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Bundle Tags Success Response
  def createBundleTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createBundleTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createBundleTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateBundleTagsActor")

    "CreateBundleTags should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(tagActor, CreateBundleTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createBundleTagsResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createBundleTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Bundle Tags Other Response Response
  def createBundleTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createBundleTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createBundleTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateBundleTagsOtherResponseActor")

    "CreateBundleTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, CreateBundleTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createBundleTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createBundleTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Bundle Tags Failure Response
  def createBundleTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateBundleTagsFailureActor")

    "CreateBundleTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, CreateBundleTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createBundleTagsResponse = Await.result(fut, timeout.duration)
        createBundleTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Bundle Tags Success Response
  def deleteBundleTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "204"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteBundleTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns deleteBundleTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteBundleTagsActor")

    "DeleteBundleTags should" >> {
      "return a 204 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteBundleTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteBundleTagsResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        deleteBundleTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Bundle Tags Other Response Response
  def deleteBundleTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteBundleTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deleteBundleTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteBundleTagsOtherResponseActor")

    "DeleteBundleTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteBundleTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteBundleTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deleteBundleTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Bundle Tags Failure Response
  def deleteBundleTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeleteBundleTagsFailureActor")

    "DeleteBundleTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, DeleteBundleTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteBundleTagsResponse = Await.result(fut, timeout.duration)
        deleteBundleTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Bundle Tags Success Response
  def getBundleTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getBundleTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getBundleTagsJsonContent = Source.fromInputStream(getBundleTagsStream, "UTF-8").getLines.mkString
    val getBundleTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getBundleTagsJsonContent.getBytes())
    mockResponse.entity returns getBundleTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetBundleTagsActor")

    "GetBundleTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetBundleTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[List[Any]]
        val getBundleTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getBundleTagsResponse mustEqual expected
      }
    }
  }

  // Test Get Bundle Tags Failure Response
  def getBundleTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetBundleTagsFailureActor")

    "GetBundleTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetBundleTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[Any]
        val getBundleTagsResponse = Await.result(fut, timeout.duration)
        getBundleTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Subscription Tags Success Response
  def createSubscriptionTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createSubscriptionTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createSubscriptionTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateSubscriptionTagsActor")

    "CreateSubscriptionTags should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(tagActor, CreateSubscriptionTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createSubscriptionTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Subscription Tags Other Response Response
  def createSubscriptionTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createSubscriptionTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createSubscriptionTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateSubscriptionTagsOtherResponseActor")

    "CreateSubscriptionTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, CreateSubscriptionTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createSubscriptionTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Subscription Tags Failure Response
  def createSubscriptionTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateSubscriptionTagsFailureActor")

    "CreateSubscriptionTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, CreateSubscriptionTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        createSubscriptionTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Subscription Tags Success Response
  def deleteSubscriptionTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "204"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteSubscriptionTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns deleteSubscriptionTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteSubscriptionTagsActor")

    "DeleteSubscriptionTags should" >> {
      "return a 204 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteSubscriptionTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        deleteSubscriptionTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Subscription Tags Other Response Response
  def deleteSubscriptionTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteSubscriptionTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deleteSubscriptionTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteSubscriptionTagsOtherResponseActor")

    "DeleteSubscriptionTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteSubscriptionTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deleteSubscriptionTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Subscription Tags Failure Response
  def deleteSubscriptionTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeleteSubscriptionTagsFailureActor")

    "DeleteSubscriptionTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, DeleteSubscriptionTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        deleteSubscriptionTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Subscription Tags Success Response
  def getSubscriptionTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getSubscriptionTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getSubscriptionTagsJsonContent = Source.fromInputStream(getSubscriptionTagsStream, "UTF-8").getLines.mkString
    val getSubscriptionTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getSubscriptionTagsJsonContent.getBytes())
    mockResponse.entity returns getSubscriptionTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetSubscriptionTagsActor")

    "GetSubscriptionTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetSubscriptionTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[List[Any]]
        val getSubscriptionTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getSubscriptionTagsResponse mustEqual expected
      }
    }
  }

  // Test Get Subscription Tags Failure Response
  def getSubscriptionTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetSubscriptionTagsFailureActor")

    "GetSubscriptionTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetSubscriptionTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[Any]
        val getSubscriptionTagsResponse = Await.result(fut, timeout.duration)
        getSubscriptionTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Invoice Tags Success Response
  def createInvoiceTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createInvoiceTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createInvoiceTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoiceTagsActor")

    "CreateInvoiceTags should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(tagActor, CreateInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createInvoiceTagsResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createInvoiceTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Tags Other Response Response
  def createInvoiceTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createInvoiceTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createInvoiceTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateInvoiceTagsOtherResponseActor")

    "CreateInvoiceTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, CreateInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createInvoiceTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createInvoiceTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Invoice Tags Failure Response
  def createInvoiceTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateInvoiceTagsFailureActor")

    "CreateInvoiceTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, CreateInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createInvoiceTagsResponse = Await.result(fut, timeout.duration)
        createInvoiceTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Invoice Tags Success Response
  def deleteInvoiceTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "204"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteInvoiceTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns deleteInvoiceTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteInvoiceTagsActor")

    "DeleteInvoiceTags should" >> {
      "return a 204 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteInvoiceTagsResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        deleteInvoiceTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Invoice Tags Other Response Response
  def deleteInvoiceTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteInvoiceTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deleteInvoiceTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteInvoiceTagsOtherResponseActor")

    "DeleteInvoiceTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, DeleteInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteInvoiceTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deleteInvoiceTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Invoice Tags Failure Response
  def deleteInvoiceTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeleteInvoiceTagsFailureActor")

    "DeleteInvoiceTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, DeleteInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deleteInvoiceTagsResponse = Await.result(fut, timeout.duration)
        deleteInvoiceTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Invoice Tags Success Response
  def getInvoiceTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getInvoiceTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getInvoiceTagsJsonContent = Source.fromInputStream(getInvoiceTagsStream, "UTF-8").getLines.mkString
    val getInvoiceTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getInvoiceTagsJsonContent.getBytes())
    mockResponse.entity returns getInvoiceTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetInvoiceTagsActor")

    "GetInvoiceTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetInvoiceTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[List[Any]]
        val getInvoiceTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getInvoiceTagsResponse mustEqual expected
      }
    }
  }

  // Test Get Invoice Tags Failure Response
  def getInvoiceTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetInvoiceTagsFailureActor")

    "GetInvoiceTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetInvoiceTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[Any]
        val getInvoiceTagsResponse = Await.result(fut, timeout.duration)
        getInvoiceTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Payment Tags Success Response
  def createPaymentTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createPaymentTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns createPaymentTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreatePaymentTagsActor")

    "CreatePaymentTags should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(tagActor, CreatePaymentTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createPaymentTagsResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        createPaymentTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Payment Tags Other Response Response
  def createPaymentTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createPaymentTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createPaymentTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreatePaymentTagsOtherResponseActor")

    "CreatePaymentTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, CreatePaymentTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createPaymentTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createPaymentTagsResponse mustEqual expected
      }
    }
  }

  // Test Create Payment Tags Failure Response
  def createPaymentTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreatePaymentTagsFailureActor")

    "CreatePaymentTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, CreatePaymentTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val createPaymentTagsResponse = Await.result(fut, timeout.duration)
        createPaymentTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Payment Tags Success Response
  def deletePaymentTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "204"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deletePaymentTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns deletePaymentTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeletePaymentTagsActor")

    "DeletePaymentTags should" >> {
      "return a 204 status" in {
        val fut: Future[Any] = ask(tagActor, DeletePaymentTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deletePaymentTagsResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        deletePaymentTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Payment Tags Other Response Response
  def deletePaymentTagsOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deletePaymentTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns deletePaymentTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeletePaymentTagsOtherResponseActor")

    "DeletePaymentTags should" >> {
      "return a 200 status" in {
        val fut: Future[Any] = ask(tagActor, DeletePaymentTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deletePaymentTagsResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        deletePaymentTagsResponse mustEqual expected
      }
    }
  }

  // Test Delete Payment Tags Failure Response
  def deletePaymentTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeletePaymentTagsFailureActor")

    "DeletePaymentTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, DeleteInvoiceTag(UUID.randomUUID(), UUID.randomUUID())).mapTo[Any]
        val deletePaymentTagsResponse = Await.result(fut, timeout.duration)
        deletePaymentTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Payment Tags Success Response
  def getPaymentTagsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPaymentTagsStream: InputStream = getClass.getResourceAsStream("/getTagsResponse.json")
    val getPaymentTagsJsonContent = Source.fromInputStream(getPaymentTagsStream, "UTF-8").getLines.mkString
    val getPaymentTagsBodyResponse = HttpEntity(MediaTypes.`application/json`, getPaymentTagsJsonContent.getBytes())
    mockResponse.entity returns getPaymentTagsBodyResponse

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPaymentTagsActor")

    "GetPaymentTags should" >> {
      "return a List of Tag objects" in {
        val fut: Future[List[Any]] = ask(tagActor, GetPaymentTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[List[Any]]
        val getPaymentTagsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagResult[Tag]](TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"),
          Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option("tagDefinitionName")),
          TagResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option(ObjectType.ACCOUNT), Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"),
            Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option("tagDefinitionName2")))
        getPaymentTagsResponse mustEqual expected
      }
    }
  }

  // Test Get Payment Tags Failure Response
  def getPaymentTagsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagActor = system.actorOf(Props(new TagActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPaymentTagsFailureActor")

    "GetPaymentTags should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagActor, GetPaymentTags(UUID.randomUUID(), any[String], any[Boolean])).mapTo[Any]
        val getPaymentTagsResponse = Await.result(fut, timeout.duration)
        getPaymentTagsResponse mustEqual expectedErrorMessage
      }
    }
  }

  getTagsSuccessResponseTest()
  getTagsFailureResponseTest()
  searchTagsSuccessResponseTest()
  searchTagsFailureResponseTest()
  getAllAccountTagsSuccessResponseTest()
  getAllAccountTagsFailureResponseTest()
  createAccountTagsSuccessResponseTest()
  createAccountTagsOtherResponseTest()
  createAccountTagsFailureResponseTest()
  deleteAccountTagsSuccessResponseTest()
  deleteAccountTagsOtherResponseTest()
  deleteAccountTagsFailureResponseTest()
  getAccountTagsSuccessResponseTest()
  getAccountTagsFailureResponseTest()
  createBundleTagsSuccessResponseTest()
  createBundleTagsOtherResponseTest()
  createBundleTagsFailureResponseTest()
  deleteBundleTagsSuccessResponseTest()
  deleteBundleTagsOtherResponseTest()
  deleteBundleTagsFailureResponseTest()
  getBundleTagsSuccessResponseTest()
  getBundleTagsFailureResponseTest()
  createSubscriptionTagsSuccessResponseTest()
  createSubscriptionTagsOtherResponseTest()
  createSubscriptionTagsFailureResponseTest()
  deleteSubscriptionTagsSuccessResponseTest()
  deleteSubscriptionTagsOtherResponseTest()
  deleteSubscriptionTagsFailureResponseTest()
  getSubscriptionTagsSuccessResponseTest()
  getSubscriptionTagsFailureResponseTest()
  createInvoiceTagsSuccessResponseTest()
  createInvoiceTagsOtherResponseTest()
  createInvoiceTagsFailureResponseTest()
  deleteInvoiceTagsSuccessResponseTest()
  deleteInvoiceTagsOtherResponseTest()
  deleteInvoiceTagsFailureResponseTest()
  getInvoiceTagsSuccessResponseTest()
  getInvoiceTagsFailureResponseTest()
  createPaymentTagsSuccessResponseTest()
  createPaymentTagsOtherResponseTest()
  createPaymentTagsFailureResponseTest()
  deletePaymentTagsSuccessResponseTest()
  deletePaymentTagsOtherResponseTest()
  deletePaymentTagsFailureResponseTest()
  getPaymentTagsSuccessResponseTest()
  getPaymentTagsFailureResponseTest()
}