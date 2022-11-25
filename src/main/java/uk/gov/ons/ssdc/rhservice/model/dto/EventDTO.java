package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
// @Builder
// @NoArgsConstructor
public class EventDTO {
  private EventHeaderDTO header;
  private PayloadDTO payload;
}
