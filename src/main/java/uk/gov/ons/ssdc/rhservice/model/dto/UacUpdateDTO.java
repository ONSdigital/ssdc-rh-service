package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
