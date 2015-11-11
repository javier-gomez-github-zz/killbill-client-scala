package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Catalog(
  name: Option[String],
  products: Option[List[Product]]
)

case class CatalogResult[T](
  name: Option[String],
  products: Option[List[Product]]
)

object CatalogJsonProtocol extends DefaultJsonProtocol {
  import ProductJsonProtocol._
  implicit val catalogFormat = jsonFormat2(Catalog)
  implicit def catalogResultFormat[T :JsonFormat] = jsonFormat2(CatalogResult.apply[T])
}
