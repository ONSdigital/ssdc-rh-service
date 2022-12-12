package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UacUpdateDTO {

  private String caseId;

  private String collectionExerciseId;

  private String surveyId;

  private String collectionInstrumentUrl;

  private boolean active;

  private String uacHash;

  private String qid;

  private boolean receiptReceived;

  private boolean eqLaunched;

  private Map<String, String> launchData;
}
