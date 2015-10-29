package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class AccountTimeline(
  account: Option[Account],
  bundles: Option[List[Bundle]],
  invoices: Option[List[Invoice]],
  payments: Option[List[InvoicePayment]]
)

case class AccountTimelineResult[T](
  account: Option[Account],
  bundles: Option[List[Bundle]],
  invoices: Option[List[Invoice]],
  payments: Option[List[InvoicePayment]]
)

object AccountTimelineJsonProtocol extends DefaultJsonProtocol {
  import AccountJsonProtocol._
  import BundleJsonProtocol._
  import InvoiceJsonProtocol._
  import InvoicePaymentJsonProtocol._
  implicit val accountTimelineFormat = jsonFormat4(AccountTimeline)
  implicit def accountTimelineResultFormat[T :JsonFormat] = jsonFormat4(AccountTimelineResult.apply[T])
}
