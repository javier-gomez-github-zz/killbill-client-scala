package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class InvoicePaymentTransaction(
  isAdjusted: Option[Boolean],
  adjustments: Option[List[InvoiceItem]],
  transactionId: Option[String],
  transactionExternalKey: Option[String],
  paymentId: Option[String],
  paymentExternalKey: Option[String],
  transactionType: Option[String],
  effectiveDate: Option[String],
  status: Option[String],
  amount: Option[BigDecimal],
  currency: Option[String],
  gatewayErrorCode: Option[String],
  gatewayErrorMsg: Option[String],
  firstPaymentReferenceId: Option[String],
  secondPaymentReferenceId: Option[String],
  properties: Option[List[PluginProperty]]
)

case class InvoicePaymentTransactionResult[T](
  isAdjusted: Option[Boolean],
  adjustments: Option[List[InvoiceItem]],
  transactionId: Option[String],
  transactionExternalKey: Option[String],
  paymentId: Option[String],
  paymentExternalKey: Option[String],
  transactionType: Option[String],
  effectiveDate: Option[String],
  status: Option[String],
  amount: Option[BigDecimal],
  currency: Option[String],
  gatewayErrorCode: Option[String],
  gatewayErrorMsg: Option[String],
  firstPaymentReferenceId: Option[String],
  secondPaymentReferenceId: Option[String],
  properties: Option[List[PluginProperty]]
)

object InvoicePaymentTransactionJsonProtocol extends DefaultJsonProtocol {
  import InvoiceItemJsonProtocol._
  import PluginPropertyJsonProtocol._
  implicit val invoicePaymentTransactionFormat = jsonFormat16(InvoicePaymentTransaction)
  implicit def invoicePaymentTransactionResultFormat[T :JsonFormat] = jsonFormat16(InvoicePaymentTransactionResult.apply[T])
}
