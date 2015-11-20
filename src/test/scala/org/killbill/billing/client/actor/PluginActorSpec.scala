package org.killbill.billing.client.actor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by jgomez on 19/11/2015.
  */
class PluginActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import PluginActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  def pluginCommonActionsSuccessAndOtherResponse(statusCode: String, actorName: String, actionName: String) = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns statusCode
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val pluginBodyResponse = HttpEntity(MediaTypes.`application/json`, statusCode)
    mockResponse.entity returns pluginBodyResponse

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = actorName + "Actor")

    actionName + " should" >> {
      "return a Response" in {
        actionName match {
          case "PluginGet" => {
            val fut: Future[Any] = ask(pluginActor, PluginGet("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual mockResponse
          }
          case "PluginHead" => {
            val fut: Future[Any] = ask(pluginActor, PluginHead("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual mockResponse
          }
          case "PluginOptions" => {
            val fut: Future[Any] = ask(pluginActor, PluginOptions("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual mockResponse
          }
          case "PluginDelete" => {
            val fut: Future[Any] = ask(pluginActor, PluginDelete("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual mockResponse
          }
        }
      }
    }
  }

  def pluginCommonActionsFailureResponse(actorName: String, actionName: String) = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = actorName + "FailureActor")

    actionName + " should" >> {
      "throw an Exception" in {
        actionName match {
          case "PluginGet" => {
            val fut: Future[Any] = ask(pluginActor, PluginGet("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual expectedErrorMessage
          }
          case "PluginHead" => {
            val fut: Future[Any] = ask(pluginActor, PluginHead("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual expectedErrorMessage
          }
          case "PluginOptions" => {
            val fut: Future[Any] = ask(pluginActor, PluginOptions("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual expectedErrorMessage
          }
          case "PluginDelete" => {
            val fut: Future[Any] = ask(pluginActor, PluginDelete("anyUri")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual expectedErrorMessage
          }
          case "PluginPost" => {
            val fut: Future[Any] = ask(pluginActor, PluginPost("anyUri", "anyBody")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual expectedErrorMessage
          }
          case "PluginPut" => {
            val fut: Future[Any] = ask(pluginActor, PluginPut("anyUri", "anyBody")).mapTo[Any]
            val pluginResponse = Await.result(fut, timeout.duration)
            pluginResponse mustEqual expectedErrorMessage
          }
        }
      }
    }
  }

  def pluginPutPostCommonActions(statusCode: String, actorName: String, actionName: String) = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns statusCode
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    val pluginResponseBody = HttpEntity(MediaTypes.`application/json`, statusCode)
    mockResponse.entity returns pluginResponseBody

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = actorName + "Actor")

    actionName + " should" >> {
      "return a " + statusCode + " status" in {
        actionName match {
          case "PluginPost" => {
            val fut: Future[String] = ask(pluginActor, PluginPost("anyUri", "anyBody")).mapTo[String]
            val pluginResponse = Await.result(fut, timeout.duration)
            val expected = statusCode
            pluginResponse mustEqual expected
          }
          case "PluginPut" => {
            val fut: Future[String] = ask(pluginActor, PluginPut("anyUri", "anyBody")).mapTo[String]
            val pluginResponse = Await.result(fut, timeout.duration)
            val expected = statusCode
            pluginResponse mustEqual expected
          }
        }
      }
    }
  }

  // Test PluginGet Success Response
  def pluginGetSuccessResponseTest() = {
    pluginCommonActionsSuccessAndOtherResponse("201", "PluginGet", "PluginGet")
  }

  // Test PluginGet Failure Response
  def pluginGetFailureResponseTest() = {
    pluginCommonActionsFailureResponse("PluginGetFailure", "PluginGet")
  }

  // Test PluginHead Success Response
  def pluginHeadSuccessResponseTest() = {
    pluginCommonActionsSuccessAndOtherResponse("201", "PluginHead", "PluginHead")
  }

  // Test PluginHead Failure Response
  def pluginHeadFailureResponseTest() = {
    pluginCommonActionsFailureResponse("PluginHeadFailure", "PluginHead")
  }

  // Test PluginOptions Success Response
  def pluginOptionsSuccessResponseTest() = {
    pluginCommonActionsSuccessAndOtherResponse("201", "PluginOptions", "PluginOptions")
  }

  // Test PluginOptions Failure Response
  def pluginOptionsFailureResponseTest() = {
    pluginCommonActionsFailureResponse("PluginOptionsFailure", "PluginOptions")
  }

  // Test PluginDelete Success Response
  def pluginDeleteSuccessResponseTest() = {
    pluginCommonActionsSuccessAndOtherResponse("201", "PluginDelete", "PluginDelete")
  }

  // Test PluginDelete Failure Response
  def pluginDeleteFailureResponseTest() = {
    pluginCommonActionsFailureResponse("PluginDeleteFailure", "PluginDelete")
  }

  // Test PluginPost Success Response
  def pluginPostSuccessResponseTest() = {
    pluginPutPostCommonActions("201", "PluginPost", "PluginPost")
  }

  // Test PluginPost Other Response Response
  def pluginPostOtherResponseTest() = {
    pluginPutPostCommonActions("200", "PluginPostOtherResponse", "PluginPost")
  }

  // Test PluginPost Failure Response
  def pluginPostFailureResponseTest() = {
    pluginCommonActionsFailureResponse("PluginPostFailure", "PluginPost")
  }

  // Test PluginPost Success Response
  def pluginPutSuccessResponseTest() = {
    pluginPutPostCommonActions("201", "PluginPut", "PluginPut")
  }

  // Test PluginPost Other Response Response
  def pluginPutOtherResponseTest() = {
    pluginPutPostCommonActions("200", "PluginPutOtherResponse", "PluginPut")
  }

  // Test PluginPut Failure Response
  def pluginPutFailureResponseTest() = {
    pluginCommonActionsFailureResponse("PluginPutFailure", "PluginPut")
  }

  pluginGetSuccessResponseTest()
  pluginGetFailureResponseTest()
  pluginHeadSuccessResponseTest()
  pluginHeadFailureResponseTest()
  pluginOptionsSuccessResponseTest()
  pluginOptionsFailureResponseTest()
  pluginDeleteSuccessResponseTest()
  pluginDeleteFailureResponseTest()
  pluginPutSuccessResponseTest()
  pluginPutOtherResponseTest()
  pluginPutFailureResponseTest()
  pluginPostSuccessResponseTest()
  pluginPostOtherResponseTest()
  pluginPostFailureResponseTest()
}