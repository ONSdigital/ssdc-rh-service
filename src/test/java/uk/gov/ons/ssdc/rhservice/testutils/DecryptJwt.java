package uk.gov.ons.ssdc.rhservice.testutils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import java.text.ParseException;
import uk.gov.ons.ssdc.rhservice.model.dto.Key;

public class DecryptJwt {
  public static JWSObject decryptJwe(String token, Key key) {

    JWEObject jweObject;
    try {
      jweObject = JWEObject.parse(token);
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

  public static String decodeJws(JWSObject jwsObject, Key key) {

    try {
      if (jwsObject.verify(new RSASSAVerifier((RSAKey) key.getJWK()))) {
        Payload payload = jwsObject.getPayload();
        if (payload == null) {
          throw new RuntimeException("Extracted JWS Payload null");
        }
        return payload.toString();
      } else {
        throw new RuntimeException("Failed to verify JWS signature");
      }
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to verify JWS signature");
    }
  }
}
