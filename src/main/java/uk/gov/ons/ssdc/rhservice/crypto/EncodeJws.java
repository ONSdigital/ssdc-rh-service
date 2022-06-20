package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.shaded.json.JSONObject;
import java.util.Map;

public class EncodeJws {
  private final Key key;
  private final JWSHeader jwsHeader;
  private final RSASSASigner signer;

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
    return new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .keyID(key.getKeyId())
        .build();
  }

  private Payload buildClaims(Map<String, Object> claims) {
    JSONObject jsonObject = new JSONObject(claims);
    return new Payload(jsonObject);
  }
}
