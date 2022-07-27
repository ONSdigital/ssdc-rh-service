package uk.gov.ons.ssdc.rhservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;

public class JsonHelper {
  private static final ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();

  public static String convertObjectToJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed converting Object To Json", e);
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
