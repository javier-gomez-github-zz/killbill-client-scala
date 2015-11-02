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
 * Created by jgomez on 30/10/2015.
 */
object TagDefinitionActor {
  case class GetTagDefinitions(auditMode: String)
  case class GetTagDefinition(tagDefinitionId: UUID, auditLevel: String)
  case class CreateTagDefinition(tagDefinition: TagDefinition)
  case class DeleteTagDefinition(tagDefinitionId: UUID)
}

class TagDefinitionActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import TagDefinitionActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)

  def receive = {
    case GetTagDefinitions(auditLevel) => {
      getTagDefinitions(sender, auditLevel)
      context.stop(self)
    }
      
    case GetTagDefinition(tagDefinitionId, auditLevel) => {
        getTagDefinition(sender, tagDefinitionId, auditLevel)
      context.stop(self)
    }

    case CreateTagDefinition(tagDefinition) =>
      createTagDefinition(sender, tagDefinition)
      context.stop(self)

    case DeleteTagDefinition(tagDefinitionId) => {
      deleteTagDefinition(sender, tagDefinitionId)
      context.stop(self)
    }
  }

  def getTagDefinitions(originalSender: ActorRef, auditLevel: String) = {
    log.info("Requesting All Tag Definitions")

    import SprayJsonSupport._
    import TagDefinitionJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[List[TagDefinitionResult[TagDefinition]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tagDefinitions?audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getTagDefinition(originalSender: ActorRef, tagDefinitionId: UUID, auditLevel: String) = {
    log.info("Requesting Tag Definition with ID: {}", tagDefinitionId.toString)

    import SprayJsonSupport._
    import TagDefinitionJsonProtocol._

    val pipeline = sendReceive ~> unmarshal[TagDefinitionResult[TagDefinition]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/tagDefinitions/$tagDefinitionId?audit=$auditLevel") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val tagDefinition = new TagDefinition(response.id, response.isControlTag, response.name, response.description, response.applicableObjectTypes)
        originalSender ! tagDefinition
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def createTagDefinition(originalSender: ActorRef, tagDefinition: TagDefinition) = {
    log.info("Creating new Tag Definition...")

    import SprayJsonSupport._
    import TagDefinitionJsonProtocol._

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/tagDefinitions", tagDefinition) ~> addHeaders(headers)
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

  def deleteTagDefinition(originalSender: ActorRef, tagDefinitionId: UUID) = {
    log.info("Deleting Tag Definition with ID: " + tagDefinitionId.toString)

    val pipeline = sendReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/tagDefinitions/$tagDefinitionId") ~> addHeaders(headers)
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
}