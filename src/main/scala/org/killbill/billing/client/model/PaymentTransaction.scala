package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PaymentTransaction(
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

case class PaymentTransactionResult[T](
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

object PaymentTransactionJsonProtocol extends DefaultJsonProtocol {
  import PluginPropertyJsonProtocol._

  implicit val paymentTransactionFormat = jsonFormat14(PaymentTransaction)
  implicit def paymenTransactiontResultFormat[T :JsonFormat] = jsonFormat14(PaymentTransactionResult.apply[T])
}
