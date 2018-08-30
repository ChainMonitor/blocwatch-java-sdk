package com.blocwatch.sdk.v1.support;

import java.time.Duration;

/** Sleeper that invokes Thread.sleep. */
public class DefaultSleeper implements Sleeper {

  @Override
  public void sleep(Duration duration) throws InterruptedException {
    // duration.getNano returns all nanos within the second, including those contained within
    // milliseconds. Use mod to extract just the nano portion:
    Thread.sleep(duration.toMillis(), duration.getNano() % 1000000);
  }
}
