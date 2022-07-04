package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Data
public class UacOr4xxResponseEntity {
    Optional<ResponseEntity> responseEntityOptional;
    UacUpdateDTO uacUpdateDTO;
    CaseUpdateDTO caseUpdateDTO;
}
