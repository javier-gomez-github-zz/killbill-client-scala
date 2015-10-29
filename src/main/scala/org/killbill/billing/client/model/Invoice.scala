package org.killbill.billing.client.model

import org.killbill.billing.client.model.Currency.Currency
import org.killbill.billing.client.util.JsonHelper
import spray.json._

case class Invoice(
  amount: Option[BigDecimal],
  currency: Option[Currency],
  invoiceId: Option[String],
  invoiceDate: Option[String],
  targetDate: Option[String],
  invoiceNumber: Option[String],
  balance: Option[BigDecimal],
  creditAdj: Option[BigDecimal],
  refundAdj: Option[BigDecimal],
  accountId: Option[String],
  items: Option[List[InvoiceItem]],
  bundleKeys: Option[String],
  credits: List[Credit]
)

case class InvoiceResult[T](
  amount: Option[BigDecimal],
  currency: Option[Currency],
  invoiceId: Option[String],
  invoiceDate: Option[String],
  targetDate: Option[String],
  invoiceNumber: Option[String],
  balance: Option[BigDecimal],
  creditAdj: Option[BigDecimal],
  refundAdj: Option[BigDecimal],
  accountId: Option[String],
  items: Option[List[InvoiceItem]],
  bundleKeys: Option[String],
  credits: List[Credit]
)

object InvoiceJsonProtocol extends DefaultJsonProtocol {
  import CreditJsonProtocol._
  import InvoiceItemJsonProtocol._
  implicit val currencyFormat = JsonHelper.jsonEnum(Currency)
  implicit val invoiceFormat = jsonFormat13(Invoice)
  implicit def invoiceResultFormat[T :JsonFormat] = jsonFormat13(InvoiceResult.apply[T])
}


