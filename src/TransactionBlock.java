import java.util.LinkedHashMap;

public class TransactionBlock {

  private LinkedHashMap<String, Transaction> blockTransactions;

  public TransactionBlock(LinkedHashMap<String, Transaction> blockTransactions) {
    this.blockTransactions = blockTransactions;
  }

  public LinkedHashMap<String, Transaction> getTxns() {
    return blockTransactions;
  }

  @Override
  public String toString() {
    return "[Size: " + blockTransactions.size() + ", Transactions: " + blockTransactions + "]";
  }
}
