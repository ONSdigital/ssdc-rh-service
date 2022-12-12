package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionExerciseUpdateDTO {
  private String collectionExerciseId;
  private List<CollectionInstrumentSelectionRule> collectionInstrumentRules;
}
