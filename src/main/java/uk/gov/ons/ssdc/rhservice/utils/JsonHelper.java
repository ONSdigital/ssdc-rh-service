package uk.gov.ons.ssdc.rhservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.JWTKeys;

public class JsonHelper {
  private static final ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();

  public static String convertObjectToJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed converting Object To Json", e);
    }
  }

  public static JWTKeys jwtKeyFilesToJWTKEYS(String fileLocation) {

    Path filePath = Path.of(fileLocation);
    try {
      String content = Files.readString(filePath);
      return stringToKeys(content);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load Keys file from: " + fileLocation, e);
    }
  }

  private static JWTKeys stringToKeys(String keyString) {
    try {
      JWTKeys a = objectMapper.readValue(keyString, JWTKeys.class);
      return a;
    } catch (Exception e) {
      throw new RuntimeException("Failed to read cryptographic keys", e);
    }
  }

  public static EventDTO convertJsonBytesToEvent(byte[] bytes) {
    EventDTO event;
    try {
      event = objectMapper.readValue(bytes, EventDTO.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return event;
  }
}
