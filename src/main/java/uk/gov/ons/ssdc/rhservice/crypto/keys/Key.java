package uk.gov.ons.ssdc.rhservice.crypto.keys;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
/** Cryptographic Key model object */
public class Key {
  private String kid;
  private String purpose;
  private String type;
  private String value;

  public JWK getJWK() {
    try {
      return JWK.parseFromPEMEncodedObjects(value);
    } catch (JOSEException ex) {
      throw new RuntimeException("Could not parse key value");
    }
  }
}
