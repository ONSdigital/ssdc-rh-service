package uk.gov.ons.ssdc.rhservice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PayloadDTO {
  private CaseUpdateDTO caseUpdate;
  private UacUpdateDTO uacUpdate;
  private CollectionExerciseUpdateDTO collectionExerciseUpdate;
  private EqLaunchDTO eqLaunch;
}
