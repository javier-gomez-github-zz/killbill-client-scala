package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Price(
  currency: Option[String],
  value: Option[BigDecimal]
)

case class PriceResult[T](
  currency: Option[String],
  value: Option[BigDecimal]
)

object PriceJsonProtocol extends DefaultJsonProtocol {
  implicit val priceFormat = jsonFormat2(Price)
  implicit def priceResultFormat[T :JsonFormat] = jsonFormat2(PriceResult.apply[T])
}
