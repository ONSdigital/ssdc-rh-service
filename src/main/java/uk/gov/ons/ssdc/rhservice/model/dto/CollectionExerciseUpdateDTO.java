package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CollectionExerciseUpdateDTO {
  private String collectionExerciseId;
//  private String name;
//  private UUID surveyId;
//  private String reference;
//  private OffsetDateTime startDate;
//  private OffsetDateTime endDate;
//  private Object metadata;
  private List<CollectionInstrumentSelectionRule> collectionInstrumentSelectionRules;
}
