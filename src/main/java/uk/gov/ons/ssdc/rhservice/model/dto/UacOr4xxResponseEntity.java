package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Optional;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
public class UacOr4xxResponseEntity {
  Optional<ResponseEntity> responseEntityOptional;
  UacUpdateDTO uacUpdateDTO;
  CaseUpdateDTO caseUpdateDTO;
  CollectionExerciseUpdateDTO collectionExerciseUpdateDTO;
}
