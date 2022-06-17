package uk.gov.ons.ssdc.rhservice.testutils;

import com.nimbusds.jose.JWSObject;
import uk.gov.ons.ssdc.rhservice.crypto.JWEHelper;
import uk.gov.ons.ssdc.rhservice.crypto.JWSHelper;
import uk.gov.ons.ssdc.rhservice.crypto.keys.Key;
import uk.gov.ons.ssdc.rhservice.crypto.keys.KeyStore;

import java.util.Optional;

public class JweDecryptor {

  private final DecryptJwe.DecodeJws jwsHelper = new DecryptJwe.DecodeJws();
  private final DecryptJwe jweHelper = new DecryptJwe();
  private final KeyStore keyStore;

  public JweDecryptor(KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  public String decrypt(String encryptedValue) {

    Optional<Key> publicKey = keyStore.getKeyById(JWEHelper.getKid(encryptedValue));
    JWSObject jws;

    Optional<Key> key = keyStore.getKeyById("0d6ba9ff8cd6b9dd4514d9a87c50b27d1dd6c5b5");
    if (publicKey.isPresent()) {
      jws = jweHelper.decrypt(encryptedValue, key.get());
    } else {
      throw new RuntimeException("Failed to decrypt JWE");
    }

    Optional<Key> privateKey = keyStore.getKeyById(JWSHelper.getKid(jws));
    if (privateKey.isPresent()) {
      return jwsHelper.decode(jws, privateKey.get());
    } else {
      throw new RuntimeException("Failed to retrieve private key to verify JWS");
    }
  }
}
