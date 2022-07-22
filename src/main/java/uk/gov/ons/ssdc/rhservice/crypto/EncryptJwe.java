package uk.gov.ons.ssdc.rhservice.crypto;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.stringToKeys;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.Key;

@Service
public class EncryptJwe {
  private final JWEHeader jweHeader;
  private final RSAEncrypter encryptor;

  public EncryptJwe(@Value("${jwt_keys}") String jwtKeyStr) {
    Key jwePublicKey = stringToKeys(jwtKeyStr).getJwePublicKey();

    this.jweHeader = buildHeader(jwePublicKey);
    RSAKey jwk = (RSAKey) jwePublicKey.getJWK();

    try {
      encryptor = new RSAEncrypter(jwk);
    } catch (JOSEException e) {
      throw new RuntimeException("Cannot initialise encryption for JWE", e);
    }
  }

  public String encrypt(JWSObject jws) {
    Payload payload = new Payload(jws);
    JWEObject jweObject = new JWEObject(jweHeader, payload);

    try {
      jweObject.encrypt(this.encryptor);
      return jweObject.serialize();
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to encrypt JWE", e);
    }
  }

  @SuppressWarnings("deprecation")
  private JWEHeader buildHeader(Key key) {

    // We HAVE to use the deprecated Algo to remain compatible with EQ
    return new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM)
        .keyID(key.getKeyId())
        .build();
  }
}
