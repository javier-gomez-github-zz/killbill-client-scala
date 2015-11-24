package org.killbill.billing.client.actor

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeaders.Accept
import spray.http._
import spray.httpx.SprayJsonSupport

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 10/11/2015.
  */
object CatalogActor {
  case class GetSimpleCatalog()
  case class GetAvailableAddons(baseProductName: String)
  case class GetAvailableBasePlans()
  case class UploadXMLCatalog(xmlCatalog: String)
  case class GetXMLCatalog()
  case class GetJSONCatalog()
}

case class CatalogActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import CatalogActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)
  def sendAndReceive = sendReceive

  def receive = {
    case GetSimpleCatalog() =>
      getSimpleCatalog(sender)
      context.stop(self)

    case GetAvailableAddons(baseProductName) =>
      getAvailableAddons(sender, baseProductName)
      context.stop(self)

    case GetAvailableBasePlans() =>
      getAvailableBasePlans(sender)
      context.stop(self)

    case UploadXMLCatalog(xmlCatalog) =>
      uploadXMLCatalog(sender, xmlCatalog)
      context.stop(self)

    case GetXMLCatalog() =>
      getXMLCatalog(sender)
      context.stop(self)

    case GetJSONCatalog() =>
      getJSONCatalog(sender)
      context.stop(self)
  }

  def getJSONCatalog(originalSender: ActorRef) = {
    log.info("Getting JSON Catalog...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Get(killBillUrl+s"/catalog") ~> addHeaders(headers) ~> addHeader(Accept(MediaRange.apply(MediaTypes.`application/json`)))
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.entity.asString
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getXMLCatalog(originalSender: ActorRef) = {
    log.info("Getting XML Catalog...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Get(killBillUrl+s"/catalog") ~> addHeaders(headers) ~> addHeader(Accept(MediaRange.apply(MediaTypes.`application/xml`)))
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response.entity.asString
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def uploadXMLCatalog(originalSender: ActorRef, xmlCatalog: String) = {
    log.info("Uploading XML Catalog...")

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/catalog", HttpEntity(MediaTypes.`application/xml`, xmlCatalog)) ~> addHeaders(headers)
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

  def getAvailableBasePlans(originalSender: ActorRef) = {
    log.info("Requesting Available Base Plans...")

    import PlanDetailJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[PlanDetailResult[PlanDetail]]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/catalog/availableBasePlans") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getAvailableAddons(originalSender: ActorRef, baseProductName: String) = {
    log.info("Requesting Available Add-ons...")

    import PlanDetailJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[PlanDetailResult[PlanDetail]]]

    var suffixUrl = ""
    if (!baseProductName.equalsIgnoreCase("")) {
      suffixUrl = "?baseProductName=" + baseProductName
    }

    val responseFuture = pipeline {
      Get(killBillUrl+s"/catalog/availableAddons" + suffixUrl) ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }

  def getSimpleCatalog(originalSender: ActorRef) = {
    log.info("Requesting Simple Catalog...")

    import CatalogJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[CatalogResult[Catalog]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/catalog/simpleCatalog") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        val catalog = new Catalog(response.name, response.products)
        originalSender ! catalog
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}