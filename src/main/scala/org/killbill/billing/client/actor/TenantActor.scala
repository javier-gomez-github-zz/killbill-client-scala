package org.killbill.billing.client.actor

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model.{Tenant, TenantJsonProtocol}
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 11/11/2015.
  */
object TenantActor {
  case class CreateTenant(tenant: Tenant)
  case class RegisterCallbackNotificationForTenant(callback: String)
  case class GetCallbackNotificationForTenant()
  case class UnRegisterCallbackNotificationForTenant()
  case class RegisterPluginConfigurationForTenant(pluginName: String, pluginConfig: String)
  case class GetPluginConfigurationForTenant(pluginName: String)
  case class UnRegisterPluginConfigurationForTenant(pluginName: String)
}

case class TenantActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import TenantActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case CreateTenant(tenant) =>
      createTenant(sender, tenant)

    case RegisterCallbackNotificationForTenant(callback) =>
      registerCallbackNotificationForTenant(sender, callback)

    case GetCallbackNotificationForTenant() =>
      getCallbackNotificationForTenant(sender)

    case UnRegisterCallbackNotificationForTenant() =>
      unRegisterCallbackNotificationForTenant(sender)

    case RegisterPluginConfigurationForTenant(pluginName, pluginConfig) =>
      registerPluginConfigurationForTenant(sender, pluginName, pluginConfig)

    case GetPluginConfigurationForTenant(pluginName) =>
      getPluginConfigurationForTenant(sender, pluginName)

    case UnRegisterPluginConfigurationForTenant(pluginName) =>
      unRegisterPluginConfigurationForTenant(sender, pluginName)
  }

  def unRegisterPluginConfigurationForTenant(originalSender: ActorRef, pluginName: String) = {
    log.info("Unregistering Plugin Configuration for Tenant...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/tenants/uploadPluginConfig/$pluginName") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.status.toString
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getPluginConfigurationForTenant(originalSender: ActorRef, pluginName: String) = {
    log.info("Getting Plugin Configuration for Tenant...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tenants/uploadPluginConfig/$pluginName") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.entity.asString
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def registerPluginConfigurationForTenant(originalSender: ActorRef, pluginName: String, pluginConfig: String) = {
    log.info("Registering Plugin Configuration For Tenant...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/tenants/uploadPluginConfig/$pluginName", pluginConfig) ~> addHeaders(headers)
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

  def unRegisterCallbackNotificationForTenant(originalSender: ActorRef) = {
    log.info("Unregistering Callback Notification for Tenant...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/tenants/registerNotificationCallback") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.status.toString
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getCallbackNotificationForTenant(originalSender: ActorRef) = {
    log.info("Getting Callback Notification for Tenant...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tenants/registerNotificationCallback") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.entity.asString
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def registerCallbackNotificationForTenant(originalSender: ActorRef, callback: String) = {
    log.info("Registering Callback Notification For Tenant...")

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/tenants/registerNotificationCallback?cb=$callback") ~> addHeaders(headers)
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

  def createTenant(originalSender: ActorRef, tenant: Tenant) = {
    log.info("Creating new Tenant...")

    import SprayJsonSupport._
    import TenantJsonProtocol._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/tenants", tenant) ~> addHeaders(headers)
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
}