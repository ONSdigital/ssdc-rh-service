package uk.gov.ons.ssdc.rhservice.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@Service
public class RetryableCloudDataStore {
  private static final Logger log = LoggerFactory.getLogger(RetryableCloudDataStore.class);

  private FirestoreDataStore cloudDataStore;
  private Retrier retrier;

  @Autowired
  public RetryableCloudDataStore(FirestoreDataStore cloudDataStore, Retrier retrier) {
    this.cloudDataStore = cloudDataStore;
    this.retrier = retrier;
  }

  public void storeObject(
      final String schema, final String key, final Object value, final String id)
      throws RuntimeException {
    try {
      retrier.store(schema, key, value);
    } catch (DataStoreContentionException | RuntimeException e) {
      String identity = value.getClass().getSimpleName() + ": " + id;
      System.out.println("Data store error");
      System.out.println("Exception message: " + e.getMessage());

      throw new RuntimeException("Failed to Store Object");

      //      log.error(
      //          "Retries exhausted for storage",
      //          kv("key", key),
      //          kv("schema", schema),
      //          kv("indentity", identity),
      //          e);
      //      throw new CTPException(Fault.SYSTEM_ERROR, e, "Retries exhausted for storage of " +
      // identity);
    }
  }

  public <T> Optional<T> retrieveObject(Class<T> target, final String schema, final String key) {
    return cloudDataStore.retrieveObject(target, schema, key);
  }

  public <T> List<T> list(Class<T> target, String schema) throws CTPException {
    return cloudDataStore.list(target, schema);
  }

  public <T> List<T> search(
      Class<T> target, final String schema, String[] fieldPathElements, String searchValue) {
    return cloudDataStore.search(target, schema, fieldPathElements, searchValue);
  }

  public Set<String> getCollectionNames() {
    return cloudDataStore.getCollectionNames();
  }

  /**
   * We need another class for the retryable annotation, since calling a retryable annotated within
   * the same class does not honour the annotations.
   */
  @Component
  static class Retrier {

    private FirestoreDataStore cloudDataStore;

    public Retrier(FirestoreDataStore cloudDataStore) {
      this.cloudDataStore = cloudDataStore;
    }

    @Retryable(
        label = "storeObject",
        include = DataStoreContentionException.class,
        backoff =
            @Backoff(
                delayExpression = "${cloud-storage.backoff.initial}",
                multiplierExpression = "${cloud-storage.backoff.multiplier}",
                maxDelayExpression = "${cloud-storage.backoff.max}"),
        maxAttemptsExpression = "${cloud-storage.backoff.max-attempts}",
        listeners = "cloudRetryListener")
    public void store(final String schema, final String key, final Object value)
        throws RuntimeException, DataStoreContentionException {
      cloudDataStore.storeObject(schema, key, value);
    }
  }
}
