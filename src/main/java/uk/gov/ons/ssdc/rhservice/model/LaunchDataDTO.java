package uk.gov.ons.ssdc.rhservice.model;

import lombok.Data;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;

@Data
public class LaunchDataDTO {
  private UacUpdateDTO uacUpdateDTO;
  private CaseUpdateDTO caseUpdateDTO;
}
