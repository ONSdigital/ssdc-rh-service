package uk.gov.ons.ssdc.rhservice.crypto;

import lombok.Data;

import java.util.Map;

/** Holder for Cryptographic keys */
@Data
public class Keys {

  private Map<String, Key> keys;
}
