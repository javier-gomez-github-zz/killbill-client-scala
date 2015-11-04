package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class OverdueState(
  name: Option[String],
  externalMessage: Option[String],
  daysBetweenPaymentRetries: Option[List[Int]],
  disableEntitlementAndChangesBlocked: Option[Boolean],
  blockChanges: Option[Boolean],
  isClearState: Option[Boolean],
  reevaluationIntervalDays: Option[Int]
)

case class OverdueStateResult[T](
  name: Option[String],
  externalMessage: Option[String],
  daysBetweenPaymentRetries: Option[List[Int]],
  disableEntitlementAndChangesBlocked: Option[Boolean],
  blockChanges: Option[Boolean],
  isClearState: Option[Boolean],
  reevaluationIntervalDays: Option[Int]
)

object OverdueStateJsonProtocol extends DefaultJsonProtocol {
  implicit val overdueStateFormat = jsonFormat7(OverdueState)
  implicit def overdueStateResultFormat[T :JsonFormat] = jsonFormat7(OverdueStateResult.apply[T])
}
