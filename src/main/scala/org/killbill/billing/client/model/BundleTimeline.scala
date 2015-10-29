package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class BundleTimeline(
  accountId: Option[String],
  bundleId: Option[String],
  externalKey: Option[String],
  events: Option[List[EventSubscription]]
)

case class BundleTimelineResult[T](
  accountId: Option[String],
  bundleId: Option[String],
  externalKey: Option[String],
  events: Option[List[EventSubscription]]
)

object BundleTimelineJsonProtocol extends DefaultJsonProtocol {
  import EventSubscriptionJsonProtocol._
  implicit val bundleTimelineFormat = jsonFormat4(BundleTimeline)
  implicit def bundleTimelineResultFormat[T :JsonFormat] = jsonFormat4(BundleTimelineResult.apply[T])
}
