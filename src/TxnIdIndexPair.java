public class TxnIdIndexPair {
  private String txnId;
  private int index;

  @Override
  public String toString() {
    return "[txnId=" + txnId + ", index=" + index + "]";
  }

  public TxnIdIndexPair(String txnId, int index) {
    super();
    this.txnId = txnId;
    this.index = index;
  }

  public String getTxnId() {
    return txnId;
  }

  public int getIndex() {
    return index;
  }
}
