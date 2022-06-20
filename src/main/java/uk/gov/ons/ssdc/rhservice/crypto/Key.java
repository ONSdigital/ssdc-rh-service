package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import lombok.Data;

@Data
public class Key {
  private String keyId;
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
