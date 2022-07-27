package uk.gov.ons.ssdc.rhservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.JWTKeys;
import uk.gov.ons.ssdc.rhservice.utils.ObjectMapperFactory;

@Data
@Component
public class JWTKeysLoader {
  private final ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();
  private JWTKeys jwtKeys;

  public JWTKeysLoader(@Value("${jwt_keys}") String jwtKeyStr) {
    jwtKeys = jwtKeyFilesToJWTKEYS(jwtKeyStr);
  }

  public JWTKeys jwtKeyFilesToJWTKEYS(String fileLocation) {

    Path filePath = Path.of(fileLocation);
    try {
      String content = Files.readString(filePath);
      return stringToKeys(content);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load Keys file from: " + fileLocation, e);
    }
  }

  private JWTKeys stringToKeys(String keyString) {
    try {
      JWTKeys a = objectMapper.readValue(keyString, JWTKeys.class);
      return a;
    } catch (Exception e) {
      throw new RuntimeException("Failed to read cryptographic keys", e);
    }
  }
}
