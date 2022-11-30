package uk.gov.ons.ssdc.rhservice.model.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class CollectionInstrumentSelectionRule implements Serializable {
  private int priority;
  private String spelExpression;
  private String collectionInstrumentUrl;
}
