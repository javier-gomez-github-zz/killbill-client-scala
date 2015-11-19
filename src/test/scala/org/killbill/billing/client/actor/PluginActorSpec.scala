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

  // Test PluginGet Success Response
  def pluginGetSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val pluginGetBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns pluginGetBodyResponse

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "PluginGetActor")

    "PluginGet should" >> {
      "return a Response" in {
        val fut: Future[Any] = ask(pluginActor, PluginGet("anyUri")).mapTo[Any]
        val pluginGetResponse = Await.result(fut, timeout.duration)
        pluginGetResponse mustEqual mockResponse
      }
    }
  }

  // Test PluginGet Other Response
  def pluginGetOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val pluginGetBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns pluginGetBodyResponse

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "PluginGetOtherActor")

    "PluginGet should" >> {
      "return a Response" in {
        val fut: Future[Any] = ask(pluginActor, PluginGet("anyUri")).mapTo[Any]
        val pluginGetResponse = Await.result(fut, timeout.duration)
        pluginGetResponse mustEqual mockResponse
      }
    }
  }

  // Test PluginGet Failure Response
  def pluginGetFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "PluginGetFailureActor")

    "PluginGet should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(pluginActor, PluginGet("anyUri")).mapTo[Any]
        val pluginGetResponse = Await.result(fut, timeout.duration)
        pluginGetResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test PluginHead Success Response
  def pluginHeadSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val pluginHeadBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns pluginHeadBodyResponse

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "PluginHeadActor")

    "PluginHead should" >> {
      "return a Response" in {
        val fut: Future[Any] = ask(pluginActor, PluginHead("anyUri")).mapTo[Any]
        val pluginHeadResponse = Await.result(fut, timeout.duration)
        pluginHeadResponse mustEqual mockResponse
      }
    }
  }

  // Test PluginHead Other Response
  def pluginHeadOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val pluginHeadBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns pluginHeadBodyResponse

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "PluginHeadOtherActor")

    "PluginHead should" >> {
      "return a Response" in {
        val fut: Future[Any] = ask(pluginActor, PluginHead("anyUri")).mapTo[Any]
        val pluginHeadResponse = Await.result(fut, timeout.duration)
        pluginHeadResponse mustEqual mockResponse
      }
    }
  }

  // Test PluginHead Failure Response
  def pluginHeadFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val pluginActor = system.actorOf(Props(new PluginActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "PluginHeadFailureActor")

    "PluginHead should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(pluginActor, PluginHead("anyUri")).mapTo[Any]
        val pluginHeadResponse = Await.result(fut, timeout.duration)
        pluginHeadResponse mustEqual expectedErrorMessage
      }
    }
  }

  pluginGetSuccessResponseTest()
  pluginGetOtherResponseTest()
  pluginGetFailureResponseTest()
  pluginHeadSuccessResponseTest()
  pluginHeadOtherResponseTest()
  pluginHeadFailureResponseTest()
}