package org.killbill.billing.client.model

/**
  * Created by jgomez on 28/10/2015.
  */
object PhaseType extends Enumeration {
  type PhaseType = Value
  val TRIAL = Value("TRIAL")
  val DISCOUNT = Value("DISCOUNT")
  val FIXEDTERM = Value("FIXEDTERM")
  val EVERGREEN = Value("EVERGREEN")
}