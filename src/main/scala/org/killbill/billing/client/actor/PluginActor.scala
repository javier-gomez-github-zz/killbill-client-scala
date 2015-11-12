package org.killbill.billing.client.actor

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import spray.client.pipelining._
import spray.http.HttpHeader

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 12/11/2015.
  */
object PluginActor {
  case class PluginGet(uri: String)
  case class PluginHead(uri: String)
  case class PluginPost(uri: String, body: String)
  case class PluginPut(uri: String, body: String)
  case class PluginDelete(uri: String)
  case class PluginOptions(uri: String)
}

case class PluginActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import PluginActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case PluginGet(uri) =>
      pluginGet(sender, uri)
      context.stop(self)

    case PluginHead(uri) =>
      pluginHead(sender, uri)
      context.stop(self)

    case PluginPost(uri, body) =>
      pluginPost(sender, uri, body)
      context.stop(self)

    case PluginPut(uri, body) =>
      pluginPut(sender, uri, body)
      context.stop(self)

    case PluginDelete(uri) =>
      pluginDelete(sender, uri)
      context.stop(self)

    case PluginOptions(uri) =>
      pluginOptions(sender, uri)
      context.stop(self)
  }

  def pluginPut(originalSender: ActorRef, uri: String, body: String) = {
    log.info("Plugin Put...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/plugins", body) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("200")) {
          originalSender ! response.entity.asString
        }
        else {
          originalSender ! response.status.toString()
        }
      }
      case Failure(error) => {
        originalSender ! error.getMessage()
      }
    }
  }

  def pluginPost(originalSender: ActorRef, uri: String, body: String) = {
    log.info("Plugin Post...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/plugins", body) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("201")) {
          originalSender ! response.entity.asString
        }
        else {
          originalSender ! response.status.toString()
        }
      }
      case Failure(error) => {
        originalSender ! error.getMessage()
      }
    }
  }

  def pluginGet(originalSender: ActorRef, uri: String) = {
    log.info("Plugin GET...")
    pluginActions(originalSender, uri, "Get")
  }

  def pluginHead(originalSender: ActorRef, uri: String) = {
    log.info("Plugin HEAD...")
    pluginActions(originalSender, uri, "Head")
  }

  def pluginOptions(originalSender: ActorRef, uri: String) = {
    log.info("Plugin OPTIONS...")
    pluginActions(originalSender, uri, "Options")
  }

  def pluginDelete(originalSender: ActorRef, uri: String) = {
    log.info("Plugin DELETE...")
    pluginActions(originalSender, uri, "Delete")
  }

  def pluginActions(originalSender: ActorRef, uri: String, action: String) = {
    val pipeline = sendReceive

    val responseFuture = pipeline {
      action match {
        case "Get" => Get(killBillUrl+s"/plugins/$uri") ~> addHeaders(headers)
        case "Head" => Head(killBillUrl+s"/plugins/$uri") ~> addHeaders(headers)
        case "Options" => Options(killBillUrl+s"/plugins/$uri") ~> addHeaders(headers)
        case "Delete" => Delete(killBillUrl+s"/plugins/$uri") ~> addHeaders(headers)
      }
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}