package uk.gov.ons.ssdc.rhservice.model.dto;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Key {
  private String keyId;
  private String value;

  public JWK getJWK() {
    try {
      return JWK.parseFromPEMEncodedObjects(value);
    } catch (JOSEException ex) {
      throw new RuntimeException("Could not parse key value", ex);
    }
  }
}
