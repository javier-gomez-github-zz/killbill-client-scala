package org.killbill.billing.client.model

/**
  * Created by jgomez on 28/10/2015.
  */
object BillingActionPolicy extends Enumeration {
  type BillingActionPolicy = Value
  val END_OF_TERM = Value("END_OF_TERM")
  val IMMEDIATE = Value("IMMEDIATE")
  val ILLEGAL = Value("ILLEGAL")
 }