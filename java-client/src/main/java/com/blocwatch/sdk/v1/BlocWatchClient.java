package com.blocwatch.sdk.v1;

import com.blocwatch.client.v1.ApiClient;
import com.blocwatch.client.v1.api.bitcoin.BitcoinAddressesApi;
import com.blocwatch.client.v1.api.bitcoin.BitcoinBlocksApi;
import com.blocwatch.client.v1.api.bitcoin.BitcoinTransactionIteratorsApi;
import com.blocwatch.client.v1.api.bitcoin.BitcoinTransactionsApi;

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
    this(new ApiClient());
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

  public void setDebugging(boolean debugging) {
    apiClient.setDebugging(debugging);
  }

  public boolean isDebugging() {
    return apiClient.isDebugging();
  }

  public ApiClient setBasePath(String basePath) {
    return apiClient.setBasePath(basePath);
  }
}
