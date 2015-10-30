package org.killbill.billing.client.model

import org.killbill.billing.client.model.ObjectType.ObjectType
import org.killbill.billing.client.util.JsonHelper
import spray.json.{DefaultJsonProtocol, JsonFormat}

case class TagDefinition(
  id: Option[String],
  isControlTag: Option[Boolean],
  name: Option[String],
  description: Option[String],
  applicableObjectTypes: Option[List[ObjectType]]
)

case class TagDefinitionResult[T](
  id: Option[String],
  isControlTag: Option[Boolean],
  name: Option[String],
  description: Option[String],
  applicableObjectTypes: Option[List[ObjectType]]
)

object TagDefinitionJsonProtocol extends DefaultJsonProtocol {
  implicit val objectTypeFormat = JsonHelper.jsonEnum(ObjectType)
  implicit val tagDefinitionFormat = jsonFormat5(TagDefinition)
  implicit def tagDefinitionResultFormat[T :JsonFormat] = jsonFormat5(TagDefinitionResult.apply[T])
}
