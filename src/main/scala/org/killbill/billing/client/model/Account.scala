package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Account(
  accountId: Option[String],
  externalKey: Option[String],
  accountCBA: Option[BigDecimal],
  accountBalance: Option[BigDecimal],
  name: Option[String],
  firstNameLength: Option[Int],
  email: Option[String],
  billCycleDayLocal: Option[Int],
  currency: Option[String],
  paymentMethodId: Option[String],
  timeZone: Option[String],
  address1: Option[String],
  address2: Option[String],
  postalCode: Option[String],
  company: Option[String],
  city: Option[String],
  state: Option[String],
  country: Option[String],
  locale: Option[String],
  phone: Option[String],
  isMigrated: Option[Boolean],
  isNotifiedForInvoices: Option[Boolean]
)

case class AccountResult[T](
  accountId: Option[String],
  externalKey: Option[String],
  accountCBA: Option[BigDecimal],
  accountBalance: Option[BigDecimal],
  name: Option[String],
  firstNameLength: Option[Int],
  email: Option[String],
  billCycleDayLocal: Option[Int],
  currency: Option[String],
  paymentMethodId: Option[String],
  timeZone: Option[String],
  address1: Option[String],
  address2: Option[String],
  postalCode: Option[String],
  company: Option[String],
  city: Option[String],
  state: Option[String],
  country: Option[String],
  locale: Option[String],
  phone: Option[String],
  isMigrated: Option[Boolean],
  isNotifiedForInvoices: Option[Boolean]
)

object AccountJsonProtocol extends DefaultJsonProtocol {
  implicit val accountFormat = jsonFormat22(Account)
  implicit def accountResultFormat[T :JsonFormat] = jsonFormat22(AccountResult.apply[T])
}
