package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class HostedPaymentPageFields(
  formFields: Option[List[PluginProperty]]
)

case class HostedPaymentPageFieldsResult[T](
  formFields: Option[List[PluginProperty]]
)

object HostedPaymentPageFieldsJsonProtocol extends DefaultJsonProtocol {
  import PluginPropertyJsonProtocol._
  implicit val hostedPaymentPageFieldsFormat = jsonFormat1(HostedPaymentPageFields)
  implicit def hostedPaymentPageFieldsResultFormat[T :JsonFormat] = jsonFormat1(HostedPaymentPageFieldsResult.apply[T])
}
