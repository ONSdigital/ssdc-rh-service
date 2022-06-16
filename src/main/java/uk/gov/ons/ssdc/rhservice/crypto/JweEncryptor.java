package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JWSObject;

import java.util.Map;
import java.util.Optional;


public class JweEncryptor {
  private static String KEYTYPE_PUBLIC = "public";

  private JWSHelper.EncodeJws jwsEncoder;
  private JWEHelper.EncryptJwe jweEncryptor;
  private Key privateKey;
  private Key publicKey;

  public JweEncryptor(KeyStore keyStore, String keyPurpose)  {
    String KEYTYPE_PRIVATE = "private";
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

    this.jwsEncoder = new JWSHelper.EncodeJws(this.privateKey);
    this.jweEncryptor = new JWEHelper.EncryptJwe(this.publicKey);
  }

  public String encrypt(Map<String, Object> claims) {
    JWSObject jws = jwsEncoder.encode(claims);
    return jweEncryptor.encrypt(jws);
  }
}
