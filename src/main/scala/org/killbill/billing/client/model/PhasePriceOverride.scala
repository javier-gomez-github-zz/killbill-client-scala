package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PhasePriceOverride(
  phaseName: Option[String],
  phaseType: Option[String],
  fixedPrice: Option[BigDecimal],
  recurringPrice: Option[BigDecimal]
)

case class PhasePriceOverrideResult[T](
  phaseName: Option[String],
  phaseType: Option[String],
  fixedPrice: Option[BigDecimal],
  recurringPrice: Option[BigDecimal]
)

object PhasePriceOverrideJsonProtocol extends DefaultJsonProtocol {
  implicit val phasePriceOverrideFormat = jsonFormat4(PhasePriceOverride)
  implicit def phasePriceOverrideResultFormat[T :JsonFormat] = jsonFormat4(PhasePriceOverrideResult.apply[T])
}
