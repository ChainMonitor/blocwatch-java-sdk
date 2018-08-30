package com.blocwatch.sdk.v1.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@RunWith(JUnit4.class)
public class RetryHttpRequestInterceptorTest {

  @Mock(answer = Answers.RETURNS_SMART_NULLS)
  public ClientHttpResponse clientHttpResponse;

  @Mock(answer = Answers.RETURNS_SMART_NULLS)
  public HttpRequest mockHttpRequest;

  @Mock(answer = Answers.RETURNS_SMART_NULLS)
  public ClientHttpRequestExecution clientHttpRequestExecution;

  @Spy public FakeSleeper fakeSleeper = new FakeSleeper();
  public FakeBackoffPolicy fakeBackoffPolicy = new FakeBackoffPolicy();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFirstAttemptDoesntSleep() throws IOException {
    byte[] postBody = new byte[0];

    RetryHttpRequestInterceptor interceptor =
        new RetryHttpRequestInterceptor(fakeBackoffPolicy, fakeSleeper);

    Mockito.when(
            clientHttpRequestExecution.execute(Mockito.eq(mockHttpRequest), Mockito.eq(postBody)))
        .thenReturn(clientHttpResponse);

    Mockito.when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);

    interceptor.intercept(mockHttpRequest, postBody, clientHttpRequestExecution);

    Mockito.verify(clientHttpRequestExecution, times(1))
        .execute(Mockito.eq(mockHttpRequest), Mockito.eq(postBody));
    Mockito.verifyNoMoreInteractions(clientHttpRequestExecution);
  }

  @Test
  public void testBackoffAfter500() throws IOException, InterruptedException {
    byte[] postBody = new byte[0];

    RetryHttpRequestInterceptor interceptor =
        new RetryHttpRequestInterceptor(fakeBackoffPolicy, fakeSleeper);

    Mockito.when(
            clientHttpRequestExecution.execute(Mockito.eq(mockHttpRequest), Mockito.eq(postBody)))
        .thenReturn(clientHttpResponse);

    // On execution, fail once and then succeed.
    Mockito.when(clientHttpResponse.getStatusCode())
        .thenReturn(HttpStatus.BAD_GATEWAY)
        .thenReturn(HttpStatus.OK);

    interceptor.intercept(mockHttpRequest, postBody, clientHttpRequestExecution);

    // Verify the request is tried twice:
    Mockito.verify(clientHttpRequestExecution, times(2))
        .execute(Mockito.eq(mockHttpRequest), Mockito.eq(postBody));
    Mockito.verifyNoMoreInteractions(clientHttpRequestExecution);

    // Verify sleep is invoked:
    Mockito.verify(fakeSleeper, times(1)).sleep(any(Duration.class));
    Mockito.verifyNoMoreInteractions(fakeSleeper);
  }

  @Test
  public void testNoRetryOnBadRequest() throws IOException {
    byte[] postBody = new byte[0];

    RetryHttpRequestInterceptor interceptor =
        new RetryHttpRequestInterceptor(fakeBackoffPolicy, fakeSleeper);

    Mockito.when(
            clientHttpRequestExecution.execute(Mockito.eq(mockHttpRequest), Mockito.eq(postBody)))
        .thenReturn(clientHttpResponse);

    // On execution, fail once and then succeed.
    Mockito.when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

    interceptor.intercept(mockHttpRequest, postBody, clientHttpRequestExecution);

    // Verify the request is tried twice:
    Mockito.verify(clientHttpRequestExecution, times(1))
        .execute(Mockito.eq(mockHttpRequest), Mockito.eq(postBody));
    Mockito.verifyNoMoreInteractions(clientHttpRequestExecution);
  }
}
