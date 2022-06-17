package uk.gov.ons.ssdc.rhservice.crypto.keys;

import java.util.Map;
import lombok.Data;

/** Holder for Cryptographic keys */
@Data
public class Keys {

  private Map<String, Key> keys;
}
