package org.killbill.billing.client.model

import org.killbill.billing.client.model.ObjectType.ObjectType
import org.killbill.billing.client.util.JsonHelper
import spray.json.{DefaultJsonProtocol, JsonFormat}

case class CustomField(
  customFieldId: Option[String],
  objectId: Option[String],
  objectType: Option[ObjectType],
  name: Option[String],
  value: Option[String]
)

case class CustomFieldResult[T](
  customFieldId: Option[String],
  objectId: Option[String],
  objectType: Option[ObjectType],
  name: Option[String],
  value: Option[String]
)

object CustomFieldJsonProtocol extends DefaultJsonProtocol {
  implicit val objectTypeFormat = JsonHelper.jsonEnum(ObjectType)
  implicit val customFieldFormat = jsonFormat5(CustomField)
  implicit def customFieldResultFormat[T :JsonFormat] = jsonFormat5(CustomFieldResult.apply[T])
}
