package uk.gov.ons.ssdc.rhservice.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertObjectToJson;

import com.nimbusds.jose.JWSObject;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.ons.ssdc.rhservice.model.dto.Key;
import uk.gov.ons.ssdc.rhservice.testutils.DecryptJwt;

class EncodeAndEncryptionTest {

  private static final String SIGNING_KEY_ID = "f008897e548d5c4fb9271e5a85f6fc4759301f26";
  private static final String SIGNING_PRIVATE_KEY =
      "-----BEGIN RSA PRIVATE KEY-----\n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\n-----END RSA PRIVATE KEY-----";

  private static final String DECODE_KEY_ID = "f008897e548d5c4fb9271e5a85f6fc4759301f26";
  private static final String DECODE_KEY =
      "-----BEGIN PUBLIC KEY-----\nMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAiflKcRSyjo5iIx/Qhqm3\ntkoIvnaLDjf06LTiD4piyM1Y1V2ATMK5KFVRAmpHN8CaTEdieufY9cuywbPEaPqR\npdlQr9xTix05mlUBs826ktCGD8UKHmNV6CgLYLaXzswBl5NDZTMX/9g4KdyadUpa\nJ3Z3Jrp7Brnb1vSVt8uBnjcfOhWsA7BuEYQqgIWw/hkGEBh0yMhvgSQda69gukFS\nfhsV4YcZ7qUTVctujIOOiVYkCsRRVAvIXenVb1f1BjwqLVnG5qRXUBXRTd1w5P90\nT5BTuBQcCQlvtNiCbxHoKrt6LGOp7dikgnU/jcsLsT4X3c49UnOB4zRQIde4v50y\nRqcwloZ/cDTmyWYEUZEAVHI9DZ4+R9M2J0XWIrtL/bHoGGMZdxGs4VkvMJ6YprER\n8krXiNQoxRVzRhWGHVpGLC/EZvnE65FhSc21vjd5n85Biax/Cxf1V3whjmc5O9ve\n/nOqsx3rWPYbFGvDYEdYAoah/IxW9mMt5SZCL0ifxQXkt39pd8eYPIhGCcZ7J1mP\nlAwzPFb35pWSBcokZ3XoJHPdEHKoyhOqvnujiIkjlIwgXOumNRzSLzUshVIJR9GR\nBm8QmV387qUjnI8baULGKlRyY48WBUs7M++tzpNFQtHGwwsV1V0A53pC3P3e4gg4\nh6Gp4romAeU+Md8k/bmuyn8CAwEAAQ==\n-----END PUBLIC KEY-----";

  static final String PUBLIC_ENCRYPTING_KEY =
      "-----BEGIN PUBLIC KEY-----\nMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAoJFv7Mnp1YQNNFBGwf/T\nAhLtjvZxwPJCV3nfLxC7rcdyYddsrKtda7E4c4nhPykPbnkpFqa4GGj7fHz8PS6T\ns/F72owfz8HJ2vHtOlClwOv9C6xl2qZ9TayVQLt2UNLSQrT18163svpF7pZWYmFe\nGqNeqO2UtY+xJb+uRAgwR5gVISglVDXWEtHMESQyVY5rXECMneuVcKmHKvVBLSpf\nRd7gnE53hO2GpzQgRcVkuf4wIg6oAoxuLBFgpSYUF8Bp/CjGAjKyzlx6trLj9PXM\nHzMIlVGrcmqqgdkpzPGjOSHOJHxZpCS264a1V2JUr+xdzmpoxaH1z3xnoUayBkWO\nW2AJNQwtIKV8MpUvglYkFPZgnZ0mT4DSLtNdgWxG+uxlHIk6DMU+/ghDTzEXrnwq\nfZYeAYj4+dQpYDDIpekThFhMSz5sKrttMM959Zu4WdpfP7TnIn3OrJmtHNEU4mZj\nGgO1oM5cGCTeAVRCAQbg3gqB0JcxD7PI3HhY4yDYW5zHOH1Wv0BxeLBMJmHJqUyD\nrFrfRWT6l8IgNk7WxFD6Vvz/6VXzrxTJO+BQ8LJaX3IW0/NFYq1t0MVuJ3tuXCe6\n/2lIQW+quzVe1saQPXH0hmZ9Q26h98WbWAsziF57kdgqakMGqMMVBv9/FshaKDPn\nVGfxlg5OusA1TBy7BMRwfa8CAwEAAQ==\n-----END PUBLIC KEY-----";
  static final String PUBLIC_ENCRYPTING_KEY_ID = "bc26b780aa46f053291ba122062e6075656c2345";

  private static final String PRIVATE_DECRYPTING_KEY =
      "-----BEGIN RSA PRIVATE KEY-----\n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\n-----END RSA PRIVATE KEY-----";
  private static final String PRIVATE_DECRYPTING_KEY_ID =
      "bc26b780aa46f053291ba122062e6075656c2345";

  @Test
  public void testGoodEncoding() {
    Key key = new Key();
    key.setKeyId(SIGNING_KEY_ID);
    key.setValue(SIGNING_PRIVATE_KEY);
    EncodeJws encodeJws = new EncodeJws(convertObjectToJson(key));

    Map<String, Object> payload = new HashMap<>();
    payload.put("KEY1", "VALUE1");

    JWSObject encodedPayload = encodeJws.encode(payload);

    Key decodeKey = new Key();
    decodeKey.setKeyId(DECODE_KEY_ID);
    decodeKey.setValue(DECODE_KEY);
    String decoded = DecryptJwt.decodeJws(encodedPayload, convertObjectToJson(decodeKey));
    assertThat(decoded).isEqualTo("{\"KEY1\":\"VALUE1\"}");
  }

  @Test
  public void testEncodeJWSsetUpWithBadPrivateKey() {
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () ->
                new EncodeJws(
                    "{\"keyId\":\"f008897e548d5c4fb9271e5a85f6fc4759301f26\",\"purpose\":null,\"type\":null,\"value\":\"-----BEGIN RSA PRIVATE KEY-----\\n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\\n-----END RSA PRIVATE KEY-----\",\"jwk\":{\"keyStore\":null,\"private\":true,\"requiredParams\":{\"e\":\"AQAB\",\"kty\":\"RSA\",\"n\":\"iflKcRSyjo5iIx_Qhqm3tkoIvnaLDjf06LTiD4piyM1Y1V2ATMK5KFVRAmpHN8CaTEdieufY9cuywbPEaPqRpdlQr9xTix05mlUBs826ktCGD8UKHmNV6CgLYLaXzswBl5NDZTMX_9g4KdyadUpaJ3Z3Jrp7Brnb1vSVt8uBnjcfOhWsA7BuEYQqgIWw_hkGEBh0yMhvgSQda69gukFSfhsV4YcZ7qUTVctujIOOiVYkCsRRVAvIXenVb1f1BjwqLVnG5qRXUBXRTd1w5P90T5BTuBQcCQlvtNiCbxHoKrt6LGOp7dikgnU_jcsLsT4X3c49UnOB4zRQIde4v50yRqcwloZ_cDTmyWYEUZEAVHI9DZ4-R9M2J0XWIrtL_bHoGGMZdxGs4VkvMJ6YprER8krXiNQoxRVzRhWGHVpGLC_EZvnE65FhSc21vjd5n85Biax_Cxf1V3whjmc5O9ve_nOqsx3rWPYbFGvDYEdYAoah_IxW9mMt5SZCL0ifxQXkt39pd8eYPIhGCcZ7J1mPlAwzPFb35pWSBcokZ3XoJHPdEHKoyhOqvnujiIkjlIwgXOumNRzSLzUshVIJR9GRBm8QmV387qUjnI8baULGKlRyY48WBUs7M--tzpNFQtHGwwsV1V0A53pC3P3e4gg4h6Gp4romAeU-Md8k_bmuyn8\"},\"modulus\":{},\"publicExponent\":{},\"privateExponent\":{},\"firstPrimeFactor\":{},\"secondPrimeFactor\":{},\"firstFactorCRTExponent\":{},\"secondFactorCRTExponent\":{},\"firstCRTCoefficient\":{},\"otherPrimes\":[],\"keyType\":{\"value\":\"RSA\",\"requirement\":\"REQUIRED\"},\"keyUse\":null,\"keyOperations\":null,\"algorithm\":null,\"keyID\":null,\"x509CertURL\":null,\"x509CertThumbprint\":null,\"x509CertSHA256Thumbprint\":null,\"x509CertChain\":null,\"parsedX509CertChain\":null}}"));

    Assertions.assertThat(thrown.getMessage()).isEqualTo("Could not parse key value");
  }

  @Test
  public void testEncodeAndEncrypt() {
    Key jwsKey = new Key();
    jwsKey.setKeyId(SIGNING_KEY_ID);
    jwsKey.setValue(SIGNING_PRIVATE_KEY);
    EncodeJws encodeJws = new EncodeJws(convertObjectToJson(jwsKey));

    Map<String, Object> payload = new HashMap<>();
    payload.put("KEY1", "VALUE1");

    JWSObject encodedPayload = encodeJws.encode(payload);

    Key jweKey = new Key();
    jweKey.setKeyId(PUBLIC_ENCRYPTING_KEY_ID);
    jweKey.setValue(PUBLIC_ENCRYPTING_KEY);

    EncryptJwe encryptJwe = new EncryptJwe(convertObjectToJson(jweKey));

    String encryptJweToken = encryptJwe.encrypt(encodedPayload);

    Key decryptKey = new Key();
    decryptKey.setKeyId(PRIVATE_DECRYPTING_KEY_ID);
    decryptKey.setValue(PRIVATE_DECRYPTING_KEY);
    JWSObject jwsObject = DecryptJwt.decryptJwe(encryptJweToken, convertObjectToJson(decryptKey));

    Key decodeKey = new Key();
    decodeKey.setKeyId(DECODE_KEY_ID);
    decodeKey.setValue(DECODE_KEY);
    String decodedDecrypted = DecryptJwt.decodeJws(jwsObject, convertObjectToJson(decodeKey));

    assertThat(decodedDecrypted).isEqualTo("{\"KEY1\":\"VALUE1\"}");
  }
}