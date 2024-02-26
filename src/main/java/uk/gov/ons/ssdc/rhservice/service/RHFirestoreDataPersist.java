package uk.gov.ons.ssdc.rhservice.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

// This is in its own class as Spring Retryable doesn't work otherwise
@Component
public class RHFirestoreDataPersist {

  private final RHFirestoreProvider rhFirestoreProvider;

  public RHFirestoreDataPersist(RHFirestoreProvider rhFirestoreProvider) {
    this.rhFirestoreProvider = rhFirestoreProvider;
  }

  @Retryable(
      label = "storeObjectRetryable",
      include = DataStoreContentionException.class,
      backoff =
          @Backoff(
              delayExpression = "${cloud-storage.backoff.initial}",
              multiplierExpression = "${cloud-storage.backoff.multiplier}",
              maxDelayExpression = "${cloud-storage.backoff.max}"),
      maxAttemptsExpression = "${cloud-storage.backoff.max-attempts}",
      listeners = {"retryListener"})
  public void storeObjectRetryable(final String schema, final String key, final Object value)
      throws RuntimeException, DataStoreContentionException {

    try {
      ApiFuture<WriteResult> result =
          rhFirestoreProvider.get().collection(schema).document(key).set(value);
      result.get();
    } catch (Exception e) {
      if (isRetryableFirestoreException(e)) {
        throw new DataStoreContentionException(
            "Firestore contention on schema '" + schema + "'", e);
      }

      throw new RuntimeException(
          "Failed to create object in Firestore. Schema: " + schema + " with key " + key, e);
    }
  }

  private boolean isRetryableFirestoreException(Exception e) {
    // Traverse the exception chain looking for a StatusRuntimeException
    Throwable t = e;
    while (t != null) {
      if (t instanceof StatusRuntimeException) {
        StatusRuntimeException statusRuntimeException = (StatusRuntimeException) t;
        Status.Code failureCode = statusRuntimeException.getStatus().getCode();

        if (failureCode == Status.RESOURCE_EXHAUSTED.getCode()
            || failureCode == Status.ABORTED.getCode()
            || failureCode == Status.DEADLINE_EXCEEDED.getCode()
            || failureCode == Status.UNAVAILABLE.getCode()) {
          return true;
        }
      }

      //  Get the next level of exception
      t = t.getCause();
    }

    return false;
  }
}
