package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PluginProperty(
  key: Option[String],
  value: Option[String],
  isUpdatable: Option[Boolean]
)

case class PluginPropertyResult[T](
  key: Option[String],
  value: Option[String],
  isUpdatable: Option[Boolean]
)

object PluginPropertyJsonProtocol extends DefaultJsonProtocol {
  implicit val pluginPropertyFormat = jsonFormat3(PluginProperty)
  implicit def pluginPropertyResultFormat[T :JsonFormat] = jsonFormat3(PluginPropertyResult.apply[T])
}
