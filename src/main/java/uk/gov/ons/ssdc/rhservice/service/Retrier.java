package uk.gov.ons.ssdc.rhservice.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@Component
public class Retrier {

  private FirestoreDataStore firestoreDataStore;

  public Retrier(FirestoreDataStore cloudDataStore) {
    this.firestoreDataStore = cloudDataStore;
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
    firestoreDataStore.storeObject(schema, key, value);
  }
}
