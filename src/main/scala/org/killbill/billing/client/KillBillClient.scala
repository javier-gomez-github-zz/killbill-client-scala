package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import org.killbill.billing.client.actor.AccountActor._
import org.killbill.billing.client.actor.BundleActor._
import org.killbill.billing.client.actor.InvoiceActor._
import org.killbill.billing.client.actor.SubscriptionActor._
import org.killbill.billing.client.actor.TagDefinitionActor._
import org.killbill.billing.client.model.BillingActionPolicy.BillingActionPolicy
import org.killbill.billing.client.model._
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
  val bundleActor = system.actorOf(Props(new BundleActor(killBillUrl, headers)), name = "BundleActor")
  val subscriptionActor = system.actorOf(Props(new SubscriptionActor(killBillUrl, headers)), name = "SubscriptionActor")
  val invoiceActor = system.actorOf(Props(new InvoiceActor(killBillUrl, headers)), name = "InvoiceActor")

  /**
  Public methods to connect to the KillBill API
   */

  // Invoices
  def createInvoice(accountId: UUID, futureDate: String = DateTime.now.toIsoDateString): String = {
    val future: Future[String] = ask(invoiceActor, CreateInvoice(accountId, futureDate)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def searchInvoices(searchKey: String, offset: Long = 0, limit: Long = 100, withItems: Boolean = false, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(invoiceActor, SearchInvoices(searchKey, offset, limit, withItems, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getInvoicesForAccount(accountId: UUID, withItems: Boolean = false, unpaidInvoicesOnly: Boolean = false, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(invoiceActor, GetInvoicesForAccount(accountId, withItems, unpaidInvoicesOnly, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getInvoiceByIdOrNumber(invoiceIdOrNumber: String, withItems: Boolean = false, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(invoiceActor, GetInvoiceByIdOrNumber(invoiceIdOrNumber, withItems, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }
  
  def getInvoiceByNumber(invoiceNumber: Int, withItems: Boolean = false, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(invoiceActor, GetInvoiceByNumber(invoiceNumber, withItems, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }
  
  def getInvoiceById(invoiceId: UUID, withItems: Boolean = false, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(invoiceActor, GetInvoiceById(invoiceId, withItems, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getInvoices(offset: Long = 0, limit: Long = 100, withItems: Boolean = false, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(invoiceActor, GetInvoices(offset, limit, withItems, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  // Subscriptions
  def unCancelSubscription(subscriptionId: UUID): String = {
    val future: Future[String] = ask(subscriptionActor, UnCancelSubscription(subscriptionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def cancelSubscription(subscriptionId: UUID): String = {
    val future: Future[String] = ask(subscriptionActor, CancelSubscription(subscriptionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createSubscription(subscription: Subscription): String = {
    val future: Future[String] = ask(subscriptionActor, CreateSubscription(subscription)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getSubscriptionById(subscriptionId: UUID): Any = {
    val future: Future[Any] = ask(subscriptionActor, GetSubscriptionById(subscriptionId)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def updateSubscription(subscription: Subscription, subscriptionId: UUID): String = {
    val future: Future[String] = ask(subscriptionActor, UpdateSubscription(subscription, subscriptionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }
  
  // Bundles
  def getBundles(offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(bundleActor, GetBundles(offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }
  def getBundleByExternalKey(externalKey: String): Any = {
    val future: Future[Any] = ask(bundleActor, GetBundleByExternalKey(externalKey)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getBundleById(bundleId: UUID): Any = {
    val future: Future[Any] = ask(bundleActor, GetBundleById(bundleId)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def searchBundles(searchKey: String, offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(bundleActor, SearchBundles(searchKey, offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getAccountBundles(accountId: UUID, externalKey: String = ""): List[Any] = {
    val future: Future[List[Any]] = ask(bundleActor, GetAccountBundles(accountId, externalKey)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def transferBundleToAccount(bundle: Bundle, bundleId: UUID, billingPolicy: BillingActionPolicy = BillingActionPolicy.END_OF_TERM): String = {
    val future: Future[String] = ask(bundleActor, TransferBundleToAccount(bundle, bundleId, billingPolicy)).mapTo[String]
    Await.result(future, timeout.duration)
  }

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
