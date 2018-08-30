package com.blocwatch.sdk.v1.support;

import java.time.Duration;

/** Sleeper that doesn't sleep. */
public class FakeSleeper implements Sleeper {

  @Override
  public void sleep(Duration duration) throws InterruptedException {
    return;
  }
}
