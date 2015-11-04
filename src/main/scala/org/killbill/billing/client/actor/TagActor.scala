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
  * Created by jgomez on 04/11/2015.
  */
object TagActor {
  case class GetTags(offset: Long, limit: Long, auditMode: String)
  case class SearchTags(searchKey: String, offset: Long, limit: Long, auditMode: String)
  case class GetAllAccountTags(accountId: UUID, objectType: String, auditMode: String, includedDeleted: Boolean)
  case class GetAccountTags(accountId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreateAccountTag(accountId: UUID, tagDefinitionId: UUID)
  case class DeleteAccountTag(accountId: UUID, tagDefinitionId: UUID)
  case class GetBundleTags(bundleId: UUID, auditMode: String, includedDeleted: Boolean)
  case class CreateBundleTag(bundleId: UUID, tagDefinitionId: UUID)
  case class DeleteBundleTag(bundleId: UUID, tagDefinitionId: UUID)
}

case class TagActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import TagActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetTags(offset, limit, auditLevel) =>
      getTags(sender, offset, limit, auditLevel)
      context.stop(self)

    case SearchTags(searchKey, offset, limit, auditLevel) =>
      searchTags(sender, searchKey, offset, limit, auditLevel)
      context.stop(self)

    case GetAllAccountTags(accountId, objectType, auditMode, includedDeleted) =>
      getAllAccountTags(sender, accountId, objectType, auditMode, includedDeleted)
      context.stop(self)

    case GetAccountTags(accountId, auditMode, includedDeleted) =>
      getAccountTags(sender, accountId, auditMode, includedDeleted)
      context.stop(self)

    case CreateAccountTag(accountId, tagDefinitionId) =>
      createAccountTag(sender, accountId, tagDefinitionId)
      context.stop(self)

    case DeleteAccountTag(accountId, tagDefinitionId) =>
      deleteAccountTag(sender, accountId, tagDefinitionId)
      context.stop(self)

    case GetBundleTags(bundleId, auditMode, includedDeleted) =>
      getBundleTags(sender, bundleId, auditMode, includedDeleted)
      context.stop(self)

    case CreateBundleTag(bundleId, tagDefinitionId) =>
      createBundleTag(sender, bundleId, tagDefinitionId)
      context.stop(self)

    case DeleteBundleTag(bundleId, tagDefinitionId) =>
      deleteBundleTag(sender, bundleId, tagDefinitionId)
      context.stop(self)
  }

  def deleteBundleTag(originalSender: ActorRef, bundleId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Bundle Tag: " + tagDefinitionId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/bundles/$bundleId/tags?tagList=$tagDefinitionId") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("204")) {
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

  def createBundleTag(originalSender: ActorRef, bundleId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Bundle Tag for Bundle: " + bundleId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/bundles/$bundleId/tags?tagList=$tagDefinitionId") ~> addHeaders(headers)
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

  def getBundleTags(originalSender: ActorRef, bundleId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Bundle Tags for Bundle: " + bundleId.toString)

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/bundles/$bundleId/tags?audit=$auditLevel&includedDeleted=$includedDeleted") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def deleteAccountTag(originalSender: ActorRef, accountId: UUID, tagDefinitionId: UUID) = {
    log.info("Deleting Account Tag: " + tagDefinitionId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/accounts/$accountId/tags?tagList=$tagDefinitionId") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) => {
        if (!response.status.toString().contains("204")) {
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

  def createAccountTag(originalSender: ActorRef, accountId: UUID, tagDefinitionId: UUID) = {
    log.info("Creating Account Tag for Account: " + accountId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/accounts/$accountId/tags?tagList=$tagDefinitionId") ~> addHeaders(headers)
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

  def getAccountTags(originalSender: ActorRef, accountId: UUID, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting Tags for Account: " + accountId.toString)

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/tags?audit=$auditLevel&includedDeleted=$includedDeleted") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getAllAccountTags(originalSender: ActorRef, accountId: UUID, objectType: String, auditLevel: String, includedDeleted: Boolean) = {
    log.info("Requesting All Tags for Account: " + accountId.toString)

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[TagResult[Tag]]]

    var suffixUrl = ""
    if (!objectType.equalsIgnoreCase("")) {
      suffixUrl = "&objectType=" + objectType
    }

    val responseFuture = pipeline {
      Get(killBillUrl+s"/accounts/$accountId/allTags?audit=$auditLevel&includedDeleted=$includedDeleted" + suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def searchTags(originalSender: ActorRef, searchKey: String, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Searching All Tags with searchKey=" + searchKey)

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tags/search/$searchKey?offset=$offset&limit=$limit&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getTags(originalSender: ActorRef, offset: Long, limit: Long, auditLevel: String) = {
    log.info("Requesting All Tags")

    import SprayJsonSupport._
    import TagJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[TagResult[Tag]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tags/pagination?offset=$offset&limit=$limit&audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}
