package rand;

import java.io.File;
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

public class TestPrivateKey {
	public static boolean generate(String user) throws InvalidKeySpecException {
		try {
			/*
			 * Using DSA algorithm for key generation. SUN parameter here implies that the
			 * default implementation to be used.
			 */
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			/*
			 * Using SHA1 algorithm for Pseudo-Random Number Generation. SUN parameter here
			 * implies that the default implementation to be used.
			 */
			keyGen.initialize(1024);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey generatedPrivateKey = pair.getPrivate();
			// byte[] encodedPrivateKey =
			// Base64.getEncoder().encode(generatedPrivateKey.getEncoded());
			PKCS8EncodedKeySpec pkcs8EncodedPrivateKey = new PKCS8EncodedKeySpec(generatedPrivateKey.getEncoded());
			FileOutputStream keyfos = new FileOutputStream(user + "_private_key");
			keyfos.write(pkcs8EncodedPrivateKey.getEncoded());
			keyfos.close();

			KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			// keyfis = new FileInputStream(user + "_private_key");
			// byte[] encPrivKey = new byte[keyfis.available()];
			// keyfis.read(encPrivKey);
			// keyfis.close();
			// PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encPrivKey);
			// PrivateKey privateKeyFromFile = keyFactory.generatePrivate(privKeySpec);
			File filePrivateKey = new File(user + "_private_key");
			FileInputStream fis = new FileInputStream(user + "_private_key");
			byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			fis.read(encodedPrivateKey);
			fis.close();
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
			PrivateKey privateKeyFromFile = keyFactory.generatePrivate(privateKeySpec);
			System.out.println("private keys are equal" + privateKeyFromFile.equals(generatedPrivateKey));
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

	private static boolean areKeysEqual(byte[] encodedPublicKey, byte[] decodedPublicKey) {
		if (encodedPublicKey.length != decodedPublicKey.length) {
			return false;
		}
		for (int i = 0; i < encodedPublicKey.length; i++) {
			if (encodedPublicKey[i] != decodedPublicKey[i]) {
				return false;
			}
		}
		return true;
	}

	public static String generateSignature(String user, String transaction) {
		return "";
	}

	public static void main(String[] args) throws InvalidKeySpecException {
		System.out.println(generate("test"));
	}
}
