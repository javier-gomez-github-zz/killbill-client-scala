package org.killbill.billing.client.model

/**
 * Created by jgomez on 28/10/2015.
 */
object ProductCategory extends Enumeration {
  type ProductCategory = Value
  val BASE = Value("BASE")
  val ADD_ON = Value("ADD_ON")
  val STANDALONE = Value("STANDALONE")
}