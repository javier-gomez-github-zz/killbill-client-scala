package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PaymentMethodPluginDetail(
  externalPaymentMethodId: Option[String],
  isDefaultPaymentMethod: Option[Boolean],
  properties: Option[List[PluginProperty]]
)

case class PaymentMethodPluginDetailResult[T](
  externalPaymentMethodId: Option[String],
  isDefaultPaymentMethod: Option[Boolean],
  properties: Option[List[PluginProperty]]
)

object PaymentMethodPluginDetailJsonProtocol extends DefaultJsonProtocol {
  import PluginPropertyJsonProtocol._
  implicit val paymentMethodPluginDetailFormat = jsonFormat3(PaymentMethodPluginDetail)
  implicit def paymentMethodPluginDetailResultFormat[T :JsonFormat] = jsonFormat3(PaymentMethodPluginDetailResult.apply[T])
}
