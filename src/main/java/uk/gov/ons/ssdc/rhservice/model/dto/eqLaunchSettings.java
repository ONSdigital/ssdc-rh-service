package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class eqLaunchSettings {
  private String sampleField;
  private String launchDataFieldName;
  private boolean mandatory;
}
