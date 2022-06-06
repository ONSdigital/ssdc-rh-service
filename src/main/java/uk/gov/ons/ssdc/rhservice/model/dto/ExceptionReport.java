package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class ExceptionReport {
  private String messageHash;
  private String service;
  private String subscription;
  private String exceptionClass;
  private String exceptionMessage;
  private String exceptionRootCause;
}
