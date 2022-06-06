package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Date;
import java.util.Map;
import lombok.Data;

@Data
public class CaseUpdateDTO {
  private String caseId;
  private String surveyId;
  private String collectionExerciseId;
  private boolean invalid;
  private String refusalReceived;
  private Map<String, String> sample;
  private Map<String, String> sampleSensitive;
  private String caseRef;
  private Date createdAt;
  private Date lastUpdatedAt;
}
