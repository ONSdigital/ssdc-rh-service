package uk.gov.ons.ssdc.rhservice.model.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionInstrumentSelectionRule implements Serializable {
  private String collectionInstrumentUrl;
  private List<EqLaunchSettings> eqLaunchSettings;
}
