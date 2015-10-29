package org.killbill.billing.client.model

import java.time.LocalDate

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Credit(
  creditAmount: Option[BigDecimal],
  invoiceId: Option[String],
  invoiceNumber: Option[String],
  effectiveDate: Option[String],
  accountId: Option[String]
)

case class CreditResult[T](
  creditAmount: Option[BigDecimal],
  invoiceId: Option[String],
  invoiceNumber: Option[String],
  effectiveDate: Option[String],
  accountId: Option[String]
)

object CreditJsonProtocol extends DefaultJsonProtocol {
  implicit val creditFormat = jsonFormat5(Credit)
  implicit def creditResultFormat[T :JsonFormat] = jsonFormat5(CreditResult.apply[T])
}
