package uk.gov.ons.ssdc.rhservice.model.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseUpdateDTO {
    private String caseId;
    private String collectionExerciseId;
    private Map<String, String> sample;
}
