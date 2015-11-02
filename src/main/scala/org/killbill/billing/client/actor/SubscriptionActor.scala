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
 * Created by jgomez on 02/11/2015.
 */
object SubscriptionActor {
  case class CreateSubscription(subscription: Subscription)
  case class GetSubscriptionById(subscriptionId: UUID)
  case class UpdateSubscription(subscription: Subscription, subscriptionId: UUID)
  case class CancelSubscription(subscriptionId: UUID)
  case class UnCancelSubscription(subscriptionId: UUID)
}

case class SubscriptionActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {
  
  import SubscriptionActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case CreateSubscription(subscription) =>
      createSubscription(sender, subscription)
      context.stop(self)

    case GetSubscriptionById(subscriptionId) =>
      getSubscriptionById(sender, subscriptionId)
      context.stop(self)

    case UpdateSubscription(subscription, subscriptionId) =>
      updateSubscription(sender, subscription, subscriptionId)
      context.stop(self)

    case CancelSubscription(subscriptionId) =>
      cancelSubscription(sender, subscriptionId)
      context.stop(self)

    case UnCancelSubscription(subscriptionId) =>
      unCancelSubscription(sender, subscriptionId)
      context.stop(self)
  }

  def createSubscription(originalSender: ActorRef, subscription: Subscription) = {
    log.info("Creating new Subscription...")

    import SubscriptionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/subscriptions", subscription) ~> addHeaders(headers)
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

  def getSubscriptionById(originalSender: ActorRef, subscriptionId: UUID) = {
    log.info("Requesting Subscription with ID: {}", subscriptionId.toString)

    import SprayJsonSupport._
    import SubscriptionJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[SubscriptionResult[Subscription]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/subscriptions/$subscriptionId") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val subscription = new Subscription(response.accountId, response.bundleId, response.subscriptionId, response.externalKey, response.startDate, response.productName,
          response.productCategory, response.billingPeriod, response.phaseType, response.priceList, response.state, response.sourceType, response.cancelledDate,
          response.chargedThroughDate, response.billingStartDate, response.billingEndDate, response.events, response.priceOverrides)
        originalSender ! subscription
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def updateSubscription(originalSender: ActorRef, subscription: Subscription, subscriptionId: UUID) = {
    log.info("Updating Subscription..." + subscription.externalKey)

    import SubscriptionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/subscriptions/$subscriptionId", subscription) ~> addHeaders(headers)
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

  def cancelSubscription(originalSender: ActorRef, subscriptionId: UUID) = {
    log.info("Cancelling Subscription with ID: " + subscriptionId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/subscriptions/$subscriptionId") ~> addHeaders(headers)
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

  def unCancelSubscription(originalSender: ActorRef, subscriptionId: UUID) = {
    log.info("Uncancelling Subscription: " + subscriptionId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/subscriptions/$subscriptionId/uncancel") ~> addHeaders(headers)
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
}