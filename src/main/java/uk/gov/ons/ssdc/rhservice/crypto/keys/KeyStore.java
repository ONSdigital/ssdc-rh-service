package uk.gov.ons.ssdc.rhservice.crypto.keys;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyStore {

  // TODO: load in with @Value CryptoKeys and use in Constructor

  private Keys keys;

  @SuppressWarnings("deprecation")
  public KeyStore(@Value("${keystore}") String cryptoKeys) {

    ObjectMapper mapper = new ObjectMapper();
    try {
      keys = mapper.readValue(cryptoKeys, Keys.class);
    } catch (Exception e) {
      System.out.println("Failed to read cryptographic keys");
      throw new RuntimeException("Failed to read cryptographic keys", e);
    }
    keys.getKeys()
        .forEach(
            (key, value) -> {
              value.setKid(key);
            });
  }

  public Optional<Key> getKeyForPurposeAndType(String purpose, String type) {

    List<Key> matching =
        keys.getKeys().values().stream()
            .filter(x -> (x.getPurpose().equals(purpose) && x.getType().equals(type)))
            .collect(Collectors.toList());

    if (!matching.isEmpty()) {
      return Optional.of(matching.get(0));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Key> getKeyById(String keyId) {
    if (keys.getKeys().containsKey(keyId)) {
      return Optional.of(keys.getKeys().get(keyId));
    } else {
      return Optional.empty();
    }
  }
}
