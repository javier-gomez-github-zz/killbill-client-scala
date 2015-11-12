package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class ComboHostedPaymentPage(
  hostedPaymentPageFields: Option[HostedPaymentPageFields],
  account: Option[Account],
  paymentMethod: Option[PaymentMethod],
  paymentMethodPluginProperties: Option[List[PluginProperty]]
)

case class ComboHostedPaymentPageResult[T](
  hostedPaymentPageFields: Option[HostedPaymentPageFields],
  account: Option[Account],
  paymentMethod: Option[PaymentMethod],
  paymentMethodPluginProperties: Option[List[PluginProperty]]
)

object ComboHostedPaymentPageJsonProtocol extends DefaultJsonProtocol {
  import AccountJsonProtocol._
  import HostedPaymentPageFieldsJsonProtocol._
  import PaymentMethodJsonProtocol._
  import PluginPropertyJsonProtocol._
  implicit val comboHostedPaymentPageFormat = jsonFormat4(ComboHostedPaymentPage)
  implicit def comboHostedPaymentPageResultFormat[T :JsonFormat] = jsonFormat4(ComboHostedPaymentPageResult.apply[T])
}
