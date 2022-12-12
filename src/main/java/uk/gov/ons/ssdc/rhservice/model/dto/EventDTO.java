package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
  private EventHeaderDTO header;
  private PayloadDTO payload;
}
