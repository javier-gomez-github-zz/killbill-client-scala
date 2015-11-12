package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Bundle(
  accountId: Option[String],
  bundleId: Option[String],
  externalKey: Option[String],
  subscriptions: Option[List[Subscription]],
  timeline: Option[BundleTimeline]
)

case class BundleResult[T](
  accountId: Option[String],
  bundleId: Option[String],
  externalKey: Option[String],
  subscriptions: Option[List[Subscription]],
  timeline: Option[BundleTimeline]
)

object BundleJsonProtocol extends DefaultJsonProtocol {
  import BundleTimelineJsonProtocol._
  import SubscriptionJsonProtocol._
  implicit val bundleFormat = jsonFormat5(Bundle)
  implicit def bundleResultFormat[T :JsonFormat] = jsonFormat5(BundleResult.apply[T])
}
