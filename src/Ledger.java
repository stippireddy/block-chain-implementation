import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

public class Ledger {

  private HashMap<String, Long> userToBalanceMap;
  private LinkedHashMap<String, Transaction> txnChain;
  private boolean isGenesisTransaction;
  private boolean isExit;
  private boolean isInteractive;
  private boolean isVerbose;
  private LinkedHashMap<String, Transaction> txnPool;
  private LinkedList<TransactionBlock> blockChain;
  private HashMap<String, PublicKey> userPublicKeyMap;
  private HashSet<String> userSet;

  public Ledger() {
    isGenesisTransaction = true;
    isExit = false;
    isInteractive = false;
    isVerbose = false;
    userToBalanceMap = new HashMap<>();
    txnChain = new LinkedHashMap<>();
    txnPool = new LinkedHashMap<>();
    blockChain = new LinkedList<>();
    userPublicKeyMap = new HashMap<>();
    userSet = new HashSet<>();
  }

  public void dumpLedger(FileWriter fw) throws IOException {
    if (txnPool.isEmpty()) {
      System.out.println("The ledger is currently empty");
    } else {
      for (Entry<String, Transaction> entry : txnPool.entrySet()) {
        fw.write(entry.getValue().getTransactionString());
        fw.write(System.lineSeparator());
        fw.write(DatatypeConverter.printHexBinary(entry.getValue().getSignature()));
        fw.write(System.lineSeparator());
      }
    }
  }

  public void printLedger() {
    if (txnPool.isEmpty()) {
      System.out.println("The ledger is currently empty");
    } else {
      for (Entry<String, Transaction> entry : txnPool.entrySet()) {
        System.out.println(entry.getValue().getTransactionString());
        System.out.println(DatatypeConverter.printHexBinary(entry.getValue().getSignature()));
      }
    }
  }

  public void validateAndAddTransaction(String inputTxn, String signature) {
    inputTxn = inputTxn.replaceAll(" ", "").replaceAll(";", "; ").replaceAll(",", ", ");
    int indexofFirstSemiColon = inputTxn.indexOf("; ");
    if (indexofFirstSemiColon < 0) {
      System.out.println(
          "Invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
      return;
    }
    String inputTxnID = inputTxn.substring(0, indexofFirstSemiColon);
    String givenTxnID = inputTxnID;
    if (!isValidTxnId(givenTxnID)) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID
          + ": transaction Id should be 8 characters long and contain either characters between a and f or digits");
      return;
    }
    if (indexofFirstSemiColon + 2 >= inputTxn.length()) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": transaction is not formatted correctly");
      if (isVerbose) {
        System.err.println(givenTxnID
            + " : Sorry, invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
      }
      return;
    }
    try {
      String hashedTxnId = isValidSHA1TxnId(inputTxn, indexofFirstSemiColon);
      if (!hashedTxnId.equals(inputTxnID)) {
        if (isVerbose) {
          System.err
              .println(givenTxnID + " : The transaction ID you have input is invalid. Resetting to "
                  + hashedTxnId + " to match the correct SHA1 hash.");
        }
        inputTxn =
            hashedTxnId + "; " + inputTxn.substring(indexofFirstSemiColon + 2, inputTxn.length());
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    String[] txnSplitArray = inputTxn.split(";");
    if (txnSplitArray.length < 5) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": transaction is not formatted correctly");
      if (isVerbose) {
        System.err.println(givenTxnID
            + " : Sorry, invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
      }
      return;
    }
    inputTxnID = txnSplitArray[0].trim();
    int inputSize = Integer.parseInt(txnSplitArray[1].trim());
    if ((isGenesisTransaction && inputSize != 0)) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(
          givenTxnID + ": the number of input UTXOs for a genesis transaction should be 0");
      return;
    }
    if (!isGenesisTransaction && inputSize < 1) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID
          + ": the number of input UTXOs for a non-genesis transaction should be greater than 0");
      return;
    }
    ArrayList<TxnIdIndexPair> inputUTXOs = new ArrayList<>();
    txnSplitArray[2] = txnSplitArray[2].trim();
    int left = 0, right = 0;
    while (right < txnSplitArray[2].length()) {
      if (txnSplitArray[2].charAt(left) != '(') {
        System.err.println(
            "Sorry, invalid transaction. Please use the correct format to input your transaction. Example transaction: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
        return;
      } else {
        left++;
        right++;
        while (right < txnSplitArray[2].length() && txnSplitArray[2].charAt(right) != ',') {
          right++;
        }
        String txnId = txnSplitArray[2].substring(left, right);
        right++;
        left = right;
        while (right < txnSplitArray[2].length() && txnSplitArray[2].charAt(right) != ')') {
          right++;
        }
        int amount = Integer.parseInt(txnSplitArray[2].substring(left, right).trim());
        inputUTXOs.add(new TxnIdIndexPair(txnId, amount));
        right++;
        left = right;
      }
    }
    if (inputSize != inputUTXOs.size()) {
      System.out.println(givenTxnID + ": bad");
      System.err
          .println(givenTxnID + ": Sorry, invalid transaction. Your transaction must contain ."
              + inputSize + " pairs of input UTXOs.");
      return;
    }
    int outputSize = Integer.parseInt(txnSplitArray[3].trim());
    if (outputSize < 1) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": the number of output UTXOs should be greater than 0");
      return;
    }
    ArrayList<UTXO> outputUTXOs = new ArrayList<>();
    txnSplitArray[4] = txnSplitArray[4].trim();
    left = 0;
    right = 0;
    while (right < txnSplitArray[4].length()) {
      if (txnSplitArray[4].charAt(left) != '(') {
        System.err.println(
            "Sorry, invalid transaction. Please use the correct format to input your transaction.");
        return;
      } else {
        left++;
        right++;
        while (right < txnSplitArray[4].length() && txnSplitArray[4].charAt(right) != ',') {
          right++;
        }
        String txnId = txnSplitArray[4].substring(left, right);
        right++;
        left = right;
        while (right < txnSplitArray[4].length() && txnSplitArray[4].charAt(right) != ')') {
          right++;
        }
        int amount = Integer.parseInt(txnSplitArray[4].substring(left, right).trim());
        outputUTXOs.add(new UTXO(txnId, amount));
        right++;
        left = right;
      }
    }
    if (outputSize != outputUTXOs.size()) {
      System.out.println(givenTxnID + ": bad");
      System.err.println(givenTxnID + ": Your transaction must contain ." + outputSize
          + " pairs of output UTXOs.");
      return;
    }
    long inputAmount = 0;
    for (TxnIdIndexPair t : inputUTXOs) {
      if (txnChain.containsKey(t.getTxnId())) {
        if (!txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex()).isSpent()) {
          inputAmount +=
              txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex()).getAmount();
        } else {
          System.out.println(givenTxnID + ": bad");
          System.err
              .println(givenTxnID + ": Invalid transaction. One of the input UTXOs already spent.");
          if (isVerbose) {
            System.err.println(givenTxnID + ": The UTXO with id " + t.getTxnId() + " and index "
                + t.getIndex() + " was already spent.");
          }
          return;
        }
      } else {
        System.out.println(givenTxnID + ": bad");
        System.err.println(givenTxnID + ": The input transaction with id " + t.getTxnId()
            + " does not exist in the ledger");
        return;
      }
    }
    String user = null;
    if (!isGenesisTransaction) {
      TxnIdIndexPair firstInput = inputUTXOs.get(0);
      UTXO currentTransaction =
          txnChain.get(firstInput.getTxnId()).getOutputTransactions().get(firstInput.getIndex());
      user = currentTransaction.getUser();
      for (int i = 1; i < inputUTXOs.size(); i++) {
        TxnIdIndexPair currentInput = inputUTXOs.get(i);
        currentTransaction = txnChain.get(currentInput.getTxnId()).getOutputTransactions()
            .get(currentInput.getIndex());
        if (!currentTransaction.getUser().equals(user)) {
          System.out.println(givenTxnID + ": bad");
          System.err
              .println(givenTxnID + ": All the input UTXOs should belong to the same account");
          return;
        }
      }
    } else {
      user = outputUTXOs.get(0).getUser();
    }
    long outputAmount = 0;
    for (UTXO t : outputUTXOs) {
      outputAmount += t.getAmount();
    }
    if (isGenesisTransaction || inputAmount == outputAmount) {
      if (isGenesisTransaction) {
        isGenesisTransaction = false;
      }
      Transaction newTxn = new Transaction(inputTxn, inputTxnID, inputSize, outputSize, inputUTXOs,
          outputUTXOs, user, DatatypeConverter.parseHexBinary(signature));
      txnChain.put(inputTxnID, newTxn);
      txnPool.put(inputTxnID, newTxn);
      userSet.add(user);
      for (UTXO t : outputUTXOs) {
        userSet.add(t.getUser());
      }
      System.out.println(inputTxnID + ": good");
    } else {
      System.out.println(givenTxnID + ": bad");
      System.err.println(
          givenTxnID + ": Insufficient balance in the sender side to complete this transaction.");
      return;
    }
  }

  public void checkTransactionSignature(String inputTxnID) {
    if (!txnChain.containsKey(inputTxnID)) {
      System.out.println(inputTxnID + ": bad");
      System.err.println(inputTxnID + ": A transaction with this id could not be found.");
      return;
    }
    Transaction txn = txnChain.get(inputTxnID);
    byte[] signature = txn.getSignature();
    if (signature == null || signature.length == 0) {
      System.out.println(inputTxnID + ": bad");
      System.err.println(inputTxnID + ": There is no signature attached to this transaction.");
      return;
    }
    if (!userPublicKeyMap.containsKey(txn.getUser())) {
      System.out.println(inputTxnID + ": bad");
      System.err.println(inputTxnID
          + ": Can't find a public key associated with the user of this transaction. Please use [R]ead key file command to attach a public key for this user.");
      return;
    }
    PublicKey userPublicKey = userPublicKeyMap.get(txn.getUser());
    try {
      Signature sg = Signature.getInstance("SHA256withRSA");
      sg.initVerify(userPublicKey);
      // read data file into signature instance
      byte[] data =
          (txn.getTransactionString().substring(txn.getTransactionString().indexOf(";") + 2,
              txn.getTransactionString().length()) + "\n").getBytes();
      sg.update(data);
      if (sg.verify(signature)) {
        System.out.println("OK");
        return;
      } else {
        System.out.println("Bad");
        System.err.println(inputTxnID + ": Signature validation failed.");
        return;
      }
    } catch (NoSuchAlgorithmException e) {
      System.out.println("Bad");
      System.err.println("Error: transaction not signed due to internal Java error.");
    } catch (InvalidKeyException e) {
      System.out.println("Bad");
      System.err.println("Error: transaction not signed due to corrupt key mapped to the user.");
    } catch (SignatureException e) {
      System.out.println("Bad");
      System.err.println("Error: transaction not signed due to internal Java error.");
    }
  }

  private String isValidSHA1TxnId(String input, int index) throws NoSuchAlgorithmException {
    String hashInput = input.substring(index + 2, input.length()) + "\n";
    MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
    byte[] result = mDigest.digest(hashInput.getBytes());
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < result.length; i++) {
      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.substring(0, 8).toLowerCase();
  }

  private boolean isValidTxnId(String s) {
    s = s.toLowerCase();
    if (s == null || s.length() != 8) {
      return false;
    }
    for (char c : s.toCharArray()) {
      if (Character.isLetter(c)) {
        if (c < 'a' || c > 'f') {
          return false;
        }
      }
    }
    return true;
  }

  public HashMap<String, Long> getUserToBalanceMap() {
    return userToBalanceMap;
  }

  public LinkedHashMap<String, Transaction> getTxnChain() {
    return txnChain;
  }

  public boolean isExit() {
    return isExit;
  }

  public void setExit(boolean isExit) {
    this.isExit = isExit;
  }

  public boolean isInteractive() {
    return isInteractive;
  }

  public void setInteractive(boolean isInteractive) {
    this.isInteractive = isInteractive;
  }

  public boolean isVerbose() {
    return isVerbose;
  }

  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  private TransactionBlock createBlockAndUpdateBalances() {
    Iterator<Entry<String, Transaction>> iterator = txnPool.entrySet().iterator();
    LinkedHashMap<String, Transaction> blockEntries = new LinkedHashMap<>();
    while (iterator.hasNext()) {
      Transaction currentTxn = txnPool.get(iterator.next().getKey());
      boolean validTxn = true;
      ArrayList<TxnIdIndexPair> inputTxns = currentTxn.getInputTransactions();
      for (TxnIdIndexPair t : inputTxns) {
        if (txnPool.containsKey(t.getTxnId())) {
          validTxn = false;
          break;
        }
      }
      if (validTxn) {
        if (hasValidSignature(currentTxn)) {
          long inputAmount = 0;
          for (TxnIdIndexPair t : inputTxns) {
            if (txnChain.containsKey(t.getTxnId())) {
              if (!txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex()).isSpent()) {
                inputAmount += txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex())
                    .getAmount();
              } else {
                validTxn = false;
                break;
              }
            } else {
              validTxn = false;
              break;
            }
          }
          long outputAmount = 0;
          ArrayList<UTXO> outputUTXOs = currentTxn.getOutputTransactions();
          for (UTXO t : outputUTXOs) {
            outputAmount += t.getAmount();
          }
          if (validTxn && (inputTxns.size() == 0 || inputAmount == outputAmount)) {
            for (TxnIdIndexPair t : inputTxns) {
              UTXO currentTransaction =
                  txnChain.get(t.getTxnId()).getOutputTransactions().get(t.getIndex());
              inputAmount += currentTransaction.getAmount();
              userToBalanceMap.put(currentTransaction.getUser(),
                  userToBalanceMap.get(currentTransaction.getUser())
                      - currentTransaction.getAmount());
              currentTransaction.setSpent(true);
            }

            for (UTXO t : outputUTXOs) {
              if (userToBalanceMap.containsKey(t.getUser())) {
                userToBalanceMap.put(t.getUser(),
                    userToBalanceMap.get(t.getUser()) + t.getAmount());
              } else {
                userToBalanceMap.put(t.getUser(), t.getAmount());
              }
            }
            blockEntries.put(currentTxn.getTransactionID(), currentTxn);
            iterator.remove();
          }
        }
      }
    }
    TransactionBlock newBlock = null;
    if (blockEntries.size() != 0) {
      newBlock = new TransactionBlock(blockEntries);
      blockChain.addFirst(newBlock);
    }
    return newBlock;
  }

  public void outputTransactionBlock() {
    if (txnPool.isEmpty()) {
      System.out.println("No available valid transactions to create a block.");
      return;
    }
    TransactionBlock txnBlock = createBlockAndUpdateBalances();
    if (txnBlock == null) {
      System.out.println("No available valid transactions to create a block.");
      return;
    }
    LinkedHashMap<String, Transaction> txns = txnBlock.getTxns();
    System.out.println(txns.size());
    for (Entry<String, Transaction> s : txns.entrySet()) {
      System.out.println(s.getValue().getTransactionString());
      System.out.println(DatatypeConverter.printHexBinary(s.getValue().getSignature()));
    }
  }

  public void wipeLedger() {
    for (String key : txnPool.keySet()) {
      txnChain.remove(key);
    }
    txnPool = new LinkedHashMap<>();
    if (blockChain.size() == 0) {
      isGenesisTransaction = true;
    }
  }

  public void readAndStorePublicKeyFromFile(String user, String fileName) {
    if (!userSet.contains(user)) {
      System.out
          .println("Error: Public key for account name <" + user + "> <" + fileName + "> not read");
      System.err.println("No user found matching the username <" + user
          + ">. Please note that the user name is case-sensitive.");
      return;
    }
    if (userPublicKeyMap.containsKey(user)) {
      System.out.println("Warning: Public key for account name <" + user
          + "> already exists. Overriding with the new public key.");
    }
    try {
      String publicKeyContent =
          new String(Files.readAllBytes(FileSystems.getDefault().getPath(fileName)));
      publicKeyContent = publicKeyContent.replaceAll("\\n", "")
          .replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");;
      // Initializing a key factory to generate public and private keys from files
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      // initializing key file input stream to read the files containing keys
      PublicKey publicKeyFromFile = keyFactory
          .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent)));
      userPublicKeyMap.put(user, publicKeyFromFile);
      System.out.println(
          "Success: Public key for account name <" + user + "> from file <" + fileName + "> read");
    } catch (NoSuchAlgorithmException e) {
      System.out.println("Error: Public key for account name <" + user + "> from file <" + fileName
          + "> could not be read");
      System.err.println("Error: Internal Java error while reading the key.");
    } catch (FileNotFoundException e) {
      System.out.println("Error: Public key for account name <" + user + "> from file <" + fileName
          + "> could not be read");
      System.err.println("Error: File with name <" + fileName + "> could not be found.");
    } catch (IOException e) {
      System.out.println("Error: Public key for account name <" + user + "> from file <" + fileName
          + "> could not be read");
      System.err.println("Error: File with name <" + fileName + "> could not be opened.");
    } catch (InvalidKeySpecException e) {
      System.out.println("Error: Public key for account name <" + user + "> from file <" + fileName
          + "> could not be read");
      System.err.println("Error: Key is not as per desired specification.");
    }
  }

  private boolean hasValidSignature(Transaction txn) {
    byte[] signature = txn.getSignature();
    if (signature == null || signature.length == 0) {
      return false;
    }
    if (!userPublicKeyMap.containsKey(txn.getUser())) {
      System.err.println(txn.getTransactionID()
          + ": Can't find a public key associated with the user <" + txn.getUser()
          + ">. Please use [R]ead key file command to attach a public key for this user.");
      return false;
    }
    PublicKey userPublicKey = userPublicKeyMap.get(txn.getUser());
    try {
      Signature sg = Signature.getInstance("SHA256withRSA");
      sg.initVerify(userPublicKey);
      // read data file into signature instance
      byte[] data =
          (txn.getTransactionString().substring(txn.getTransactionString().indexOf(";") + 2,
              txn.getTransactionString().length()) + "\n").getBytes();
      sg.update(data);
      if (sg.verify(signature)) {
        return true;
      } else {
        return false;
      }
    } catch (NoSuchAlgorithmException e) {
      return false;
    } catch (InvalidKeyException e) {
      return false;
    } catch (SignatureException e) {
      return false;
    }
  }
}
