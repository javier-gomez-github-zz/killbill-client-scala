package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Payment(
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

case class PaymentResult[T](
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

object PaymentJsonProtocol extends DefaultJsonProtocol {
  import PaymentTransactionJsonProtocol._

  implicit val paymentFormat = jsonFormat12(Payment)
  implicit def paymentResultFormat[T :JsonFormat] = jsonFormat12(PaymentResult.apply[T])
}
