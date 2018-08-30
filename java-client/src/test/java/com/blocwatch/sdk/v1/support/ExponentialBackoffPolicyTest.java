package com.blocwatch.sdk.v1.support;

import com.google.common.collect.Range;
import com.google.common.truth.Truth;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExponentialBackoffPolicyTest {

  @Test
  public void testInitialAttempt() {
    ExponentialBackoffPolicy policy = new ExponentialBackoffPolicy.Builder().build();
    Duration backoff = policy.getNextDelay(1); // Attempt #1, not a retry
    Truth.assertThat(backoff).isEquivalentAccordingToCompareTo(Duration.ZERO);
  }

  @Test
  public void testFirstRetry() {
    ExponentialBackoffPolicy policy =
        new ExponentialBackoffPolicy.Builder()
            .setRandomizationFactor(0)
            .setInitialDelayMillis(500)
            .setBase(1.5)
            .build();
    Duration backoff = policy.getNextDelay(2); // Attempt #2, first retry.
    Truth.assertThat(backoff).isIn(Range.closed(Duration.ofMillis(499), Duration.ofMillis(501)));
  }

  @Test
  public void testRandomization() {
    ExponentialBackoffPolicy policy =
        new ExponentialBackoffPolicy.Builder()
            .setRandomizationFactor(0.5)
            .setInitialDelayMillis(500)
            .setBase(1.5)
            .build();
    Duration backoff = policy.getNextDelay(2); // Attempt #2, first retry.
    Truth.assertThat(backoff)
        .isIn(Range.openClosed(Duration.ofMillis(250), Duration.ofMillis(501)));
  }

  @Test
  public void testSecondRetry() {
    ExponentialBackoffPolicy policy =
        new ExponentialBackoffPolicy.Builder()
            .setRandomizationFactor(0)
            .setInitialDelayMillis(500)
            .setBase(1.5)
            .build();
    Duration backoff = policy.getNextDelay(3); // Attempt #3, second retry.
    Truth.assertThat(backoff).isIn(Range.closed(Duration.ofMillis(1249), Duration.ofMillis(1251)));
  }

  @Test
  public void testSecondRetryRandomization() {
    ExponentialBackoffPolicy policy =
        new ExponentialBackoffPolicy.Builder()
            .setRandomizationFactor(0.5)
            .setInitialDelayMillis(500)
            .setBase(1.5)
            .build();
    Duration backoff = policy.getNextDelay(3); // Attempt #3, second retry.
    Truth.assertThat(backoff)
        .isIn(Range.openClosed(Duration.ofMillis(875), Duration.ofMillis(1251)));
  }

  @Test
  public void testMaxDelay() {
    ExponentialBackoffPolicy policy =
        new ExponentialBackoffPolicy.Builder()
            .setRandomizationFactor(0)
            .setInitialDelayMillis(11_000)
            .setMaxDelayMillis(10_000)
            .setBase(1.5)
            .build();
    Duration backoff = policy.getNextDelay(2); // Attempt #2, first retry.
    Truth.assertThat(backoff).isEquivalentAccordingToCompareTo(Duration.ofMillis(10_000));
  }
}
