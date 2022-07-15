package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CollectionInstrumentSelectionRule implements Serializable {
    private int priority;
    private String spelExpression;
    private String collectionInstrumentUrl;
}
