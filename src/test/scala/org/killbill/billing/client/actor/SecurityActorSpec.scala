package org.killbill.billing.client.actor

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.killbill.billing.client.model._
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import spray.http._
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * Created by jgomez on 26/11/2015.
  */
class SecurityActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito {

  import SecurityActor._

  implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))

  // Test Get Permissions Success Response
  def getPermissionsSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true
    val getPermissionsStream: InputStream = getClass.getResourceAsStream("/getPermissionsResponse.json")
    val getPermissionsJsonContent = Source.fromInputStream(getPermissionsStream, "UTF-8").getLines.mkString
    val getPermissionsBodyResponse = HttpEntity(MediaTypes.`application/json`, getPermissionsJsonContent.getBytes())
    mockResponse.entity returns getPermissionsBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "GetPermissionsActor")

    "GetPermissions should" >> {
      "return a List of String objects" in {
        val fut: Future[List[String]] = ask(securityActor, GetPermissions()).mapTo[List[String]]
        val getPermissionsResponse = Await.result(fut, timeout.duration)
        val expected = List[String]("permission1", "permission2", "permission3")
        getPermissionsResponse mustEqual expected
      }
    }
  }

  // Test Get Permissions Failure Response
  def getPermissionsFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "GetPermissionsFailureActor")

    "GetPermissions should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(securityActor, GetPermissions()).mapTo[Any]
        val getPermissionsResponse = Await.result(fut, timeout.duration)
        getPermissionsResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Add User Roles Success Response
  def addUserRolesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val addUserRolesBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns addUserRolesBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AddUserRolesActor")

    "AddUserRoles should" >> {
      "return a 201 status" in {
        val userRoles: UserRoles = UserRoles(Option("user"), Option("password"), Option(List[String]("role1", "role2")))
        val fut: Future[Any] = ask(securityActor, AddUserRoles(userRoles)).mapTo[Any]
        val addUserRolesResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        addUserRolesResponse mustEqual expected
      }
    }
  }

  // Test Add User Roles Other Response Response
  def addUserRolesOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val addUserRolesBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns addUserRolesBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AddUserRolesOtherResponseActor")

    "AddUserRoles should" >> {
      "return a 200 status" in {
        val userRoles: UserRoles = UserRoles(Option("user"), Option("password"), Option(List[String]("role1", "role2")))
        val fut: Future[Any] = ask(securityActor, AddUserRoles(userRoles)).mapTo[Any]
        val addUserRolesResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        addUserRolesResponse mustEqual expected
      }
    }
  }

  // Test Add User Roles Failure Response
  def addUserRolesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "AddUserRolesFailureActor")

    "AddUserRoles should" >> {
      "throw an Exception" in {
        val userRoles: UserRoles = UserRoles(Option("user"), Option("password"), Option(List[String]("role1", "role2")))
        val fut: Future[Any] = ask(securityActor, AddUserRoles(userRoles)).mapTo[Any]
        val addUserRolesResponse = Await.result(fut, timeout.duration)
        addUserRolesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Update User Password Success Response
  def updateUserPasswordSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updateUserPasswordBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns updateUserPasswordBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateUserPasswordActor")

    "UpdateUserPassword should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(securityActor, UpdateUserPassword("anyUsername", "newPassword")).mapTo[Any]
        val updateUserPasswordResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        updateUserPasswordResponse mustEqual expected
      }
    }
  }

  // Test Update User Password Other Response Response
  def updateUserPasswordOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updateUserPasswordBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns updateUserPasswordBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateUserPasswordOtherResponseActor")

    "UpdateUserPassword should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(securityActor, UpdateUserPassword("anyUsername", "newPassword")).mapTo[Any]
        val updateUserPasswordResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        updateUserPasswordResponse mustEqual expected
      }
    }
  }

  // Test Update User Password Failure Response
  def updateUserPasswordFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UpdateUserPasswordFailureActor")

    "UpdateUserPassword should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(securityActor, UpdateUserPassword("anyUsername", "newPassword")).mapTo[Any]
        val updateUserPasswordResponse = Await.result(fut, timeout.duration)
        updateUserPasswordResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Update User Roles Success Response
  def updateUserRolesSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updateUserRolesBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns updateUserRolesBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateUserRolesActor")

    "UpdateUserRoles should" >> {
      "return a 201 status" in {
        val newRoles: List[String] = List[String]("role1", "role2")
        val fut: Future[Any] = ask(securityActor, UpdateUserRoles("anyUsername", newRoles)).mapTo[Any]
        val updateUserRolesResponse = Await.result(fut, timeout.duration)
        val expected = "200"
        updateUserRolesResponse mustEqual expected
      }
    }
  }

  // Test Update User Roles Other Response Response
  def updateUserRolesOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val updateUserRolesBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns updateUserRolesBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "UpdateUserRolesOtherResponseActor")

    "UpdateUserRoles should" >> {
      "return a 201 status" in {
        val newRoles: List[String] = List[String]("role1", "role2")
        val fut: Future[Any] = ask(securityActor, UpdateUserRoles("anyUsername", newRoles)).mapTo[Any]
        val updateUserRolesResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        updateUserRolesResponse mustEqual expected
      }
    }
  }

  // Test Update User Roles Failure Response
  def updateUserRolesFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "UpdateUserRolesFailureActor")

    "UpdateUserRoles should" >> {
      "throw an Exception" in {
        val newRoles: List[String] = List[String]("role1", "role2")
        val fut: Future[Any] = ask(securityActor, UpdateUserRoles("anyUsername", newRoles)).mapTo[Any]
        val updateUserRolesResponse = Await.result(fut, timeout.duration)
        updateUserRolesResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Invalidate User Success Response
  def invalidateUserSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val invalidateUserBodyResponse = HttpEntity(MediaTypes.`application/json`, "204")
    mockResponse.entity returns invalidateUserBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "InvalidateUserActor")

    "InvalidateUser should" >> {
      "return a 204 status" in {
        val fut: Future[Any] = ask(securityActor, InvalidateUser("anyUsername")).mapTo[Any]
        val invalidateUserResponse = Await.result(fut, timeout.duration)
        val expected = "204"
        invalidateUserResponse mustEqual expected
      }
    }
  }

  // Test Invalidate User Other Response Response
  def invalidateUserOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val invalidateUserBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns invalidateUserBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "InvalidateUserOtherResponseActor")

    "InvalidateUser should" >> {
      "return a 201 status" in {
        val fut: Future[Any] = ask(securityActor, InvalidateUser("anyUsername")).mapTo[Any]
        val invalidateUserResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        invalidateUserResponse mustEqual expected
      }
    }
  }

  // Test Invalidate User Failure Response
  def invalidateUserFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "InvalidateUserFailureActor")

    "InvalidateUser should" >> {
      "throw an Exception" in {
        val fut: Future[Any] = ask(securityActor, InvalidateUser("anyUsername")).mapTo[Any]
        val invalidateUserResponse = Await.result(fut, timeout.duration)
        invalidateUserResponse mustEqual expectedErrorMessage
      }
    }
  }

  // Test Add Role Definition Success Response
  def addRoleDefinitionSuccessResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "201"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val addRoleDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, "201")
    mockResponse.entity returns addRoleDefinitionBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AddRoleDefinitionActor")

    "AddRoleDefinition should" >> {
      "return a 201 status" in {
        val roleDefinition: RoleDefinition = RoleDefinition(Option("role"), Option(List[String]("permission1", "permission2")))
        val fut: Future[Any] = ask(securityActor, AddRoleDefinition(roleDefinition)).mapTo[Any]
        val addRoleDefinitionResponse = Await.result(fut, timeout.duration)
        val expected = "201"
        addRoleDefinitionResponse mustEqual expected
      }
    }
  }

  // Test Add Role Definition Other Response Response
  def addRoleDefinitionOtherResponseTest() = {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockStatus.toString() returns "200"
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns false

    val addRoleDefinitionBodyResponse = HttpEntity(MediaTypes.`application/json`, "200")
    mockResponse.entity returns addRoleDefinitionBodyResponse

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.apply(mockResponse)
      }
    }), name = "AddRoleDefinitionOtherResponseActor")

    "AddRoleDefinition should" >> {
      "return a 200 status" in {
        val roleDefinition: RoleDefinition = RoleDefinition(Option("role"), Option(List[String]("permission1", "permission2")))
        val fut: Future[Any] = ask(securityActor, AddRoleDefinition(roleDefinition)).mapTo[Any]
        val addRoleDefinition = Await.result(fut, timeout.duration)
        val expected = "200"
        addRoleDefinition mustEqual expected
      }
    }
  }

  // Test Add Role Definition Failure Response
  def addRoleDefinitionFailureResponseTest() = {
    val mockFailureResponse = mock[UnsuccessfulResponseException]
    val expectedErrorMessage = "Error"

    mockFailureResponse.getMessage returns expectedErrorMessage

    val securityActor = system.actorOf(Props(new SecurityActor("AnyUrl", mock[List[HttpHeader]]) {
      override def sendAndReceive = {
        (req:HttpRequest) => Future.failed(mockFailureResponse)
      }
    }), name = "AddRoleDefinitionFailureActor")

    "AddRoleDefinition should" >> {
      "throw an Exception" in {
        val roleDefinition: RoleDefinition = RoleDefinition(Option("role"), Option(List[String]("permission1", "permission2")))
        val fut: Future[Any] = ask(securityActor, AddRoleDefinition(roleDefinition)).mapTo[Any]
        val addRoleDefinitionResponse = Await.result(fut, timeout.duration)
        addRoleDefinitionResponse mustEqual expectedErrorMessage
      }
    }
  }

  getPermissionsSuccessResponseTest()
  getPermissionsFailureResponseTest()
  addUserRolesSuccessResponseTest()
  addUserRolesOtherResponseTest()
  addUserRolesFailureResponseTest()
  updateUserPasswordSuccessResponseTest()
  updateUserPasswordOtherResponseTest()
  updateUserPasswordFailureResponseTest()
  updateUserRolesSuccessResponseTest()
  updateUserRolesOtherResponseTest()
  updateUserRolesFailureResponseTest()
  invalidateUserSuccessResponseTest()
  invalidateUserOtherResponseTest()
  invalidateUserFailureResponseTest()
  addRoleDefinitionSuccessResponseTest()
  addRoleDefinitionOtherResponseTest()
  addRoleDefinitionFailureResponseTest()
}