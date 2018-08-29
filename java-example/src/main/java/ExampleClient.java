import com.blocwatch.client.v1.ApiException;
import com.blocwatch.client.v1.model.bitcoin.BitcoinBlock;
import com.blocwatch.client.v1.model.bitcoin.BitcoinCompareQuery;
import com.blocwatch.client.v1.model.bitcoin.BitcoinQuery;
import com.blocwatch.client.v1.model.bitcoin.BitcoinSearchRequest;
import com.blocwatch.client.v1.model.bitcoin.GetBlockRequest;
import com.blocwatch.client.v1.model.bitcoin.GetBlockResponse;
import com.blocwatch.client.v1.model.bitcoin.GetTransactionRequest;
import com.blocwatch.client.v1.model.bitcoin.GetTransactionResponse;
import com.blocwatch.client.v1.model.bitcoin.ListTransactionsResponse;
import com.blocwatch.sdk.v1.BlocWatchClient;
import java.util.Arrays;

public class ExampleClient {

  public static void main(String[] args) throws ApiException {

    if (args.length < 1) {
      System.err.println("Usage; ExampleClient [access-token]");
      System.err.println(
          "Where [access-token] is a pre-shared access token for BlocWatch services.");
      System.exit(1);
    }

    String accessToken = args[0];

    BlocWatchClient blocWatchClient = new BlocWatchClient();
    blocWatchClient.setAccessToken(accessToken);
    blocWatchClient.setDebugging(true);

    GetBlockResponse response =
        blocWatchClient
            .bitcoinBlocks()
            .getBlock(
                new GetBlockRequest()
                    .id("00000000000000000022b471064a429a743b5549dee8f0e45ff4085369b7ba1e")
                    .addIncludeItem(GetBlockRequest.IncludeEnum.BASIC)
                    .addIncludeItem(GetBlockRequest.IncludeEnum.DETAILS)
                    .addIncludeItem(GetBlockRequest.IncludeEnum.SUMMARY));
    BitcoinBlock block = response.getBlock();
    System.out.println(String.format("Retreived block %s", block));

    GetTransactionResponse txResponse =
        blocWatchClient
            .bitcoinTransactions()
            .getTransaction(
                new GetTransactionRequest()
                    .id("afaed1c9b75b58a372acd950f16ac0fb5697356dc90850f93f861d80eef88e39")
                    .addIncludeItem(GetTransactionRequest.IncludeEnum.BASIC)
                    .addIncludeItem(GetTransactionRequest.IncludeEnum.DETAILS)
                    .addIncludeItem(GetTransactionRequest.IncludeEnum.SUMMARY));
    System.out.println(String.format("Retrieved txn %s", txResponse.getTransaction()));

    BitcoinQuery query =
        new BitcoinQuery()
            .compareQuery(
                new BitcoinCompareQuery()
                    .field(BitcoinCompareQuery.FieldEnum.INPUTS)
                    .op(BitcoinCompareQuery.OpEnum.GREATER_THAN)
                    .value("10"));
    BitcoinSearchRequest request = new BitcoinSearchRequest().query(query);
    ListTransactionsResponse txnSearchResponse =
        blocWatchClient
            .bitcoinTransactions()
            .searchTransactions(request, Arrays.asList("basic", "summary"), 25, "");
    System.out.println(String.format("Retreived txnSearchResponse %s", txnSearchResponse));
  }
}
