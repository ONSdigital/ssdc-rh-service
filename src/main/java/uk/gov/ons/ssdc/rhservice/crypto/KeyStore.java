package uk.gov.ons.ssdc.rhservice.crypto;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyStore {

  // TODO: load in with @Value CryptoKeys and use in Constructor
  @Value("${keystore}")
  private String cryptoKeys;

  private Keys keys;

  @SuppressWarnings("deprecation")
  @PostConstruct
  public void setUpKeys() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    try {
      keys = mapper.readValue(cryptoKeys, Keys.class);
    } catch (Exception e) {
      System.out.println("Failed to read cryptographic keys");
      throw new RuntimeException("Failed to read cryptographic keys");
    }
    keys.getKeys()
        .forEach(
            (key, value) -> {
              value.setKid(key);
            });
  }

  /**
   * Gets a list of keys that match the purpose and type and returns the first key in that list.
   *
   * @param purpose Purpose of key e.g. authentication
   * @param type e.g. private or public
   * @return Optional containing Key if match found
   */
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

  /**
   * Get key by Id
   *
   * @param kid key Id
   * @return optional key
   */
  public Optional<Key> getKeyById(String kid) {
    if (keys.getKeys().containsKey(kid)) {
      return Optional.of(keys.getKeys().get(kid));
    } else {
      return Optional.empty();
    }
  }
}
