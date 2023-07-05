package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionExerciseUpdateDTO {
  private String collectionExerciseId;
  private List<CollectionInstrumentSelectionRule> collectionInstrumentRules;
  private String name;
  private String surveyId;
  private String reference;

  private Date startDate;

  private Date endDate;

  private Object metadata;
}
