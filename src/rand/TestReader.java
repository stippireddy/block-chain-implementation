package rand;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

public class TestReader {
  public static void main(String[] args) throws IOException, URISyntaxException,
      NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String signatureFromFile = DatatypeConverter
        .printHexBinary(Files.readAllBytes(FileSystems.getDefault().getPath("signature.sign")));
    System.out.println(signatureFromFile);

    String privateKeyContent =
        new String(Files.readAllBytes(FileSystems.getDefault().getPath("test_private.pem")));
    privateKeyContent = privateKeyContent.replaceAll("\\n", "")
        .replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

    KeyFactory kf = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpecPKCS8 =
        new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
    PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
    Signature sg = Signature.getInstance("SHA256withRSA");
    sg.initSign(privKey);
    // sg.update(Files.readAllBytes(FileSystems.getDefault().getPath("trans.txt")));
    sg.update("9b27ea15; 0; ; 1; (Alice, 5000)\n".getBytes());
    String signatureCreated = DatatypeConverter.printHexBinary(sg.sign());
    boolean bool = signatureCreated.equals(signatureFromFile);

    String publicKeyContent =
        new String(Files.readAllBytes(FileSystems.getDefault().getPath("test_public.pem")));
    publicKeyContent = publicKeyContent.replaceAll("\\n", "")
        .replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");;
    X509EncodedKeySpec keySpecX509 =
        new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));

    RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
    String transaction = "1; (cbdf7b37, 0); 3; (Gopesh, 100)(Bob, 45)(Bob, 5)";
    MessageDigest sha = MessageDigest.getInstance("SHA-256"); // Obviously using SHA-256 here.
    byte[] hash = sha.digest(transaction.getBytes());

    sg.initSign(privKey);
    sg.update(transaction.getBytes());
    byte[] realSig = sg.sign();
    System.out.println(DatatypeConverter.printHexBinary(realSig));
    sg.initVerify(pubKey);
    // read data file into signature instance
    byte[] data = transaction.getBytes();
    sg.update(data);
    byte[] givenSignature =
        "74ddf54a1107dd5d3afe012139802931a7b053616f14ef19eae97c6b2c27497cea8c3d73b4e33cd080bf0d881714c2d7dfcc115ad2717be25c3e32e5dc497715797f8e581dff71006faaa6d667795218e79f9fd97133935670d646b3a962d3445c0ae3c8dbe64f0d5ea6d52c6ab52ed3f6626d85e76c76e24cc339e065807c9c6d17261f448dab8ef4b09c31c78502245f4c4d3efc1cc6a2b20f24dcf7dcc44e3b51cb7efed3678d3c1d256901fed10ead2c22162fb5633536c35686852365c17ebbe2462b10ab8f441bd4bcda70494365476ad350a504f202e0ec32ba0bfe1381cc1da3d0a65f96b3ef43d0afa01efc9324c9ec182d7b164c92fc88483db82c"
            .getBytes();
    System.out.println(sg.verify(DatatypeConverter.parseHexBinary(
        "74ddf54a1107dd5d3afe012139802931a7b053616f14ef19eae97c6b2c27497cea8c3d73b4e33cd080bf0d881714c2d7dfcc115ad2717be25c3e32e5dc497715797f8e581dff71006faaa6d667795218e79f9fd97133935670d646b3a962d3445c0ae3c8dbe64f0d5ea6d52c6ab52ed3f6626d85e76c76e24cc339e065807c9c6d17261f448dab8ef4b09c31c78502245f4c4d3efc1cc6a2b20f24dcf7dcc44e3b51cb7efed3678d3c1d256901fed10ead2c22162fb5633536c35686852365c17ebbe2462b10ab8f441bd4bcda70494365476ad350a504f202e0ec32ba0bfe1381cc1da3d0a65f96b3ef43d0afa01efc9324c9ec182d7b164c92fc88483db82c")));;
  }
}
