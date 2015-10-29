package org.killbill.billing.client.model

/**
  * Created by jgomez on 28/10/2015.
  */
object EntitlementState extends Enumeration {
  type EntitlementState = Value
  val ACTIVE = Value("ACTIVE")
  val BLOCKED = Value("BLOCKED")
  val CANCELLED = Value("CANCELLED")
}