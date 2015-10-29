package org.killbill.billing.client.model

/**
  * Created by jgomez on 28/10/2015.
  */
object BillingPeriod extends Enumeration {
  type BillingPeriod = Value
  val MONTHLY = Value(1)
  val QUARTERLY = Value(3)
  val ANNUAL = Value(12)
  val BI_ANNUAL = Value(24)
  val NO_BILLING_PERIOD = Value(0)
 }