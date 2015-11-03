package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model.BillingActionPolicy._
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
 * Created by jgomez on 30/10/2015.
 */
object BundleActor {
  case class GetBundles(offset: Long, limit: Long, auditMode: String)
  case class GetBundleByExternalKey(externalKey: String)
  case class GetBundleById(bundleId: UUID)
  case class SearchBundles(searchKey: String, offset: Long, limit: Long, auditMode: String)
  case class GetAccountBundles(accountId: UUID, externalKey: String)
  case class TransferBundleToAccount(bundle: Bundle, bundleId: UUID, billingPolicy: BillingActionPolicy)
}

case class BundleActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import BundleActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetBundles(offset, limit, auditLevel) => {
      getBundles(sender, offset, limit, auditLevel)
      context.stop(self)
    }
      
    case GetBundleByExternalKey(externalKey) => {
      getBundleByExternalKey(sender, externalKey)
      context.stop(self)
    }

    case GetBundleById(bundleId) => {
      getBundleById(sender, bundleId)
      context.stop(self)
    }

    case SearchBundles(searchKey, offset, limit, auditLevel) => {
      searchBundles(sender, searchKey, offset, limit, auditLevel)
      context.stop(self)
    }

    case GetAccountBundles(accountId, externalKey) => {
      getAccountBundles(sender, accountId, externalKey)
      context.stop(self)
    }

    case TransferBundleToAccount(bundle, bundleId, billingPolicy) => {
      transferBundleToAccount(sender, bundle, bundleId, billingPolicy)
      context.stop(self)
    }
  }

  def getBundles(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Bundles")

    import BundleJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[BundleResult[Bundle]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/bundles/pagination?offset=$offset&limit=$limit&audit="+auditLevel) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getBundleById(originalSender: ActorRef, bundleId: UUID) = {
    log.info("Requesting Bundle with ID: {}", bundleId.toString)

    import BundleJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[BundleResult[Bundle]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/bundles/$bundleId") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val bundle = new Bundle(response.accountId, response.bundleId, response.externalKey, response.subscriptions, response.timeline)
        originalSender ! bundle
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getBundleByExternalKey(originalSender: ActorRef, externalKey: String) = {
    log.info("Requesting Bundle with externalKey: {}", externalKey)

    import BundleJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[BundleResult[Bundle]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/bundles?externalKey=$externalKey") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val bundle = new Bundle(response.accountId, response.bundleId, response.externalKey, response.subscriptions, response.timeline) 
        originalSender ! bundle
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def searchBundles(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Searching All Bundles with searchKey=" + searchKey)

    import BundleJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[BundleResult[Bundle]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/bundles/search/$searchKey?offset=$offset&limit=$limit&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getAccountBundles(originalSender: ActorRef, accountId: UUID, externalKey: String) = {
    log.info("Requesting Account Bundles")

    import BundleJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive ~> unmarshal[List[BundleResult[Bundle]]]

    var suffixUrl = ""
    if (!externalKey.equalsIgnoreCase("")) {
      suffixUrl = "?externalKey=" + externalKey
    }

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/bundles" + suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def transferBundleToAccount(originalSender: ActorRef, bundle: Bundle, bundleId: UUID, billingActionPolicy: BillingActionPolicy) = {
    log.info("Transferring Bundle to new Account...")

    import BundleJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Put(killBillUrl+s"/bundles/$bundleId?billingPolicy=$billingActionPolicy", bundle) ~> addHeaders(headers)
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