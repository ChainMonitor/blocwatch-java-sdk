package com.blocwatch.sdk.v1;

import com.blocwatch.client.v1.ApiClient;
import com.blocwatch.client.v1.api.bitcoin.BitcoinAddressesApi;
import com.blocwatch.client.v1.api.bitcoin.BitcoinBlocksApi;
import com.blocwatch.client.v1.api.bitcoin.BitcoinTransactionIteratorsApi;
import com.blocwatch.client.v1.api.bitcoin.BitcoinTransactionsApi;
import com.blocwatch.sdk.v1.support.DefaultSleeper;
import com.blocwatch.sdk.v1.support.ExponentialBackoffPolicy;
import com.blocwatch.sdk.v1.support.RetryHttpRequestInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Client for BlockWatch APIs.
 *
 * <p>This client is intended to act as a simplifying abstraction over the BlockWatch APIs. All
 * functionality provided here-in can be accessed via the generated classes that this client wraps.
 */
public class BlocWatchClient {

  private static final String PROD_BLOCWATCH_API = "https://api.blocwatch.com";

  private final ApiClient apiClient;

  public BlocWatchClient() {
    this(new ApiClient(buildRestTemplate()));
    this.setBasePath(PROD_BLOCWATCH_API);
  }

  public BlocWatchClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public BitcoinAddressesApi bitcoinAddreses() {
    return new BitcoinAddressesApi(apiClient);
  }

  public BitcoinBlocksApi bitcoinBlocks() {
    return new BitcoinBlocksApi(apiClient);
  }

  public BitcoinTransactionsApi bitcoinTransactions() {
    return new BitcoinTransactionsApi(apiClient);
  }

  public BitcoinTransactionIteratorsApi bitcoinTransactionIterators() {
    return new BitcoinTransactionIteratorsApi(apiClient);
  }

  public void setAccessToken(String accessToken) {
    apiClient.setAccessToken(accessToken);
  }

  public ApiClient setUserAgent(String userAgent) {
    return apiClient.setUserAgent(userAgent);
  }

  public ApiClient addDefaultHeader(String name, String value) {
    return apiClient.addDefaultHeader(name, value);
  }

  public boolean isDebugging() {
    return apiClient.isDebugging();
  }

  public void setDebugging(boolean debugging) {
    apiClient.setDebugging(debugging);
  }

  public ApiClient setBasePath(String basePath) {
    return apiClient.setBasePath(basePath);
  }

  private static RestTemplate buildRestTemplate() {
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    RestTemplate restTemplate = new RestTemplate();

    // Allow debugging to read the response stream a second time (causes responeses to be buffered
    // in memory):
    restTemplate.setRequestFactory(
        new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));

    // The retry interceptor doesn't pay well with the default request factory. The retry
    // interceptor is required to be the last interceptor in the chain.
    interceptors.add(
        new RetryHttpRequestInterceptor(ExponentialBackoffPolicy.DEFAULT, new DefaultSleeper()));
    restTemplate.setInterceptors(interceptors);

    // Setup JSON handling:
    restTemplate.setMessageConverters(buildMessageConverters());
    return restTemplate;
  }

  private static ObjectMapper buildObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    return mapper;
  }

  private static List<HttpMessageConverter<?>> buildMessageConverters() {
    MappingJackson2HttpMessageConverter messageConverter =
        new MappingJackson2HttpMessageConverter(buildObjectMapper());
    ArrayList<HttpMessageConverter<?>> result = new ArrayList<>();
    result.add(messageConverter);
    return result;
  }
}
