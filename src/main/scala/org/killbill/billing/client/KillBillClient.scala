package org.killbill.billing.client.actor

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import org.killbill.billing.client.actor.AccountActor._
import org.killbill.billing.client.actor.BundleActor._
import org.killbill.billing.client.actor.CreditActor.{CreateCredit, GetCredit}
import org.killbill.billing.client.actor.InvoiceActor._
import org.killbill.billing.client.actor.InvoicePaymentActor._
import org.killbill.billing.client.actor.OverdueActor.{GetOverdueStateForAccount, GetXMLOverdueConfig, UploadXMLOverdueConfig}
import org.killbill.billing.client.actor.PaymentActor._
import org.killbill.billing.client.actor.PaymentGatewayActor.{BuildComboFormDescriptor, BuildFormDescriptor, ProcessNotification}
import org.killbill.billing.client.actor.PaymentMethodActor._
import org.killbill.billing.client.actor.SubscriptionActor._
import org.killbill.billing.client.actor.TagActor._
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
  val overdueActor = system.actorOf(Props(new OverdueActor(killBillUrl, headers)), name = "OverdueActor")
  val tagActor = system.actorOf(Props(new TagActor(killBillUrl, headers)), name = "TagActor")
  val creditActor = system.actorOf(Props(new CreditActor(killBillUrl, headers)), name = "CreditActor")
  val paymentActor = system.actorOf(Props(new PaymentActor(killBillUrl, headers)), name = "PaymentActor")
  val invoicePaymentActor = system.actorOf(Props(new InvoicePaymentActor(killBillUrl, headers)), name = "InvoicePaymentActor")
  val paymentGatewayActor = system.actorOf(Props(new PaymentGatewayActor(killBillUrl, headers)), name = "PaymentGatewayActor")
  val paymentMethodActor = system.actorOf(Props(new PaymentMethodActor(killBillUrl, headers)), name = "PaymentMethodActor")

  /**
  Public methods to connect to the KillBill API
   */

  // Payment Methods
  def deletePaymentMethod(paymentMethodId: UUID, deleteDefault: Boolean = false, pluginProperties: Map[String, String] = Map[String, String]()): String = {
    val future: Future[String] = ask(paymentMethodActor, DeletePaymentMethod(paymentMethodId, deleteDefault, pluginProperties)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def updatePaymentMethod(accountId: UUID, paymentMethodId: UUID, pluginProperties: Map[String, String] = Map[String, String](),
                          payAllUnpaidInvoices: Boolean = false): String = {
    val future: Future[String] = ask(paymentMethodActor, UpdatePaymentMethod(accountId, paymentMethodId, pluginProperties, payAllUnpaidInvoices)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createPaymentMethod(accountId: UUID, paymentMethod: PaymentMethod, isDefault: Boolean = false,
                          payAllUnpaidInvoices: Boolean = false): Any = {
    val future: Future[Any] = ask(paymentMethodActor, CreatePaymentMethod(accountId, paymentMethod, isDefault, payAllUnpaidInvoices)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getPaymentMethodsForAccount(accountId: UUID, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(paymentMethodActor, GetPaymentMethodsForAccount(accountId, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getPaymentMethodByExternalKey(externalKey: String, withPluginInfo: Boolean = false, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(paymentMethodActor, GetPaymentMethodByExternalKey(externalKey, withPluginInfo, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getPaymentMethodById(paymentMethodId: UUID, withPluginInfo: Boolean = false, auditLevel: String = "NONE"): Any = {
    val future: Future[Any] = ask(paymentMethodActor, GetPaymentMethodById(paymentMethodId, withPluginInfo, auditLevel)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def searchPaymentMethods(searchKey: String, offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE",
                           withPluginInfo: Boolean = false, pluginName: String = ""): List[Any] = {
    val future: Future[List[Any]] = ask(paymentMethodActor, SearchPaymentMethods(searchKey, offset, limit, auditLevel,
      withPluginInfo, pluginName)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getPaymentMethods(offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(paymentMethodActor, GetPaymentMethods(offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  // Payment Gateways (Hosted Payment pages)
  def processNotification(notification: String, pluginName: String, pluginProperties: Map[String, String] = Map[String, String]()): Any = {
    val future: Future[Any] = ask(paymentGatewayActor, ProcessNotification(notification, pluginName, pluginProperties)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def buildComboFormDescriptor(comboHostedPaymentPage: ComboHostedPaymentPage, controlPluginNames: List[String] = List[String](),
                               pluginProperties: Map[String, String] = Map[String, String]()): Any = {
    val future: Future[Any] = ask(paymentGatewayActor, BuildComboFormDescriptor(comboHostedPaymentPage, controlPluginNames, pluginProperties)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def buildFormDescriptor(fields: HostedPaymentPageFields, kbAccountId: UUID, kbPaymentMethodId: UUID,
                          pluginProperties: Map[String, String] = Map[String, String]()): Any = {
    val future: Future[Any] = ask(paymentGatewayActor, BuildFormDescriptor(fields, kbAccountId, kbPaymentMethodId, pluginProperties)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  // Payments
  def voidPayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String] = Map[String, String]()): String = {
    val future: Future[String] = ask(paymentActor, VoidPayment(paymentTransaction, pluginProperties)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def chargebackPayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String] = Map[String, String]()): String = {
    val future: Future[String] = ask(paymentActor, ChargebackPayment(paymentTransaction, pluginProperties)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def refundPayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String] = Map[String, String]()): String = {
    val future: Future[String] = ask(paymentActor, RefundPayment(paymentTransaction, pluginProperties)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def captureAuthorization(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String] = Map[String, String]()): String = {
    val future: Future[String] = ask(paymentActor, CaptureAuthorization(paymentTransaction, pluginProperties)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def completePayment(paymentTransaction: PaymentTransaction, pluginProperties: Map[String, String] = Map[String, String]()): String = {
    val future: Future[String] = ask(paymentActor, CompletePayment(paymentTransaction, pluginProperties)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createPayment(accountId: UUID, paymentMethodId: UUID = null, paymentTransaction: PaymentTransaction,
                    pluginProperties: Map[String, String] = Map[String, String]()): Any = {
    val future: Future[Any] = ask(paymentActor, CreatePayment(accountId, paymentMethodId, paymentTransaction, pluginProperties)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def createComboPayment(comboPaymentTransaction: ComboPaymentTransaction, controlPluginNames: List[String] = List[String](),
                         pluginProperties: Map[String, String] = Map[String, String]()): Any = {
    val future: Future[Any] = ask(paymentActor, CreateComboPayment(comboPaymentTransaction, controlPluginNames, pluginProperties)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getPaymentsForAccount(accountId: UUID, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(paymentActor, GetPaymentsForAccount(accountId, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def searchPayments(searchKey: String, offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE", withPluginInfo: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(paymentActor, SearchPayments(searchKey, offset, limit, auditLevel, withPluginInfo)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getPaymentById(paymentId: UUID, withPluginInfo: Boolean = false, pluginProperties: Map[String, String] = Map[String, String](),
                     audit: String = "NONE"): Any = {
    val future: Future[Any] = ask(paymentActor, GetPaymentById(paymentId, withPluginInfo, pluginProperties, audit)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getPayments(offset: Long = 0, limit: Long = 100, pluginName: String = "",
                  pluginProperties: Map[String, String] = Map[String, String](),
                  auditLevel: String = "NONE", withPluginInfo: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(paymentActor, GetPayments(offset, limit, pluginName, pluginProperties, auditLevel, withPluginInfo)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  // Invoice Payments
  def createInvoicePayment(invoiceId: UUID, invoicePayment: InvoicePayment, isExternal: Boolean = false): String = {
    val future: Future[String] = ask(invoicePaymentActor, CreateInvoicePayment(invoiceId, invoicePayment, isExternal)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def payAllInvoices(accountId: UUID, externalPayment: Boolean = false, paymentAmount: BigDecimal = null): String = {
    val future: Future[String] = ask(invoicePaymentActor, PayAllInvoices(accountId, externalPayment, paymentAmount)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getInvoicePayment(invoiceId: UUID): List[Any] = {
    val future: Future[List[Any]] = ask(invoicePaymentActor, GetInvoicePayment(invoiceId)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getInvoicePaymentsForAccount(accountId: UUID, auditLevel: String = "NONE", withPluginInfo: String = ""): List[Any] = {
    val future: Future[List[Any]] = ask(invoicePaymentActor, GetInvoicePaymentsForAccount(accountId, auditLevel, withPluginInfo)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def createInvoicePaymentRefund(paymentId: UUID, refundTransaction: InvoicePaymentTransaction): String = {
    val future: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentRefund(paymentId, refundTransaction)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createInvoicePaymentChargeback(paymentId: UUID, chargebackTransaction: InvoicePaymentTransaction): String = {
    val future: Future[String] = ask(invoicePaymentActor, CreateInvoicePaymentChargeback(paymentId, chargebackTransaction)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  // Credits
  def createCredit(credit: Credit): String = {
    val future: Future[String] = ask(creditActor, CreateCredit(credit)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getCredit(creditId: UUID): Any = {
    val future: Future[Any] = ask(creditActor, GetCredit(creditId)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  // Tags
  def deletePaymentTag(paymentId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, DeletePaymentTag(paymentId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createPaymentTag(paymentId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, CreatePaymentTag(paymentId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getPaymentTags(paymentId: UUID, auditLevel: String = "NONE", includedDeleted: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetPaymentTags(paymentId, auditLevel, includedDeleted)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def deleteInvoiceTag(invoiceId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, DeleteInvoiceTag(invoiceId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createInvoiceTag(invoiceId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, CreateInvoiceTag(invoiceId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getInvoiceTags(invoiceId: UUID, auditLevel: String = "NONE", includedDeleted: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetInvoiceTags(invoiceId, auditLevel, includedDeleted)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def deleteSubscriptionTag(subscriptionId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, DeleteSubscriptionTag(subscriptionId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createSubscriptionTag(subscriptionId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, CreateSubscriptionTag(subscriptionId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getSubscriptionTags(subscriptionId: UUID, auditLevel: String = "NONE", includedDeleted: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetSubscriptionTags(subscriptionId, auditLevel, includedDeleted)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def deleteBundleTag(bundleId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, DeleteBundleTag(bundleId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createBundleTag(bundleId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, CreateBundleTag(bundleId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getBundleTags(bundleId: UUID, auditLevel: String = "NONE", includedDeleted: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetBundleTags(bundleId, auditLevel, includedDeleted)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def deleteAccountTag(accountId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, DeleteAccountTag(accountId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createAccountTag(accountId: UUID, tagDefinitionId: UUID): String = {
    val future: Future[String] = ask(tagActor, CreateAccountTag(accountId, tagDefinitionId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getAccountTags(accountId: UUID, auditLevel: String = "NONE", includedDeleted: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetAccountTags(accountId, auditLevel, includedDeleted)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getAllAccountTags(accountId: UUID, objectType: String = "", auditLevel: String = "NONE", includedDeleted: Boolean = false): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetAllAccountTags(accountId, objectType, auditLevel, includedDeleted)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def searchTags(searchKey: String, offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, SearchTags(searchKey, offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def getTags(offset: Long = 0, limit: Long = 100, auditLevel: String = "NONE"): List[Any] = {
    val future: Future[List[Any]] = ask(tagActor, GetTags(offset, limit, auditLevel)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  // Overdue
  def uploadXMLOverdueConfig(overdueConfigPath: String): String = {
    val future: Future[String] = ask(overdueActor, UploadXMLOverdueConfig(overdueConfigPath)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getXMLOverdueConfig(): Any = {
    val future: Future[Any] = ask(overdueActor, GetXMLOverdueConfig()).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def getOverdueStateForAccount(accountId: UUID): Any = {
    val future: Future[Any] = ask(overdueActor, GetOverdueStateForAccount(accountId)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  // Invoices
  def getCatalogTranslation(locale: String): Any = {
    val future: Future[Any] = ask(invoiceActor, GetCatalogTranslation(locale)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def uploadCatalogTranslation(invoiceTemplate: String, locale: String): String = {
    val future: Future[String] = ask(invoiceActor, UploadCatalogTranslation(invoiceTemplate, locale)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getInvoiceTranslation(locale: String): Any = {
    val future: Future[Any] = ask(invoiceActor, GetInvoiceTranslation(locale)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def uploadInvoiceTranslation(invoiceTemplate: String, locale: String): String = {
    val future: Future[String] = ask(invoiceActor, UploadInvoiceTranslation(invoiceTemplate, locale)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def getInvoiceTemplate(manualPay: Boolean): Any = {
    val future: Future[Any] = ask(invoiceActor, GetInvoiceTemplate(manualPay)).mapTo[Any]
    Await.result(future, timeout.duration)
  }

  def uploadInvoiceTemplate(invoiceTemplate: String, manualPay: Boolean): String = {
    val future: Future[String] = ask(invoiceActor, UploadInvoiceTemplate(invoiceTemplate, manualPay)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def triggerInvoiceNotification(invoiceId: UUID): String = {
    val future: Future[String] = ask(invoiceActor, TriggerInvoiceNotification(invoiceId)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createExternalCharges(accountId: UUID, requestedDate: String = DateTime.now.toIsoDateString, autoPay: Boolean, externalCharges: List[InvoiceItem]): List[Any] = {
    val future: Future[List[Any]] = ask(invoiceActor, CreateExternalCharge(accountId, requestedDate, autoPay, externalCharges)).mapTo[List[Any]]
    Await.result(future, timeout.duration)
  }

  def adjustInvoiceItem(invoiceId: UUID, requestedDate: String = DateTime.now.toIsoDateString, invoiceItem: InvoiceItem): String = {
    val future: Future[String] = ask(invoiceActor, AdjustInvoiceItem(invoiceId, requestedDate, invoiceItem)).mapTo[String]
    Await.result(future, timeout.duration)
  }

  def createDryRunInvoice(accountId: UUID, futureDate: String = null, dryRunInfo: InvoiceDryRun): String = {
    val future: Future[String] = ask(invoiceActor, CreateDryRunInvoice(accountId, futureDate, dryRunInfo)).mapTo[String]
    Await.result(future, timeout.duration)
  }

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
