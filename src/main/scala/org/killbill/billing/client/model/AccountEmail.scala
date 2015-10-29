package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class AccountEmail(
  accountId: Option[String],
  email: Option[String]
)

case class AccountEmailResult[T](
  accountId: Option[String],
  email: Option[String]
)

object AccountEmailJsonProtocol extends DefaultJsonProtocol {
  implicit val accountEmailFormat = jsonFormat2(AccountEmail)
  implicit def accountEmailResultFormat[T :JsonFormat] = jsonFormat2(AccountEmailResult.apply[T])
}
