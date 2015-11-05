package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class ComboPaymentTransaction(
  transaction: Option[PaymentTransaction],
  transactionPluginProperties: Option[List[PluginProperty]]
)

case class ComboPaymentTransactionResult[T](
 transaction: Option[PaymentTransaction],
 transactionPluginProperties: Option[List[PluginProperty]]
)

object ComboPaymentTransactionJsonProtocol extends DefaultJsonProtocol {
  import PaymentTransactionJsonProtocol._
  import PluginPropertyJsonProtocol._

  implicit val comboPaymentTransactionFormat = jsonFormat2(ComboPaymentTransaction)
  implicit def comboPaymentTransactionResultFormat[T :JsonFormat] = jsonFormat2(ComboPaymentTransactionResult.apply[T])
}
