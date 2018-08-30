package com.blocwatch.sdk.v1.support;

import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/** Interceptor that will retry failed requests. */
public class RetryHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  private static final int MAX_RETRIES = 10;
  private static final Logger logger = LoggerFactory.getLogger(RetryHttpRequestInterceptor.class);
  private final BackoffPolicy backoffPolicy;
  private final Sleeper sleeper;

  public RetryHttpRequestInterceptor(BackoffPolicy backoffPolicy, Sleeper sleeper) {
    this.backoffPolicy = backoffPolicy;
    this.sleeper = sleeper;
  }

  @Override
  public ClientHttpResponse intercept(
      HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution)
      throws IOException {

    // How many retries have been attempted so far:
    int retryCount = 0;
    // Whether the current attempt should be retried.
    boolean shouldRetry;
    // Whether the current attempt can be retried.
    boolean canRetry;
    // The exception from the last retry attempt, if any.
    Exception exception;
    ClientHttpResponse response = null;
    do {
      canRetry = retryCount < MAX_RETRIES;
      // Reset shouldRetry until we encounter an error that might be fixed with retry:
      shouldRetry = false;
      exception = null;
      if (retryCount > 0) {
        logger.trace("Running backoff before retry.");
        Duration delay = backoffPolicy.getNextDelay(retryCount + 1 /* attempt number */);
        try {
          sleeper.sleep(delay);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IOException("Interrupted while sleeping during retry.", e);
        }
      }
      try {
        logger.trace("Sending request to {}", httpRequest.getURI());
        response = clientHttpRequestExecution.execute(httpRequest, body);
        HttpStatus responseCode = response.getStatusCode();
        if (responseCode.is2xxSuccessful()) {
          logger.trace("Successfully requested {}", httpRequest.getURI());
          return response;
        }

        // A non-200 error has been returned, attempt to handle the error.
        if (canRetry(responseCode)) {
          shouldRetry = canRetry;
          logger.info(
              "Will retry request to {} after error response {}",
              httpRequest.getURI(),
              responseCode);
        }
      } catch (IOException exc) {
        if (canRetry) {
          shouldRetry = canRetry;
          logger.info("Retrying request after exception.", exc);
        }
        exception = exc;
      }

      retryCount += 1;

      if (shouldRetry || exception != null) {
        if (response != null) {
          // We aren't going to return this response (On retry, we'll throw it away, and if we're
          // not going to retry then we have an exception to throw). Close this response to
          // prevent leaking connections.
          response.close();
        }
      }
    } while (shouldRetry);

    logger.info(
        "Exhausted retries ({}) or cannot retry {}, last resp: <{}>",
        retryCount,
        httpRequest.getURI(),
        response);

    if (exception != null) {
      throw new IOException(exception);
    }
    return response;
  }

  private boolean canRetry(HttpStatus statusCode) throws IOException {
    if (statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode.is5xxServerError()) {
      return true;
    }
    return false;
  }
}
