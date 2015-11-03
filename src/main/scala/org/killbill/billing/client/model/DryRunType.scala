package org.killbill.billing.client.model

/**
  * Created by jgomez on 03/11/2015.
  */
object DryRunType extends Enumeration {
  type DryRunType = Value
  val TARGET_DATE = Value("TARGET_DATE")
  val UPCOMING_INVOICE = Value("UPCOMING_INVOICE")
  val SUBSCRIPTION_ACTION = Value("SUBSCRIPTION_ACTION")
 }