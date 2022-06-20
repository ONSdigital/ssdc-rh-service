package uk.gov.ons.ssdc.rhservice.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyStore {

  private final List<Key> keys;

  @SuppressWarnings("deprecation")
  public KeyStore(@Value("${keystore}") String cryptoKeys) {

    ObjectMapper mapper = new ObjectMapper();
    try {
      keys = Arrays.stream(mapper.readValue(cryptoKeys, Key[].class)).toList();
    } catch (Exception e) {
      System.out.println("Failed to read cryptographic keys");
      throw new RuntimeException("Failed to read cryptographic keys", e);
    }
  }

  public Optional<Key> getKeyForPurposeAndType(String purpose, String type) {
    return keys.stream()
        .filter(x -> (x.getPurpose().equals(purpose) && x.getType().equals(type)))
        .findFirst();
  }

  public Optional<Key> getKeyById(String keyId) {
    return keys.stream().filter(key -> (key.getKeyId().equals(keyId))).findFirst();
  }
}
