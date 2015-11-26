package org.killbill.billing.client.model

import org.killbill.billing.client.model.BillingPeriod.BillingPeriod
import org.killbill.billing.client.model.EntitlementSourceType.EntitlementSourceType
import org.killbill.billing.client.model.EntitlementState.EntitlementState
import org.killbill.billing.client.model.PhaseType.PhaseType
import org.killbill.billing.client.model.ProductCategory.ProductCategory
import org.killbill.billing.client.util.JsonHelper
import spray.json._

case class Subscription(
  accountId: Option[String],
  bundleId: Option[String],
  subscriptionId: Option[String],
  externalKey: Option[String],
  startDate: Option[String],
  productName: Option[String],
  productCategory: Option[ProductCategory],
  billingPeriod: Option[BillingPeriod],
  phaseType: Option[PhaseType],
  priceList: Option[String],
  state: Option[EntitlementState],
  sourceType: Option[EntitlementSourceType],
  cancelledDate: Option[String],
  chargedThroughDate: Option[String],
  billingStartDate: Option[String],
  billingEndDate: Option[String],
  events: Option[List[EventSubscription]],
  priceOverrides: Option[List[PhasePriceOverride]]
)

case class SubscriptionResult[T](
  accountId: Option[String],
  bundleId: Option[String],
  subscriptionId: Option[String],
  externalKey: Option[String],
  startDate: Option[String],
  productName: Option[String],
  productCategory: Option[ProductCategory],
  billingPeriod: Option[BillingPeriod],
  phaseType: Option[PhaseType],
  priceList: Option[String],
  state: Option[EntitlementState],
  sourceType: Option[EntitlementSourceType],
  cancelledDate: Option[String],
  chargedThroughDate: Option[String],
  billingStartDate: Option[String],
  billingEndDate: Option[String],
  events: Option[List[EventSubscription]],
  priceOverrides: Option[List[PhasePriceOverride]]
)

object SubscriptionJsonProtocol extends DefaultJsonProtocol {
  implicit val billingPeriodFormat = JsonHelper.jsonEnum(BillingPeriod)
  implicit val phaseTypeFormat = JsonHelper.jsonEnum(PhaseType)
  implicit val entitlementStateFormat = JsonHelper.jsonEnum(EntitlementState)
  implicit val entitlementSourceTypeFormat = JsonHelper.jsonEnum(EntitlementSourceType)
  implicit val productCategoryFormat = JsonHelper.jsonEnum(ProductCategory)

  import EventSubscriptionJsonProtocol._
  import PhasePriceOverrideJsonProtocol._

  implicit val subscriptionFormat = jsonFormat18(Subscription)
  implicit def subscriptionResultFormat[T :JsonFormat] = jsonFormat18(SubscriptionResult.apply[T])
}
