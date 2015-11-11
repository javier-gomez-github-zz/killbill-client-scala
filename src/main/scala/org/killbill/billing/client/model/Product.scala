package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Product(
  `type`: Option[String],
  name: Option[String],
  plans: Option[List[Plan]],
  included: Option[List[String]],
  available: Option[List[String]]
)

case class ProductResult[T](
  `type`: Option[String],
  name: Option[String],
  plans: Option[List[Plan]],
  included: Option[List[String]],
  available: Option[List[String]]
)

object ProductJsonProtocol extends DefaultJsonProtocol {
  import PlanJsonProtocol._
  implicit val productFormat = jsonFormat5(Product)
  implicit def productResultFormat[T :JsonFormat] = jsonFormat5(ProductResult.apply[T])
}
