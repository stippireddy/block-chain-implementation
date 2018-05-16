package rand;

/*
 * The methods for signing data are in the java.security package, so the program imports everything
 * from that package. The program also imports the java.io package, which contains the methods
 * needed to input the file data to be signed.
 */
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

public class GenSig {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: GenSig nameOfFileToSign");
    } else
      try {
        /*
         * Using DSA algorithm for key generation. SUN parameter here implies that the default
         * implementation to be used.
         */
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "SUN");
        /*
         * Using DSA algorithm for key generation. SUN parameter here implies that the default
         * implementation to be used.
         */
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
        /*
         * Once the keyGen object is set up, we can use the generateKeyPair() method to get a public
         * and private key pair.
         */
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();
        Signature dsa = Signature.getInstance("SHA256withDSA", "SUN");
        dsa.initSign(priv);
        FileInputStream fis = new FileInputStream(args[0]);
        BufferedInputStream bufin = new BufferedInputStream(fis);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = bufin.read(buffer)) >= 0) {
          dsa.update(buffer, 0, len);
        } ;
        bufin.close();
        byte[] realSig = dsa.sign();
        /* save the signature in a file */
        FileOutputStream sigfos = new FileOutputStream("sig");
        sigfos.write(realSig);
        sigfos.close();
        /* save the public key in a file */
        byte[] key = pub.getEncoded();
        FileOutputStream keyfos = new FileOutputStream("myPublicKey");
        keyfos.write(key);
        keyfos.close();
      } catch (Exception e) {
        System.err.println("Caught exception " + e.toString());
      }
  }
}