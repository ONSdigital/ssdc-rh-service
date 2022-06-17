package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JWSObject;
import java.util.Map;
import java.util.Optional;

public class JweEncryptor {
  private final static String KEYTYPE_PUBLIC = "public";
  private final static String KEYTYPE_PRIVATE = "private";

  private JWSHelper.EncodeJws encodeJws;
  private JWEHelper.EncryptJwe encryptJwe;
  private Key privateKey;
  private Key publicKey;

  public JweEncryptor(KeyStore keyStore, String keyPurpose) {
    Optional<Key> privateKeyOpt = keyStore.getKeyForPurposeAndType(keyPurpose, KEYTYPE_PRIVATE);
    if (privateKeyOpt.isPresent()) {
      this.privateKey = privateKeyOpt.get();
    } else {
      throw new RuntimeException("Failed to retrieve private key to sign claims");
    }

    Optional<Key> publicKeyOpt = keyStore.getKeyForPurposeAndType(keyPurpose, KEYTYPE_PUBLIC);
    if (publicKeyOpt.isPresent()) {
      this.publicKey = publicKeyOpt.get();
    } else {
      throw new RuntimeException("Failed to retrieve public key to encode payload");
    }

    this.encodeJws = new JWSHelper.EncodeJws(this.privateKey);
    this.encryptJwe = new JWEHelper.EncryptJwe(this.publicKey);
  }

  public String encrypt(Map<String, Object> claims) {
    JWSObject jws = encodeJws.encode(claims);
    return encryptJwe.encrypt(jws);
  }
}
