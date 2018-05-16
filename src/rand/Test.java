package rand;

import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

public class Test {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    byte[] test = DatatypeConverter.parseHexBinary(sc.nextLine());
    System.out.println("Testing done");
  }
}
