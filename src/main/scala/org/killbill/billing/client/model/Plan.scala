package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Plan(
  name: Option[String],
  phases: Option[List[Phase]]
)

case class PlanResult[T](
  name: Option[String],
  phases: Option[List[Phase]]
)

object PlanJsonProtocol extends DefaultJsonProtocol {
  import PhaseJsonProtocol._
  implicit val planFormat = jsonFormat2(Plan)
  implicit def planResultFormat[T :JsonFormat] = jsonFormat2(PlanResult.apply[T])
}
