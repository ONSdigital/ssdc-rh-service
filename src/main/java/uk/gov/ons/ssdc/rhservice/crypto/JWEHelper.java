package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JWEObject;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;

public class JWEHelper {

  public static String getKid(String jwe) {
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
