package org.killbill.billing.client.model

/**
  * Created by jgomez on 28/10/2015.
  */
object EntitlementSourceType extends Enumeration {
  type EntitlementSourceType = Value
  val NATIVE = Value("NATIVE")
  val MIGRATED = Value("MIGRATED")
  val TRANSFERRED = Value("TRANSFERRED")
}