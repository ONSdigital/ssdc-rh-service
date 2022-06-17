package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.shaded.json.JSONObject;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public abstract class JWSHelper {

  public String getKid(JWSObject jwsObject) {
    String keyId = jwsObject.getHeader().getKeyID();
    if (StringUtils.isEmpty(keyId)) {
      throw new RuntimeException("Failed to extract Key Id");
    }
    return keyId;
  }

  /** Class for JWS encoding for single key, caching key processing. */
  public static class EncodeJws extends JWSHelper {
    private Key key;
    private JWSHeader jwsHeader;
    private RSASSASigner signer;

    public EncodeJws(Key key) {
      this.key = key;
      this.jwsHeader = buildHeader(key);
      RSAKey jwk = (RSAKey) key.getJWK();

      try {
        this.signer = new RSASSASigner(jwk);
      } catch (JOSEException e) {
        throw new RuntimeException("Failed to create private JWSSigner to sign claims");
      }
    }

    public JWSObject encode(Map<String, Object> claims) {
      Payload jwsClaims = buildClaims(claims);
      JWSObject jwsObject = new JWSObject(jwsHeader, jwsClaims);

      try {
        jwsObject.sign(this.signer);
        return jwsObject;
      } catch (JOSEException e) {
        throw new RuntimeException("Failed to sign claims");
      }
    }

    private JWSHeader buildHeader(Key key) {
      JWSHeader jwsHeader =
          new JWSHeader.Builder(JWSAlgorithm.RS256)
              .type(JOSEObjectType.JWT)
              .keyID(key.getKid())
              .build();
      return jwsHeader;
    }

    private Payload buildClaims(Map<String, Object> claims) {
      // import net.minidev.json.JSONObject had this originally
      JSONObject jsonObject = new JSONObject(claims);
      Payload jwsClaims = new Payload(jsonObject);
      return jwsClaims;
    }
  }

  public static class DecodeJws extends JWSHelper {

    public String decode(JWSObject jwsObject, Key key) {
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
}
