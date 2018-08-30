package com.blocwatch.sdk.v1.support;

import java.time.Duration;

/** Interface that abstracts a thread.sleep call. */
public interface Sleeper {
  /** Pause the current thread for the given duration. */
  void sleep(Duration duration) throws InterruptedException;
}
