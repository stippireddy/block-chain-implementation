import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class txblk {

  public static void main(String[] args) {
    Ledger l = new Ledger();
    Scanner sc = new Scanner(System.in);
    while (!l.isExit()) {
      if (l.isInteractive()) {
        System.out.println("[B]alance");
        System.out.println("[C]heck transaction signature");
        System.out.println("[D]ump");
        System.out.println("[E]xit");
        System.out.println("[F]ile");
        System.out.println("[H]elp");
        System.out.println("[I]nteractive");
        System.out.println("[P]rint");
        System.out.println("[O]utput transaction block");
        System.out.println("[R]ead key file");
        System.out.println("[T]ransaction");
        System.out.println("[V]erbose");
        System.out.println("[W]ipe");
        System.out.print("Select a command: ");
      }
      String s = sc.nextLine();
      s = s.toLowerCase();
      switch (s) {
        case "f":
        case "file":
          System.out.print("Supply file name: ");
          String fileName = sc.nextLine();
          try {
            FileReader fileReader = new FileReader(fileName);
            Scanner fileScanner = new Scanner(fileReader);
            while (fileScanner.hasNext()) {
              l.validateAndAddTransaction(fileScanner.nextLine(), fileScanner.nextLine());
            }
            fileScanner.close();
          } catch (FileNotFoundException e) {
            System.err.println("Error: file " + fileName + " cannot be opened for reading");
          }
          break;

        case "t":
        case "transaction":
          System.out.print("Enter Transaction: ");
          String transaction = sc.nextLine();
          String signature = sc.nextLine();
          l.validateAndAddTransaction(transaction, signature);
          break;

        case "p":
        case "print":
          l.printLedger();
          break;

        case "h":
        case "help":
          System.out.println(
              "[F]ile:  Supply filename:<infilename>.  Read in a file of transactions. Any invalid transaction shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be 'Error: file <infilename> cannot be opened for reading' on a single line, where <infilename> is the name provided as additional command input.  \r\n"
                  + "\r\n"
                  + "[T]ransaction: Supply Transaction:<see format below>   Read in a single transaction in the format shown below.  It shall be checked for validity against the ledger and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the transaction number followed by a colon, a space, and the reason it is invalid on a single line.\r\n"
                  + "\r\n" + "[E]xit:  Quit the program\r\n" + "\r\n"
                  + "[P]rint:  Print current ledger (all transactions in the order they were added) to stdout in the transaction format given below, one transaction per line.\r\n"
                  + "\r\n" + "[H]elp:  Command Summary\r\n" + "\r\n"
                  + "[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be 'Error: file <outfilename> cannot be opened for writing' on a single line, where <outfilename> is the name provided as additional command input. \r\n"
                  + "\r\n" + "[W]ipe:  Wipe the entire ledger to start fresh.\r\n" + "\r\n"
                  + "[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\r\n"
                  + "\r\n"
                  + "[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each transaction number as it is read in, followed by a colon, a space, and the result ('good' or 'bad'). \r\n"
                  + "\r\n"
                  + "[B]alance:  Supply username: (e.g. Alice).  This command prints the current balance of a user.    \r\n"
                  + "\r\n"
                  + "[O]utput transaction block: collect all correctly signed transactions that have not been output in a previous transaction block and output them as a transaction block.  This outputs the current block only.  This includes outputting a line with a single integer indicating the number of signed transactions that follow, followed by those transactions. \r\n"
                  + "\r\n"
                  + "[C]heck transaction signature: supply <transactionID>. The signature of the signed transaction (in the two-line format given above) shall be checked. \r\n"
                  + "\r\n"
                  + "[R]ead key file: supply <account name> <keyfilename>. <account name is the name of the account associated with the key (e.g., Alice, Bob, Gopesh). <keyfilename> is the name of the file containing the public key associated with the account named. \r\n"
                  + "\r\n" + "Format of Transactions:\r\n"
                  + "<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N \r\n"
                  + "Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. \r\n");
          break;

        case "d":
        case "dump":
          System.out.print("Supply file name: ");
          String outputFileName = sc.nextLine();
          try {
            FileWriter fw = new FileWriter(new File(outputFileName));
            l.dumpLedger(fw);
            fw.close();
          } catch (IOException e) {
            System.err.println("Error: file " + outputFileName + " cannot be opened for writing");
            e.printStackTrace();
          }
          break;

        case "w":
        case "wipe":
          l.wipeLedger();
          break;

        case "i":
        case "interactive":
          l.setInteractive(!l.isInteractive());
          break;

        case "v":
        case "verbose":
          l.setVerbose(!l.isVerbose());
          break;

        case "b":
        case "balance":
          System.out.print("Enter User: ");
          String user = sc.nextLine();
          if (l.getUserToBalanceMap().containsKey(user)) {
            System.out.println(user + " has " + l.getUserToBalanceMap().get(user));
          } else {
            System.out.println(user + " does not exist on this ledger");
          }
          break;

        case "e":
        case "exit":
          l.setExit(true);
          System.out.println("Good-bye");
          break;

        case "o":
        case "output transaction block":
          l.outputTransactionBlock();
          break;

        case "c":
        case "check transaction signature":
          System.out.print("Enter Transaction ID: ");
          String txnID = sc.nextLine();
          l.checkTransactionSignature(txnID);
          break;

        case "r":
        case "read key file":
          System.out.print("Enter account name and public key file: ");
          String[] accountPublicKey = sc.nextLine().split(" ");
          if (accountPublicKey == null || accountPublicKey.length != 2) {
            System.out.println(
                "Please enter a valid input in the form of <AccountName> <PublicKeyFile>.");
          } else {
            l.readAndStorePublicKeyFromFile(accountPublicKey[0], accountPublicKey[1]);
          }
          break;

        default:
          System.out.println("Please enter a valid command.");
          break;
      }
    }
    sc.close();
  }
}
