package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionExerciseUpdateDTO {
  private String collectionExerciseId;
  private List<CollectionInstrumentSelectionRule> collectionInstrumentRules;
}
