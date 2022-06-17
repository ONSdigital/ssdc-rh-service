package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JWSObject;
import java.util.Map;
import java.util.Optional;
import uk.gov.ons.ssdc.rhservice.crypto.keys.Key;
import uk.gov.ons.ssdc.rhservice.crypto.keys.KeyStore;

public class JwtEncryptor {
  private static final String KEYTYPE_PUBLIC = "public";
  private static final String KEYTYPE_PRIVATE = "private";

  private final EncodeJws encodeJws;
  private final EncryptJwe encryptJwe;

  public JwtEncryptor(KeyStore keyStore, String keyPurpose) {
    Optional<Key> privateKeyOpt = keyStore.getKeyForPurposeAndType(keyPurpose, KEYTYPE_PRIVATE);
    Key privateKey;
    if (privateKeyOpt.isPresent()) {
      privateKey = privateKeyOpt.get();
    } else {
      throw new RuntimeException("Failed to retrieve private key to sign claims");
    }

    Optional<Key> publicKeyOpt = keyStore.getKeyForPurposeAndType(keyPurpose, KEYTYPE_PUBLIC);
    Key publicKey;
    if (publicKeyOpt.isPresent()) {
      publicKey = publicKeyOpt.get();
    } else {
      throw new RuntimeException("Failed to retrieve public key to encode payload");
    }

    this.encodeJws = new EncodeJws(privateKey);
    this.encryptJwe = new EncryptJwe(publicKey);
  }

  public String encrypt(Map<String, Object> payload) {
    JWSObject jws = encodeJws.encode(payload);
    return encryptJwe.encrypt(jws);
  }
}
