public class UTXO {
  private String user;
  private long amount;
  private boolean isSpent;

  @Override
  public String toString() {
    return "[userName=" + user + ", amount=" + amount + "]";
  }

  public UTXO(String user, long amount) {
    this.user = user;
    this.amount = amount;
    this.isSpent = false;
  }

  public String getUser() {
    return user;
  }

  public long getAmount() {
    return amount;
  }

  public boolean isSpent() {
    return isSpent;
  }

  public void setSpent(boolean isSpent) {
    this.isSpent = isSpent;
  }
}

