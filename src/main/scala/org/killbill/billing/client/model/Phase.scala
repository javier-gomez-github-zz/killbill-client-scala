package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Phase(
  `type`: Option[String],
  prices: Option[List[Price]]
)

case class PhaseResult[T](
  `type`: Option[String],
  prices: Option[List[Price]]
)

object PhaseJsonProtocol extends DefaultJsonProtocol {
  import PriceJsonProtocol._
  implicit val phaseFormat = jsonFormat2(Phase)
  implicit def phaseResultFormat[T :JsonFormat] = jsonFormat2(PhaseResult.apply[T])
}
