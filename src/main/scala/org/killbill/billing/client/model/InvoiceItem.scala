package org.killbill.billing.client.model

import org.killbill.billing.client.model.Currency.Currency
import org.killbill.billing.client.util.JsonHelper
import spray.json._

case class InvoiceItem(
  invoiceItemId: Option[String],
  invoiceId: Option[String],
  linkedInvoiceItemId: Option[String],
  accountId: Option[String],
  bundleId: Option[String],
  subscriptionId: Option[String],
  planName: Option[String],
  phaseName: Option[String],
  usageName: Option[String],
  itemType: Option[String],
  description: Option[String],
  startDate: Option[String],
  endDate: Option[String],
  amount: Option[BigDecimal],
  currency: Option[Currency]
)

case class InvoiceItemResult[T](
  invoiceItemId: Option[String],
  invoiceId: Option[String],
  linkedInvoiceItemId: Option[String],
  accountId: Option[String],
  bundleId: Option[String],
  subscriptionId: Option[String],
  planName: Option[String],
  phaseName: Option[String],
  usageName: Option[String],
  itemType: Option[String],
  description: Option[String],
  startDate: Option[String],
  endDate: Option[String],
  amount: Option[BigDecimal],
  currency: Option[Currency]
)

object InvoiceItemJsonProtocol extends DefaultJsonProtocol {
  implicit val currencyFormat = JsonHelper.jsonEnum(Currency)
  implicit val invoiceItemFormat = jsonFormat15(InvoiceItem)
  implicit def invoiceItemResultFormat[T :JsonFormat] = jsonFormat15(InvoiceItemResult.apply[T])
}
