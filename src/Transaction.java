import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class Transaction {
  private String transactionID;
  private int inputSize;
  private int outputSize;
  private ArrayList<TxnIdIndexPair> inputTransactions;
  private ArrayList<UTXO> outputTransactions;
  private String transactionString;
  private byte[] signature;
  private String user;

  public byte[] getSignature() {
    return signature;
  }

  public void setSignature(byte[] signature) {
    this.signature = signature;
  }

  public String getTransactionString() {
    return transactionString;
  }

  public String getTransactionID() {
    return transactionID;
  }

  public int getInputSize() {
    return inputSize;
  }

  public int getOutputSize() {
    return outputSize;
  }

  public ArrayList<TxnIdIndexPair> getInputTransactions() {
    return inputTransactions;
  }

  public String getUser() {
    return user;
  }

  public ArrayList<UTXO> getOutputTransactions() {
    return outputTransactions;
  }

  public boolean isSigned() {
    return signature != null;
  }

  public Transaction(String transactionString, String txnId, int inputSize, int outputSize,
      ArrayList<TxnIdIndexPair> inputs, ArrayList<UTXO> outputs, String user, byte[] signature) {
    this.transactionString = transactionString;
    this.transactionID = txnId;
    this.inputSize = inputSize;
    this.outputSize = outputSize;
    this.inputTransactions = inputs;
    this.outputTransactions = outputs;
    this.user = user;
    this.signature = signature;
  }

  @Override
  public String toString() {
    return "[" + transactionString + ", " + DatatypeConverter.printHexBinary(signature) + "]";
  }
}
