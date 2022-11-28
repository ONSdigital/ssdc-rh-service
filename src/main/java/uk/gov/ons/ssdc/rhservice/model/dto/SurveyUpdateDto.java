package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Map;
import lombok.Data;

@Data
public class SurveyUpdateDto {
  private String surveyId;
  private String name;
  private Map<String, Object> metadata;
}
