package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PaymentMethod(
  paymentMethodId: Option[String],
  externalKey: Option[String],
  accountId: Option[String],
  isDefault: Option[Boolean],
  pluginName: Option[String],
  pluginInfo: Option[PaymentMethodPluginDetail]
)

case class PaymentMethodResult[T](
  paymentMethodId: Option[String],
  externalKey: Option[String],
  accountId: Option[String],
  isDefault: Option[Boolean],
  pluginName: Option[String],
  pluginInfo: Option[PaymentMethodPluginDetail]
)

object PaymentMethodJsonProtocol extends DefaultJsonProtocol {
  import PaymentMethodPluginDetailJsonProtocol._
  implicit val paymentMethodFormat = jsonFormat6(PaymentMethod)
  implicit def paymentMethodResultFormat[T :JsonFormat] = jsonFormat6(PaymentMethodResult.apply[T])
}
