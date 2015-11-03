package org.killbill.billing.client.model

import org.killbill.billing.client.model.BillingActionPolicy.BillingActionPolicy
import org.killbill.billing.client.model.BillingPeriod.BillingPeriod
import org.killbill.billing.client.model.DryRunType.DryRunType
import org.killbill.billing.client.model.PhaseType.PhaseType
import org.killbill.billing.client.model.ProductCategory.ProductCategory
import org.killbill.billing.client.model.SubscriptionEventType.SubscriptionEventType
import org.killbill.billing.client.util.JsonHelper
import spray.json.{DefaultJsonProtocol, JsonFormat}

case class InvoiceDryRun(
  dryRunType: Option[DryRunType],
  dryRunAction: Option[SubscriptionEventType],
  phaseType: Option[PhaseType],
  productName: Option[String],
  productCategory: Option[ProductCategory],
  billingPeriod: Option[BillingPeriod],
  priceListName: Option[String],
  effectiveDate: Option[String],
  subscriptionId: Option[String],
  bundleId: Option[String],
  billingPolicy: Option[BillingActionPolicy],
  priceOverrides: Option[List[PhasePriceOverride]]
)

case class InvoiceDryRunResult[T](
  dryRunType: Option[DryRunType],
  dryRunAction: Option[SubscriptionEventType],
  phaseType: Option[PhaseType],
  productName: Option[String],
  productCategory: Option[ProductCategory],
  billingPeriod: Option[BillingPeriod],
  priceListName: Option[String],
  effectiveDate: Option[String],
  subscriptionId: Option[String],
  bundleId: Option[String],
  billingPolicy: Option[BillingActionPolicy],
  priceOverrides: Option[List[PhasePriceOverride]]
)

object InvoiceDryRunJsonProtocol extends DefaultJsonProtocol {
  implicit val dryRunTypeFormat = JsonHelper.jsonEnum(DryRunType)
  implicit val subscriptionEventTypeFormat = JsonHelper.jsonEnum(SubscriptionEventType)
  implicit val phaseTypeFormat = JsonHelper.jsonEnum(PhaseType)
  implicit val productCategoryFormat = JsonHelper.jsonEnum(ProductCategory)
  implicit val billingPeriodFormat = JsonHelper.jsonEnum(BillingPeriod)
  implicit val billingPolicyFormat = JsonHelper.jsonEnum(BillingActionPolicy)

  import PhasePriceOverrideJsonProtocol._

  implicit val invoiceDryRunFormat = jsonFormat12(InvoiceDryRun)
  implicit def invoiceDryRunResultFormat[T :JsonFormat] = jsonFormat12(InvoiceDryRunResult.apply[T])
}
