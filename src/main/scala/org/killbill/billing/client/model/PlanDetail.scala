package org.killbill.billing.client.model

import org.killbill.billing.client.model.BillingPeriod.BillingPeriod
import org.killbill.billing.client.util.JsonHelper
import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PlanDetail(
  product: Option[String],
  plan: Option[String],
  finalPhaseBillingPeriod: Option[BillingPeriod],
  priceList: Option[String],
  finalPhaseRecurringPrice: Option[List[Price]]
)

case class PlanDetailResult[T](
  product: Option[String],
  plan: Option[String],
  finalPhaseBillingPeriod: Option[BillingPeriod],
  priceList: Option[String],
  finalPhaseRecurringPrice: Option[List[Price]]
)

object PlanDetailJsonProtocol extends DefaultJsonProtocol {
  import PriceJsonProtocol._

  implicit val billingPeriodFormat = JsonHelper.jsonEnum(BillingPeriod)
  implicit val planDetailFormat = jsonFormat5(PlanDetail)
  implicit def planDetailResultFormat[T :JsonFormat] = jsonFormat5(PlanDetailResult.apply[T])
}
