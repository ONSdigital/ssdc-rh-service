package uk.gov.ons.ssdc.rhservice.crypto;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.shaded.json.JSONObject;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import uk.gov.ons.ssdc.rhservice.crypto.keys.Key;

public class JWSHelper {

  public static String getKid(JWSObject jwsObject) {
    String keyId = jwsObject.getHeader().getKeyID();
    if (StringUtils.isEmpty(keyId)) {
      throw new RuntimeException("Failed to extract Key Id");
    }
    return keyId;
  }

  public static JWSHeader buildHeader(Key key) {
    return new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .keyID(key.getKid())
        .build();
  }

  public static Payload buildClaims(Map<String, Object> claims) {
    // import net.minidev.json.JSONObject had this originally
    JSONObject jsonObject = new JSONObject(claims);
    return new Payload(jsonObject);
  }
}
