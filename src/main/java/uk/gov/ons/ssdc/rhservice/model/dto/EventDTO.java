package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class EventDTO {
  private EventHeaderDTO header;
  private PayloadDTO payload;
}
