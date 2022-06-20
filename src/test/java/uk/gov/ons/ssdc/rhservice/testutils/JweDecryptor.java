package uk.gov.ons.ssdc.rhservice.testutils;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import java.text.ParseException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import uk.gov.ons.ssdc.rhservice.crypto.KeyStore;
import uk.gov.ons.ssdc.rhservice.model.dto.Key;

public class JweDecryptor {

  private final DecryptJwe.DecodeJws jwsHelper = new DecryptJwe.DecodeJws();
  private final DecryptJwe jweHelper = new DecryptJwe();
  private final KeyStore keyStore;

  public JweDecryptor(KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  public String decrypt(String encryptedValue) {

    Optional<Key> publicKey = keyStore.getKeyById(getJweKeyId(encryptedValue));
    JWSObject jws;

    Optional<Key> key = keyStore.getKeyById(getJweKeyId(encryptedValue));
    if (publicKey.isPresent()) {
      jws = jweHelper.decrypt(encryptedValue, key.get());
    } else {
      throw new RuntimeException("Failed to decrypt JWE");
    }

    Optional<Key> privateKey = keyStore.getKeyById(getJwsKeyId(jws));
    if (privateKey.isPresent()) {
      return jwsHelper.decode(jws, privateKey.get());
    } else {
      throw new RuntimeException("Failed to retrieve private key to verify JWS");
    }
  }

  private String getJweKeyId(String jwe) {
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

  private String getJwsKeyId(JWSObject jwsObject) {
    String keyId = jwsObject.getHeader().getKeyID();
    if (StringUtils.isEmpty(keyId)) {
      throw new RuntimeException("Failed to extract Key Id");
    }
    return keyId;
  }
}
