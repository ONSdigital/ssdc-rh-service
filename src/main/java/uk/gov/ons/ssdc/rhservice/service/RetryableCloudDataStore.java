package uk.gov.ons.ssdc.rhservice.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@Service
public class RetryableCloudDataStore {
  private static final Logger log = LoggerFactory.getLogger(RetryableCloudDataStore.class);

  private FirestoreDataStore firestoreDataStore;
  private Retrier retrier;

  public RetryableCloudDataStore(FirestoreDataStore firestoreDataStore, Retrier retrier) {
    this.firestoreDataStore = firestoreDataStore;
    this.retrier = retrier;
  }

  public void storeObject(
      final String schema, final String key, final Object value, final String id)
      throws RuntimeException {
    try {
      retrier.store(schema, key, value);
    } catch (DataStoreContentionException | RuntimeException e) {
      String identity = value.getClass().getSimpleName() + ": " + id;

      log.error(
          String.format(
              "Retries exhausted for storage. Key: %s, Schema: %s, identity: %s. Exception %s",
              key, schema, identity, e.getMessage()));

      throw new RuntimeException("Failed to Store Object", e);
    }
  }

  public <T> Optional<T> retrieveObject(Class<T> target, final String schema, final String key) {
    return firestoreDataStore.retrieveObject(target, schema, key);
  }
}
