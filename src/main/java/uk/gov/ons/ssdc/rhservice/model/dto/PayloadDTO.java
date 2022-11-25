package uk.gov.ons.ssdc.rhservice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
// @Builder
// @NoArgsConstructor

public class PayloadDTO {
  private CaseUpdateDTO caseUpdate;
  private UacUpdateDTO uacUpdate;
  private CollectionExerciseUpdateDTO collectionExerciseUpdateDTO;
  private SurveyDto surveyDto;
  private EqLaunchDTO eqLaunch;
}
