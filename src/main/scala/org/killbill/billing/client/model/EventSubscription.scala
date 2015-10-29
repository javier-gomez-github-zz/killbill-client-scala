package org.killbill.billing.client.model

import spray.json._

case class EventSubscription(
  eventId: Option[String],
  billingPeriod: Option[String],
  requestedDate: Option[String],
  effectiveDate: Option[String],
  product: Option[String],
  priceList: Option[String],
  eventType: Option[String],
  isBlockedBilling: Option[Boolean],
  isBlockedEntitlement: Option[Boolean],
  serviceName: Option[String],
  serviceStateName: Option[String],
  phase: Option[String]
)

case class EventSubscriptionResult[T](
  eventId: Option[String],
  billingPeriod: Option[String],
  requestedDate: Option[String],
  effectiveDate: Option[String],
  product: Option[String],
  priceList: Option[String],
  eventType: Option[String],
  isBlockedBilling: Option[Boolean],
  isBlockedEntitlement: Option[Boolean],
  serviceName: Option[String],
  serviceStateName: Option[String],
  phase: Option[String]
)

object EventSubscriptionJsonProtocol extends DefaultJsonProtocol {
  implicit val eventSubscriptionFormat = jsonFormat12(EventSubscription)
  implicit def eventSubscriptionResultFormat[T :JsonFormat] = jsonFormat12(EventSubscriptionResult.apply[T])
}
