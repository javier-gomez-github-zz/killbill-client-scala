package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import org.killbill.billing.client.actor.AccountActor._
import org.killbill.billing.client.actor.TagDefinitionActor._
import org.killbill.billing.client.model.{TagDefinition, Account, AccountEmail, InvoiceEmail}
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by jgomez on 20/10/2015.
 */
class KillBillClient(killBillUrl: String, headers: List[HttpHeader with Serializable with Product]) {

  // create the system, log and other shared features
  val system = ActorSystem("killbill-api-scala-client")
  val log = Logging(system, getClass)
  implicit val timeout = Timeout(30 seconds)

  // create the actors
  val accountActor = system.actorOf(Props(new AccountActor(killBillUrl, headers)), name = "AccountActor")
  val tagDefinitionActor = system.actorOf(Props(new TagDefinitionActor(killBillUrl, headers)), name = "TagDefinitionActor")

  /**
  Public methods to connect to the KillBill API
   */

  // Tag Definitions
  def getTagDefinitions(auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(tagDefinitionActor, GetTagDefinitions(auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getTagDefinition(tagDefinitionId: UUID, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(tagDefinitionActor, GetTagDefinition(tagDefinitionId, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def createTagDefinition(tagDefinition: TagDefinition): String = {
    val future: Future[String] = ask(tagDefinitionActor, CreateTagDefinition(tagDefinition)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def deleteTagDefinition(tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagDefinitionActor, DeleteTagDefinition(tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  // Accounts

  def getEmailNotificationsForAccount(accountId: UUID) = {
    val future: Future[Any] = ask(accountActor, GetEmailNotificationsForAccount(accountId)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def updateEmailNotificationsForAccount(invoiceEmail: InvoiceEmail, accountId: UUID): String = {
    val future: Future[String] = ask(accountActor, UpdateEmailNotificationsForAccount(invoiceEmail, accountId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getEmailsForAccount(accountId: UUID): Any = {
    val future: Future[Any] = ask(accountActor, GetEmailsForAccount(accountId)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def addEmailToAccount(accountEmail: AccountEmail, accountId: UUID): String = {
    val future: Future[String] = ask(accountActor, AddEmailToAccount(accountEmail, accountId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def removeEmailFromAccount(accountId: UUID, email: String): String = {
    val future: Future[String] = ask(accountActor, RemoveEmailFromAccount(accountId, email)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def searchAccounts(searchKey: String, offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(accountActor, SearchAccounts(searchKey, offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getAccounts(offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(accountActor, GetAccounts(offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getAccountByExternalKey(externalKey: String, withBalance: Boolean = false, withCBA: Boolean = false, audit: String = "NONE"): Any = {
    val future: Future[Any] = ask(accountActor, GetAccountByExternalKey(externalKey, withBalance, withCBA, audit)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getAccountById(accountId: UUID, withBalance: Boolean = false, withCBA: Boolean = false, audit: String = "NONE"): Any = {
    val future: Future[Any] = ask(accountActor, GetAccountById(accountId, withBalance, withCBA, audit)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def createAccount(account: Account): String = {
    val future: Future[String] = ask(accountActor, CreateAccount(account)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def updateAccount(account: Account, accountId: UUID): String = {
    val future: Future[String] = ask(accountActor, UpdateAccount(account, accountId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getAccountTimeline(accountId: UUID, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(accountActor, GetAccountTimeline(accountId, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }
}
