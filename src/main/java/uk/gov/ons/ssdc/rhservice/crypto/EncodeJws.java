package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.Key;
import uk.gov.ons.ssdc.rhservice.service.JWTKeysLoader;

@Service
public class EncodeJws {
  private final JWSHeader jwsHeader;
  private final RSASSASigner signer;

  public EncodeJws(JWTKeysLoader jwtKeysLoader) {

    Key jwsPrivateKey = jwtKeysLoader.getJwtKeys().getJwsPrivateKey();

    this.jwsHeader = buildHeader(jwsPrivateKey);
    RSAKey jwk = (RSAKey) jwsPrivateKey.getJWK();

    try {
      this.signer = new RSASSASigner(jwk);
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to create private JWSSigner to sign claims", e);
    }
  }

  public JWSObject encode(Map<String, Object> claims) {
    Payload jwsClaims = buildClaims(claims);
    JWSObject jwsObject = new JWSObject(jwsHeader, jwsClaims);

    try {
      jwsObject.sign(this.signer);
      return jwsObject;
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to sign claims", e);
    }
  }

  private JWSHeader buildHeader(Key key) {
    return new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .keyID(key.getKeyId())
        .build();
  }

  private Payload buildClaims(Map<String, Object> claims) {
    return new Payload(claims);
  }
}
