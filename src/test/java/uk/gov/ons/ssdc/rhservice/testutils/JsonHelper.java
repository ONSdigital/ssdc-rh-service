package uk.gov.ons.ssdc.rhservice.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.gov.ons.ssdc.rhservice.model.dto.JWTKeysDecrypt;
import uk.gov.ons.ssdc.rhservice.utils.ObjectMapperFactory;

public class JsonHelper {
  private static final ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();

  public static JWTKeysDecrypt fileToDecryptKeys(String fileLocation) {

    Path filePath = Path.of(fileLocation);
    try {
      String content = Files.readString(filePath);
      return stringToDecyptKeys(content);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load Keys file from: " + fileLocation);
    }
  }

  public static JWTKeysDecrypt stringToDecyptKeys(String keyString) {
    try {
      return objectMapper.readValue(keyString, JWTKeysDecrypt.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read cryptographic keys", e);
    }
  }
}
