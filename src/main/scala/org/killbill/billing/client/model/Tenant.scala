package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Tenant(
  tenantId: Option[String],
  externalKey: Option[String],
  apiKey: Option[String],
  apiSecret: Option[String]
)

case class TenantResult[T](
  tenantId: Option[String],
  externalKey: Option[String],
  apiKey: Option[String],
  apiSecret: Option[String]
)

object TenantJsonProtocol extends DefaultJsonProtocol {
  implicit val tenantFormat = jsonFormat4(Tenant)
  implicit def tenantResultFormat[T :JsonFormat] = jsonFormat4(TenantResult.apply[T])
}
