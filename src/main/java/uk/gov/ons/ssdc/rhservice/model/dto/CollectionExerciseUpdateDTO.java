package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class CollectionExerciseUpdateDTO {
  private String collectionExerciseId;
  private String name;
  private String surveyId;
  private String reference;
  //    private OffsetDateTime startDate;
  //    private OffsetDateTime endDate;
  private Object metadata;
  private List<CollectionInstrumentSelectionRule> collectionInstrumentSelectionRules;
}
