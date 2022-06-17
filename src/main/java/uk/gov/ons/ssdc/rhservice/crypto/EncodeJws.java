package uk.gov.ons.ssdc.rhservice.crypto;

import static uk.gov.ons.ssdc.rhservice.crypto.JWSHelper.buildClaims;
import static uk.gov.ons.ssdc.rhservice.crypto.JWSHelper.buildHeader;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import uk.gov.ons.ssdc.rhservice.crypto.keys.Key;

public class EncodeJws {
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
}
