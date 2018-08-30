package com.blocwatch.sdk.v1.support;

import java.time.Duration;

/** Policy for exponential backoff. */
public class ExponentialBackoffPolicy implements BackoffPolicy {

  /** Default exponential backoff policy. */
  public static ExponentialBackoffPolicy DEFAULT =
      new Builder()
          .setBase(1.5)
          .setInitialDelayMillis(500)
          .setRandomizationFactor(0.5)
          .setMaxDelayMillis(10_000)
          .build();

  private final double base;
  private final int initialDelayMillis;
  private final double randomizationFactor;
  private final int maxDelayMillis;

  private ExponentialBackoffPolicy(Builder builder) {
    this.base = builder.base;
    this.initialDelayMillis = builder.initialDelayMillis;
    this.randomizationFactor = builder.randomizationFactor;
    this.maxDelayMillis = builder.maxDelayMillis;
  }

  @Override
  public Duration getNextDelay(int attempt) {
    if (attempt <= 1) {
      return Duration.ZERO;
    }

    // Factor which will be used to multiply initialDelayMillis:
    double factor = 0;

    if (attempt > 2) {
      factor = Math.pow(base, attempt - 2);
    }

    // Compute a randomized next delay.
    double delayMillis =
        (factor * initialDelayMillis)
            // Add the initialDelayMillis multiplied by a number between randomizationFactor and 1:
            + ((1 - (Math.random() * randomizationFactor)) * initialDelayMillis);

    // Trim delay to the maximum delay:
    if (delayMillis > maxDelayMillis) {
      delayMillis = maxDelayMillis;
    }

    return Duration.ofMillis((long) delayMillis);
  }

  /** Builder of ExponentialBackoffPolicy objects. */
  public static class Builder {
    private double base = 2.0;
    private int initialDelayMillis = 100;
    private double randomizationFactor = 0.5;
    private int maxDelayMillis = 10000;
    private int maxAttempts = 10;

    public double getBase() {
      return base;
    }

    public Builder setBase(double base) {
      this.base = base;
      return this;
    }

    public int getInitialDelayMillis() {
      return initialDelayMillis;
    }

    public Builder setInitialDelayMillis(int initialDelayMillis) {
      this.initialDelayMillis = initialDelayMillis;
      return this;
    }

    public double getRandomizationFactor() {
      return randomizationFactor;
    }

    public Builder setRandomizationFactor(double randomizationFactor) {
      this.randomizationFactor = randomizationFactor;
      return this;
    }

    public ExponentialBackoffPolicy build() {
      return new ExponentialBackoffPolicy(this);
    }

    public int getMaxDelayMillis() {
      return maxDelayMillis;
    }

    public Builder setMaxDelayMillis(int maxDelayMillis) {
      this.maxDelayMillis = maxDelayMillis;
      return this;
    }
  }
}
