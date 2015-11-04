package org.killbill.billing.client.model

import org.killbill.billing.client.model.ObjectType.ObjectType
import org.killbill.billing.client.util.JsonHelper
import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Tag(
  tagId: Option[String],
  objectType: Option[ObjectType],
  objectId: Option[String],
  tagDefinitionId: Option[String],
  tagDefinitionName: Option[String]
)

case class TagResult[T](
  tagId: Option[String],
  objectType: Option[ObjectType],
  objectId: Option[String],
  tagDefinitionId: Option[String],
  tagDefinitionName: Option[String]
)

object TagJsonProtocol extends DefaultJsonProtocol {
  implicit val objectTypeFormat = JsonHelper.jsonEnum(ObjectType)
  implicit val tagFormat = jsonFormat5(Tag)
  implicit def tagResultFormat[T :JsonFormat] = jsonFormat5(TagResult.apply[T])
}
