package uk.gov.ons.ssdc.rhservice.testutils;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import java.text.ParseException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import uk.gov.ons.ssdc.rhservice.crypto.JWSHelper;
import uk.gov.ons.ssdc.rhservice.crypto.keys.Key;
import uk.gov.ons.ssdc.rhservice.crypto.keys.KeyStore;

public class JweDecryptor {

  private final DecryptJwe.DecodeJws jwsHelper = new DecryptJwe.DecodeJws();
  private final DecryptJwe jweHelper = new DecryptJwe();
  private final KeyStore keyStore;

  public JweDecryptor(KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  public String decrypt(String encryptedValue) {

    Optional<Key> publicKey = keyStore.getKeyById(getKid(encryptedValue));
    JWSObject jws;

    Optional<Key> key = keyStore.getKeyById(getKid(encryptedValue));
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

  private String getKid(String jwe) {
    try {
      JWEObject jweObject = JWEObject.parse(jwe);
      String keyId = jweObject.getHeader().getKeyID();
      if (StringUtils.isEmpty(keyId)) {

        throw new RuntimeException("Failed to extract key Id from JWE header");
      }
      return keyId;
    } catch (ParseException e) {
      throw new RuntimeException("Failed to parse JWE string");
    }
  }
}
