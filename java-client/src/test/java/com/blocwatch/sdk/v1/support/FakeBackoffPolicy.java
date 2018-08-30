package com.blocwatch.sdk.v1.support;

import java.time.Duration;

public class FakeBackoffPolicy implements BackoffPolicy {

  @Override
  public Duration getNextDelay(int attempt) {
    return Duration.ZERO;
  }
}
