package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model.ObjectType.ObjectType
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
class TagDefinitionActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import TagDefinitionActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Tag Definitions Success Response
  def getTagDefinitionsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getTagDefinitionsStream: InputStream = getClass.getResourceAsStream("/getTagDefinitionsResponse.json")
    val getTagDefinitionsJsonContent = Source.fromInputStream(getTagDefinitionsStream, "UTF-8").getLines.mkString
    val getTagDefinitionsBodyResponse = HttpEntity(MediaTypes.`application/json`, getTagDefinitionsJsonContent.getBytes())
    mockResponse.entity returns getTagDefinitionsBodyResponse

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetTagDefinitionsActor")

    "GetTagDefinitions should" >> {
      "return a List of TagDefinition objects" in {
        val fut: Future[List[Any]] = ask(tagDefinitionActor, GetTagDefinitions(any[String])).mapTo[List[Any]]
        val getTagDefinitionsResponse = Await.result(fut, timeout.duration)

        val expected = List[TagDefinitionResult[TagDefinition]](
          TagDefinitionResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("name"),
            Option("description"), Option(List[ObjectType]())),
          TagDefinitionResult(Option("b17298d2-37fc-4701-8b9d-92ab1d15f012"), Option(false), Option("name2"),
            Option("description2"), Option(List[ObjectType]())))
        getTagDefinitionsResponse mustEqual expected
      }
    }
  }

  // Test Get Tag Definitions Failure Response
  def getTagDefinitionsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetTagDefinitionsFailureActor")

    "GetTagDefinitions should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(tagDefinitionActor, GetTagDefinitions(any[String])).mapTo[Any]
        val getTagDefinitionsResponse = Await.result(fut, timeout.duration)
        getTagDefinitionsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Get Tag Definition Success Response
  def getTagDefinitionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getTagDefinitionstream: InputStream = getClass.getResourceAsStream("/getTagDefinitionResponse.json")
    val getTagDefinitionJsonContent = Source.fromInputStream(getTagDefinitionstream, "UTF-8").getLines.mkString
    val getTagDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, getTagDefinitionJsonContent.getBytes())
    mockResponse.entity returns getTagDefinitionBodyResponse

    val TagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetTagDefinitionActor")

    "GetTagDefinition should" >> {
      "return TagDefinition object" in {
        val fut: Future[Any] = ask(TagDefinitionActor, GetTagDefinition(UUID.randomUUID(), any[String])).mapTo[Any]
        val getTagDefinitionResponse = Await.result(fut, timeout.duration)
        val expected = TagDefinition(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("name"),
          Option("description"), Option(List[ObjectType]()))
        getTagDefinitionResponse mustEqual expected
      }
    }
  }

  // Test Get Tag Definitions by Id Failure Response
  def getTagDefinitionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val TagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetTagDefinitionFailureActor")

    "GetTagDefinition should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(TagDefinitionActor, GetTagDefinition(UUID.randomUUID(), any[String])).mapTo[Any]
        val getTagDefinitionResponse = Await.result(fut, timeout.duration)
        getTagDefinitionResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Create Tag Definition Success Response
  def createTagDefinitionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201 Created"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val createTagDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201 Created")
    mockResponse.entity returns createTagDefinitionBodyResponse

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateTagDefinitionActor")

    "CreateTagDefinition should" >> {
      "return String response" in {
        val tagDefinition: TagDefinition = TagDefinition(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("name"),
          Option("description"), Option(List[ObjectType]()))
        val fut: Future[String] = ask(tagDefinitionActor, CreateTagDefinition(tagDefinition)).mapTo[String]
        val createTagDefinitionResponse = Await.result(fut, timeout.duration)
        val expected = "201 Created"
        createTagDefinitionResponse mustEqual expected
      }
    }
  }

  // Test Create Tag Definition Other Response
  def createTagDefinitionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val createTagDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns createTagDefinitionBodyResponse

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "CreateTagDefinitionOtherActor")

    "CreateTagDefinition should" >> {
      "return a different status" in {
        val tagDefinition: TagDefinition = TagDefinition(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("name"),
          Option("description"), Option(List[ObjectType]()))
        val fut: Future[String] = ask(tagDefinitionActor, CreateTagDefinition(tagDefinition)).mapTo[String]
        val createTagDefinitionResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        createTagDefinitionResponse mustEqual expected
      }
    }
  }

  // Test Create Tag Definition Failure Response
  def createTagDefinitionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "CreateTagDefinitionFailureActor")

    "CreateTagDefinition should" >> {
      "throw an Exception" in {
        val tagDefinition: TagDefinition = TagDefinition(Option("b17298d2-37fc-4701-8b9d-92ab1d15f01c"), Option(false), Option("name"),
          Option("description"), Option(List[ObjectType]()))
        val fut: Future[String] = ask(tagDefinitionActor, CreateTagDefinition(tagDefinition)).mapTo[String]
        val createTagDefinitionResponse = Await.result(fut, timeout.duration)
        createTagDefinitionResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Delete Tag Definition Success Response
  def deleteTagDefinitionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "204"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val deleteTagDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns deleteTagDefinitionBodyResponse

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteTagDefinitionActor")

    "DeleteTagDefinition should" >> {
      "return String response" in {
        val fut: Future[String] = ask(tagDefinitionActor, DeleteTagDefinition(UUID.randomUUID())).mapTo[String]
        val deleteTagDefinitionResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        deleteTagDefinitionResponse mustEqual expected
      }
    }
  }

  // Test Delete Tag Definition Other Response
  def deleteTagDefinitionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val deleteTagDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns deleteTagDefinitionBodyResponse

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "DeleteTagDefinitionOtherResponseActor")

    "DeleteTagDefinition should" >> {
      "return a different status" in {
        val fut: Future[String] = ask(tagDefinitionActor, DeleteTagDefinition(UUID.randomUUID())).mapTo[String]
        val deleteTagDefinitionResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        deleteTagDefinitionResponse mustEqual expected
      }
    }
  }

  // Test Delete Tag Definition Failure Response
  def deleteTagDefinitionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "DeleteTagDefinitionFailureActor")

    "DeleteTagDefinition should" >> {
      "throw an Exception" in {
        val fut: Future[String] = ask(tagDefinitionActor, DeleteTagDefinition(UUID.randomUUID())).mapTo[String]
        val deleteTagDefinitionResponse = Await.result(fut, timeout.duration)
        deleteTagDefinitionResponse mustEqual expectedErrorMessage
      }
    }
  }

  getTagDefinitionsSuccessResponseTest()
  getTagDefinitionsFailureResponseTest()
  getTagDefinitionSuccessResponseTest()
  getTagDefinitionFailureResponseTest()
  createTagDefinitionSuccessResponseTest()
  createTagDefinitionOtherResponseTest()
  createTagDefinitionFailureResponseTest()
  deleteTagDefinitionSuccessResponseTest()
  deleteTagDefinitionOtherResponseTest()
  deleteTagDefinitionFailureResponseTest()
}