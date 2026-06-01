package uk.gov.ons.ssdc.rhservice.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.List;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

// This is in its own class as Spring Retryable doesn't work otherwise
@Component
public class RHFirestoreDataPersist {

  // Firestore limits the amount of writes per batch:
  // https://cloud.google.com/firestore/quotas#writes_and_transactions
  public static final int MAX_WRITES_PER_BATCH = 500;

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

  @Retryable(
      label = "deleteBatchRetryable",
      include = DataStoreContentionException.class,
      backoff =
          @Backoff(
              delayExpression = "${cloud-storage.backoff.initial}",
              multiplierExpression = "${cloud-storage.backoff.multiplier}",
              maxDelayExpression = "${cloud-storage.backoff.max}"),
      maxAttemptsExpression = "${cloud-storage.backoff.max-attempts}",
      listeners = {"retryListener"})
  public void deleteBatchRetryable(final String schema, final String id)
      throws RuntimeException, DataStoreContentionException {
    try {
      Firestore db = rhFirestoreProvider.get();

      BulkWriter bulkWriter = db.bulkWriter();
      CollectionReference collection = db.collection(schema);

      bulkWriter.addWriteErrorListener(
          e -> e.getFailedAttempts() > 5 && isRetryableFirestoreException(e));

      DocumentSnapshot lastDoc = null;

      while (true) {
        // Can only query up to 500 documents at a time, so we need to loop until we've deleted them all
        Query query =
            collection.whereEqualTo("collectionExerciseId", id).limit(MAX_WRITES_PER_BATCH);

        if (lastDoc != null) {
          query.startAfter(lastDoc);
        }

        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();

        if (docs.isEmpty()) break;

        for (QueryDocumentSnapshot doc : docs) {
          bulkWriter.delete(doc.getReference());
        }
        // There isn't a need to auto flush or commit since it's handeld automatically
        // This might be useful if we want to ensure a batch is fully completed before starting the next
        // bulkWriter.flush().get();

        lastDoc = docs.get(docs.size() - 1);
      }

      bulkWriter.close();

    } catch (Exception e) {
      if (isRetryableFirestoreException(e)) {
        throw new DataStoreContentionException(
            "Firestore contention on schema '" + schema + "'", e);
      }

      throw new RuntimeException("Failed to delete object in Firestore. Schema: " + schema, e);
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
