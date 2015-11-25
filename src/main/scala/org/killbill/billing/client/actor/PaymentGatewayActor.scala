package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 09/11/2015.
  */
object PaymentGatewayActor {
  case class BuildFormDescriptor(fields: HostedPaymentPageFields, kbAccountId: UUID,
                                 kbPaymentMethodId: UUID, pluginProperties: Map[String, String])
  case class BuildComboFormDescriptor(comboHostedPaymentPage: ComboHostedPaymentPage, controlPluginNames: List[String] = List[String](),
                                      pluginProperties: Map[String, String])
  case class ProcessNotification(notification: String, pluginName: String, pluginProperties: Map[String, String])
}

case class PaymentGatewayActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import PaymentGatewayActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)
  def sendAndReceive = sendReceive

  def receive = {
    case BuildFormDescriptor(fields, kbAccountId, kbPaymentMethodId, pluginProperties) =>
      buildFormDescriptor(sender, fields, kbAccountId, kbPaymentMethodId, pluginProperties)
      context.stop(self)

    case BuildComboFormDescriptor(comboHostedPaymentPage, controlPluginNames, pluginProperties) =>
      buildComboFormDescriptor(sender, comboHostedPaymentPage, controlPluginNames, pluginProperties)
      context.stop(self)

    case ProcessNotification(notification, pluginName, pluginProperties) =>
      processNotification(sender, notification, pluginName, pluginProperties)
      context.stop(self)

  }

  def processNotification(originalSender: ActorRef, notification: String, pluginName: String, pluginProperties: Map[String, String]) = {
    log.info("Processing Notification...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/paymentGateways/notification/$pluginName?pluginProperty=$pluginProperties", notification) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("200")) {
          originalSender ! response.entity.asString
        }
        else {
          originalSender ! response.entity
        }
      }
      case Failure(error) => {
        originalSender ! error.getMessage()
      }
    }
  }

  def buildComboFormDescriptor(originalSender: ActorRef, comboHostedPaymentPage: ComboHostedPaymentPage,
                               controlPluginNames: List[String], pluginProperties: Map[String, String]) = {
    log.info("Building Combo Form Descriptor...")

    import ComboHostedPaymentPageJsonProtocol._
    import HostedPaymentPageFormDescriptorJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/paymentGateways/hosted/form/?controlPluginName=$controlPluginNames&pluginProperty=$pluginProperties", comboHostedPaymentPage) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("200")) {
          originalSender ! response.entity.asString
        }
        else {
          originalSender ! response ~> unmarshal[HostedPaymentPageFormDescriptorResult[HostedPaymentPageFormDescriptor]]
        }
      }
      case Failure(error) => {
        originalSender ! error.getMessage()
      }
    }
  }

  def buildFormDescriptor(originalSender: ActorRef, fields: HostedPaymentPageFields, kbAccountId: UUID,
                          kbPaymentMethodId: UUID, pluginProperties: Map[String, String]) = {
    log.info("Building Form Descriptor...")

    import HostedPaymentPageFieldsJsonProtocol._
    import HostedPaymentPageFormDescriptorJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/paymentGateways/hosted/form/$kbAccountId?paymentMethodId=$kbPaymentMethodId&pluginProperty=$pluginProperties", fields) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("200")) {
          originalSender ! response.entity.asString
        }
        else {
          originalSender ! response ~> unmarshal[HostedPaymentPageFormDescriptorResult[HostedPaymentPageFormDescriptor]]
        }
      }
      case Failure(error) => {
        originalSender ! error.getMessage()
      }
    }
  }
}