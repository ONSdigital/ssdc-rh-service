package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class LaunchDataFieldDTO {
  private String launchDataFieldName;
  private String defaultValue; // made up idea, perhaps useful
  private boolean mandatory;
}
