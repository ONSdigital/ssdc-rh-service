package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Map;
import lombok.Data;

@Data
public class SurveyDto {
  private String id;
  private String name;
  private Map<String, Object> metadata;
}
