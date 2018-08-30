package com.blocwatch.sdk.v1.support;

import java.time.Duration;

/** Sleeper that invokes Thread.sleep. */
public class DefaultSleeper implements Sleeper {

  @Override
  public void sleep(Duration duration) throws InterruptedException {
    Thread.sleep(duration.toMillis(), duration.getNano());
  }
}
