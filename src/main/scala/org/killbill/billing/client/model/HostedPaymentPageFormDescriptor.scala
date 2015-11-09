package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class HostedPaymentPageFormDescriptor(
  kbAccountId: Option[String],
  formMethod: Option[String],
  formUrl: Option[String],
  formFields: Option[Map[String, String]],
  properties: Option[Map[String, String]]
)

case class HostedPaymentPageFormDescriptorResult[T](
  kbAccountId: Option[String],
  formMethod: Option[String],
  formUrl: Option[String],
  formFields: Option[Map[String, String]],
  properties: Option[Map[String, String]]
)

object HostedPaymentPageFormDescriptorJsonProtocol extends DefaultJsonProtocol {
  implicit val hostedPaymentPageFormDescriptorFormat = jsonFormat5(HostedPaymentPageFormDescriptor)
  implicit def hostedPaymentPageFormDescriptorResultFormat[T :JsonFormat] = jsonFormat5(HostedPaymentPageFormDescriptorResult.apply[T])
}
