package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class Peek {
  private String messageHash;
  private byte[] messagePayload;
}
