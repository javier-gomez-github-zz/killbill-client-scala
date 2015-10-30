package org.killbill.billing.client.model

/**
  * Created by jgomez on 30/10/2015.
  */
object ObjectType extends Enumeration {
  type ObjectType = Value
  val ACCOUNT = Value("ACCOUNT")
  val ACCOUNT_EMAIL = Value("ACCOUNT_EMAIL")
  val BLOCKING_STATES = Value("BLOCKING_STATES")
  val BUNDLE = Value("BUNDLE")
  val CUSTOM_FIELD = Value("CUSTOM_FIELD")
  val INVOICE = Value("INVOICE")
  val PAYMENT = Value("PAYMENT")
  val TRANSACTION = Value("TRANSACTION")
  val INVOICE_ITEM = Value("INVOICE_ITEM")
  val INVOICE_PAYMENT = Value("INVOICE_PAYMENT")
  val SUBSCRIPTION = Value("SUBSCRIPTION")
  val SUBSCRIPTION_EVENT = Value("SUBSCRIPTION_EVENT")
  val PAYMENT_ATTEMPT = Value("PAYMENT_ATTEMPT")
  val PAYMENT_METHOD = Value("PAYMENT_METHOD")
  val REFUND = Value("REFUND")
  val TAG = Value("TAG")
  val TAG_DEFINITION = Value("TAG_DEFINITION")
  val TENANT = Value("TENANT")
  val TENANT_KVS = Value("TENANT_KVS")
}