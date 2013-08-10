package org.opensplice.mobile.dev.dadds


abstract class Event

abstract class ReaderEvent extends Event

case class DataAvailable[T](reader: org.omg.dds.sub.DataReader[T]) extends ReaderEvent
case class SubscriptionMatched[T](reader: org.omg.dds.sub.DataReader[T]) extends ReaderEvent



