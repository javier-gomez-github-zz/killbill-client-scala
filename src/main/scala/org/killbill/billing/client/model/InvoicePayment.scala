package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class InvoicePayment(
  targetInvoiceId: Option[String],
  accountId: Option[String],
  paymentId: Option[String],
  paymentNumber: Option[String],
  paymentExternalKey: Option[String],
  authAmount: Option[BigDecimal],
  capturedAmount: Option[BigDecimal],
  purchasedAmount: Option[BigDecimal],
  refundedAmount: Option[BigDecimal],
  creditedAmount: Option[BigDecimal],
  currency: Option[String],
  paymentMethodId: Option[String],
  transactions: Option[List[PaymentTransaction]]
)

case class InvoicePaymentResult[T](
  targetInvoiceId: Option[String],
  accountId: Option[String],
  paymentId: Option[String],
  paymentNumber: Option[String],
  paymentExternalKey: Option[String],
  authAmount: Option[BigDecimal],
  capturedAmount: Option[BigDecimal],
  purchasedAmount: Option[BigDecimal],
  refundedAmount: Option[BigDecimal],
  creditedAmount: Option[BigDecimal],
  currency: Option[String],
  paymentMethodId: Option[String],
  transactions: Option[List[PaymentTransaction]]
)

object InvoicePaymentJsonProtocol extends DefaultJsonProtocol {
  import PaymentTransactionJsonProtocol._

  implicit val invoicePaymentFormat = jsonFormat13(InvoicePayment)
  implicit def invoicePaymentResultFormat[T :JsonFormat] = jsonFormat13(InvoicePaymentResult.apply[T])
}
