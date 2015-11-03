package org.killbill.billing.client.model

/**
  * Created by jgomez on 03/11/2015.
  */
object SubscriptionEventType extends Enumeration {
  type SubscriptionEventType = Value
  val START_ENTITLEMENT = Value(ObjectType.SUBSCRIPTION_EVENT.toString)
  val START_BILLING = Value(ObjectType.SUBSCRIPTION_EVENT.toString)
  val PAUSE_ENTITLEMENT = Value(ObjectType.BLOCKING_STATES.toString)
  val PAUSE_BILLING = Value(ObjectType.BLOCKING_STATES.toString)
  val RESUME_ENTITLEMENT = Value(ObjectType.BLOCKING_STATES.toString)
  val RESUME_BILLING = Value(ObjectType.BLOCKING_STATES.toString)
  val PHASE = Value(ObjectType.SUBSCRIPTION_EVENT.toString)
  val CHANGE = Value(ObjectType.SUBSCRIPTION_EVENT.toString)
  val STOP_ENTITLEMENT = Value(ObjectType.BLOCKING_STATES.toString)
  val STOP_BILLING = Value(ObjectType.SUBSCRIPTION_EVENT.toString)
  val SERVICE_STATE_CHANGE = Value(ObjectType.BLOCKING_STATES.toString)
 }