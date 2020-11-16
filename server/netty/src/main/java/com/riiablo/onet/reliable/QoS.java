package com.riiablo.onet.reliable;

public enum QoS {
  /**
   * Message is guaranteed to arrive and in order.
   */
  Reliable,

  /**
   * Message is not guaranteed delivery nor order.
   */
  Unreliable,

  /**
   * Message is not guaranteed delivery, but will be in order
   */
  UnreliableOrdered;

  public static QoS valueOf(int i) {
    switch (i) {
      case 0: return Reliable;
      case 1: return Unreliable;
      case 2: return UnreliableOrdered;
      default: return null;
    }
  }
}
