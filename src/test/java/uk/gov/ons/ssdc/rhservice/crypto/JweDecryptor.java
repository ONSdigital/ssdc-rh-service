package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JWSObject;
import java.util.Optional;

public class JweDecryptor {

  private JWSHelper.DecodeJws jwsHelper = new JWSHelper.DecodeJws();
  private JWEHelper.DecryptJwe jweHelper = new JWEHelper.DecryptJwe();
  private KeyStore keyStore;

  public JweDecryptor(KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  public String decrypt(String encryptedValue) {

    Optional<Key> publicKey = keyStore.getKeyById(jweHelper.getKid(encryptedValue));
    JWSObject jws;
    if (publicKey.isPresent()) {
      jws = jweHelper.decrypt(encryptedValue, publicKey.get());
    } else {
      throw new RuntimeException("Failed to retrieve public key to decrypt JWE");
    }

    Optional<Key> privateKey = keyStore.getKeyById(jwsHelper.getKid(jws));
    if (privateKey.isPresent()) {
      return jwsHelper.decode(jws, privateKey.get());
    } else {
      throw new RuntimeException("Failed to retrieve private key to verify JWS");
    }
  }
}
