package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class CollectionExerciseUpdateDTO {
  private String collectionExerciseId;
  private String name;
  private String surveyId;
  private Object metadata;
}
