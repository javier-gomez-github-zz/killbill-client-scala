package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class UserRoles(
  username: Option[String],
  password: Option[String],
  roles: Option[List[String]]
)

case class UserRolesResult[T](
  username: Option[String],
  password: Option[String],
  roles: Option[List[String]]
)

object UserRolesJsonProtocol extends DefaultJsonProtocol {
  implicit val creditFormat = jsonFormat3(UserRoles)
  implicit def creditResultFormat[T :JsonFormat] = jsonFormat3(UserRolesResult.apply[T])
}
