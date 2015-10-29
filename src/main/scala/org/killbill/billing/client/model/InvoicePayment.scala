package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class InvoicePayment(
  targetInvoiceId: Option[String]
)

case class InvoicePaymentResult[T](
  targetInvoiceId: Option[String]
)

object InvoicePaymentJsonProtocol extends DefaultJsonProtocol {
  implicit val invoicePaymentFormat = jsonFormat1(InvoicePayment)
  implicit def invoicePaymentResultFormat[T :JsonFormat] = jsonFormat1(InvoicePaymentResult.apply[T])
}
