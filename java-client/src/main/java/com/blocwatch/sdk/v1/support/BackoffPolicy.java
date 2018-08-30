package com.blocwatch.sdk.v1.support;

import java.time.Duration;

/** A representation of how backoff should be handled on retry. */
public interface BackoffPolicy {

  /** Return the amount of time to delay before the next retry. */
  public Duration getNextDelay(int attempt);
}
