package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class InvoiceEmail(
  accountId: Option[String],
  isNotifiedForInvoices: Option[Boolean]
)

case class InvoiceEmailResult[T](
  accountId: Option[String],
  isNotifiedForInvoices: Option[Boolean]
)

object InvoiceEmailJsonProtocol extends DefaultJsonProtocol {
  implicit val invoiceEmailFormat = jsonFormat2(InvoiceEmail)
  implicit def invoiceEmailResultFormat[T :JsonFormat] = jsonFormat2(InvoiceEmailResult.apply[T])
}
