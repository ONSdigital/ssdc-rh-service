package uk.gov.ons.ssdc.rhservice.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import uk.gov.ons.ssdc.rhservice.model.dto.JWTKeysDecrypt;
import uk.gov.ons.ssdc.rhservice.utils.ObjectMapperFactory;

public class JsonHelper {
  private static final ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();

  public static <T> T convertJsonBytesToObject(byte[] bytes, Class<T> clazz) {
    try {
      return objectMapper.readValue(bytes, clazz);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static JWTKeysDecrypt stringToDecyptKeys(String keyString) {
    try {
      JWTKeysDecrypt a = objectMapper.readValue(keyString, JWTKeysDecrypt.class);
      return a;
    } catch (Exception e) {
      throw new RuntimeException("Failed to read cryptographic keys", e);
    }
  }
}
