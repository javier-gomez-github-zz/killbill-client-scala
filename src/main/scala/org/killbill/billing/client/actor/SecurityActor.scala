package org.killbill.billing.client.actor

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import org.killbill.billing.client.model._
import spray.client.pipelining._
import spray.http.HttpHeader
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success}

/**
  * Created by jgomez on 11/11/2015.
  */
object SecurityActor {
  case class GetPermissions()
  case class AddUserRoles(userRoles: UserRoles)
  case class UpdateUserPassword(userName: String, newPassword: String)
  case class UpdateUserRoles(userName: String, newRoles: List[String])
  case class InvalidateUser(userName: String)
  case class AddRoleDefinition(roleDefinition: RoleDefinition)
}

case class SecurityActor(killBillUrl: String, headers: List[HttpHeader]) extends Actor {

  import SecurityActor._

  implicit val system = context.system
  val parent = context.parent
  import system.dispatcher
  val log = Logging(system, getClass)
  def sendAndReceive = sendReceive

  def receive = {
    case GetPermissions() =>
      getPermissions(sender)
      context.stop(self)

    case AddUserRoles(userRoles) =>
      addUserRoles(sender, userRoles)
      context.stop(self)

    case UpdateUserPassword(userName, newPassword) =>
      updateUserPassword(sender, userName, newPassword)
      context.stop(self)

    case UpdateUserRoles(userName, newRoles) =>
      updateUserRoles(sender, userName, newRoles)
      context.stop(self)

    case InvalidateUser(userName) =>
      invalidateUser(sender, userName)
      context.stop(self)

    case AddRoleDefinition(roleDefinition) =>
      addRoleDefinition(sender, roleDefinition)
      context.stop(self)
  }

  def addRoleDefinition(originalSender: ActorRef, roleDefinition: RoleDefinition) = {
    log.info("Add Role definition...")

    import RoleDefinitionJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/security/roles", roleDefinition) ~> addHeaders(headers)
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

  def invalidateUser(originalSender: ActorRef, userName: String) = {
    log.info("Invalidating User: " + userName)

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Delete(killBillUrl+s"/security/users/$userName") ~> addHeaders(headers)
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

  def updateUserRoles(originalSender: ActorRef, userName: String, newRoles: List[String]) = {
    log.info("Update User Roles...")

    val pipeline = sendAndReceive

    import SprayJsonSupport._
    import UserRolesJsonProtocol._

    val userRoles = UserRoles.apply(Option(userName), None, Option(newRoles))

    val responseFuture = pipeline {
      Put(killBillUrl+s"/security/users/$userName/roles", userRoles) ~> addHeaders(headers)
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

  def updateUserPassword(originalSender: ActorRef, userName: String, newPassword: String) = {
    log.info("Update User Password...")

    val pipeline = sendAndReceive

    import SprayJsonSupport._
    import UserRolesJsonProtocol._

    val userRoles = UserRoles.apply(Option(userName), Option(newPassword), None)

    val responseFuture = pipeline {
      Put(killBillUrl+s"/security/users/$userName/password", userRoles) ~> addHeaders(headers)
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

  def addUserRoles(originalSender: ActorRef, userRoles: UserRoles) = {
    log.info("Add User Roles...")

    import SprayJsonSupport._
    import UserRolesJsonProtocol._

    val pipeline = sendAndReceive

    val responseFuture = pipeline {
      Post(killBillUrl+s"/security/users", userRoles) ~> addHeaders(headers)
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

  def getPermissions(originalSender: ActorRef) = {
    log.info("Getting Permissions...")

    import DefaultJsonProtocol._
    import SprayJsonSupport._

    val pipeline = sendAndReceive ~> unmarshal[List[String]]

    val responseFuture = pipeline {
      Get(killBillUrl+s"/security/permissions") ~> addHeaders(headers)
    }
    responseFuture.onComplete {
      case Success(response) =>
        originalSender ! response
      case Failure(error) =>
        originalSender ! error.getMessage
    }
  }
}