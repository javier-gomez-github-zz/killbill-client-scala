package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class RoleDefinition(
  role: Option[String],
  permissions: Option[List[String]]
)

case class RoleDefinitionResult[T](
  role: Option[String],
  permissions: Option[List[String]]
)

object RoleDefinitionJsonProtocol extends DefaultJsonProtocol {
  implicit val roleDefinitionFormat = jsonFormat2(RoleDefinition)
  implicit def roleDefinitionResultFormat[T :JsonFormat] = jsonFormat2(RoleDefinitionResult.apply[T])
}
