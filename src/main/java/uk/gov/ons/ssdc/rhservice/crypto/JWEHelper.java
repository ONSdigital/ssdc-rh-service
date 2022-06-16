package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;

public abstract class JWEHelper {

  public static class EncryptJwe extends JWEHelper {
    private Key key;
    private JWEHeader jweHeader;
    private RSAEncrypter encryptor;

    public EncryptJwe(Key key) {
      this.key = key;
      this.jweHeader = buildHeader(key);
      RSAKey jwk = (RSAKey) key.getJWK();

      try {
        encryptor = new RSAEncrypter(jwk);
      } catch (JOSEException e) {
        throw new RuntimeException("Cannot initialise encryption for JWE");
      }
    }

    public String encrypt(JWSObject jws) {
      Payload payload = new Payload(jws);
      JWEObject jweObject = new JWEObject(jweHeader, payload);

      try {
        jweObject.encrypt(this.encryptor);
        return jweObject.serialize();
      } catch (JOSEException e) {
        throw new RuntimeException("Failed to encrypt JWE");
      }
    }

    @SuppressWarnings("deprecation")
    private JWEHeader buildHeader(Key key) {

      // We HAVE to use the deprecated Algo to remain compatible with EQ
      JWEHeader jweHeader =
          new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM)
              .keyID(key.getKid())
              .build();
      return jweHeader;
    }
  }

  public static class DecryptJwe extends JWEHelper {

    public JWSObject decrypt(String jwe, Key key) {

      JWEObject jweObject;
      try {
        jweObject = JWEObject.parse(jwe);
      } catch (ParseException e) {
        throw new RuntimeException("Failed to parse JWE string");
      }

      try {
        jweObject.decrypt(new RSADecrypter((RSAKey) key.getJWK()));
      } catch (JOSEException e) {
        throw new RuntimeException("Failed to decrypt JWE with provided key");
      }

      Payload payload = jweObject.getPayload();
      if (payload == null) {
        throw new RuntimeException("Extracted JWE Payload null");
      }

      return payload.toJWSObject();
    }
  }

  public String getKid(String jwe) {
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
