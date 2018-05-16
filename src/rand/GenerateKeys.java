package rand;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class GenerateKeys {
	public static boolean generate(String user) throws InvalidKeySpecException {
		try {
			/*
			 * Using DSA algorithm for key generation. SUN parameter here implies that the
			 * default implementation to be used.
			 */
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			// Creating key pair
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey generatedPrivateKey = pair.getPrivate();
			PublicKey generatedPublicKey = pair.getPublic();
			FileOutputStream keyfos = new FileOutputStream(user + "_public_key");
			// encoding the public key into Base64 to write to file
			keyfos.write(Base64.getEncoder().encode(generatedPublicKey.getEncoded()));
			keyfos.close();
			// reusing the same key file output stream
			keyfos = new FileOutputStream(user + "_private_key");
			// encoding the public key into Base64 to write to file
			keyfos.write(Base64.getEncoder().encode(generatedPrivateKey.getEncoded()));
			keyfos.close();

			// Reading from file starts here
			// Initializing a key factory to generate public and private keys from files
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			// initializing key file input stream to read the files containing keys
			FileInputStream keyfis = new FileInputStream(user + "_public_key");
			byte[] encPubKey = new byte[keyfis.available()];
			keyfis.read(encPubKey);
			keyfis.close();
			PublicKey publicKeyFromFile = keyFactory
					.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(encPubKey)));
			System.out.println("Are public keys are equal? " + publicKeyFromFile.equals(generatedPublicKey));

			keyfis = new FileInputStream(user + "_private_key");
			byte[] encPrivKey = new byte[keyfis.available()];
			keyfis.read(encPrivKey);
			keyfis.close();
			PrivateKey privateKeyFromFile = keyFactory
					.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encPrivKey)));
			System.out.println("Are private keys are equal? " + privateKeyFromFile.equals(generatedPrivateKey));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String generateSignature(String user, String transaction) {
		return "";
	}

	public static void main(String[] args) throws InvalidKeySpecException {
		System.out.println(generate("alice"));
		System.out.println(generate("bob"));
	}

}
