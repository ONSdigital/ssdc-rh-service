package uk.gov.ons.ssdc.rhservice.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@Service
public class RHFirestoreClient {
  private static final Logger log = LoggerFactory.getLogger(RHFirestoreClient.class);
  private final RHFirestoreProvider RHFirestoreProvider;

  public RHFirestoreClient(RHFirestoreProvider RHFirestoreProvider) {
    this.RHFirestoreProvider = RHFirestoreProvider;
  }

  public void storeObject(final String schema, final String key, final Object value) {
    try {
      storeObjectRetryable(schema, key, value);
    } catch (DataStoreContentionException e) {
      throw new RuntimeException("Data Contention Error", e);
    }
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
  private void storeObjectRetryable(final String schema, final String key, final Object value)
      throws RuntimeException, DataStoreContentionException {

    try {
      ApiFuture<WriteResult> result =
          RHFirestoreProvider.get().collection(schema).document(key).set(value);
      result.get();
    } catch (Exception e) {
      log.error("Failed to store Object: " + e.getMessage());

      if (isRetryableFirestoreException(e)) {
        throw new DataStoreContentionException(
            "Firestore contention on schema '" + schema + "'", e);
      }

      throw new RuntimeException(
          "Failed to create object in Firestore. Schema: " + schema + " with key " + key, e);
    }
  }

  private boolean isRetryableFirestoreException(Exception e) {
    boolean retryable = false;

    // Traverse the exception chain looking for a StatusRuntimeException
    Throwable t = e;
    while (t != null) {
      if (t instanceof StatusRuntimeException) {
        StatusRuntimeException statusRuntimeException = (StatusRuntimeException) t;
        Code failureCode = statusRuntimeException.getStatus().getCode();

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

    return retryable;
  }

  public <T> Optional<T> retrieveObject(Class<T> target, final String schema, final String key)
      throws RuntimeException {

    List<T> documents = runSearch(target, schema, FieldPath.documentId(), key);

    if (documents.isEmpty()) {
      return Optional.empty();
    } else if (documents.size() == 1) {
      return Optional.of(documents.get(0));
    } else {
      throw new RuntimeException(
          String.format(
              "Firestore returned more than 1 result object. Returned: %s objects for Schema: %s with key: %s",
              documents.size(), schema, key));
    }
  }

  private <T> List<T> runSearch(
      Class<T> targetClass, final String schema, FieldPath fieldPathForId, String searchValue)
      throws RuntimeException {

    ApiFuture<QuerySnapshot> querySnapshotApiFuture =
        RHFirestoreProvider.get()
            .collection(schema)
            .whereEqualTo(fieldPathForId, searchValue)
            .get();

    List<QueryDocumentSnapshot> documents;

    try {
      documents = querySnapshotApiFuture.get().getDocuments();
    } catch (Exception e) {
      String failureMessage =
          "Failed to search schema '" + schema + "' by field '" + "'" + fieldPathForId;
      throw new RuntimeException(failureMessage, e);
    }

    return convertToObjects(targetClass, documents);
  }

  private <T> List<T> convertToObjects(Class<T> target, List<QueryDocumentSnapshot> documents) {
    try {
      return documents.stream().map(d -> d.toObject(target)).collect(Collectors.toList());
    } catch (Exception e) {
      String failureMessage =
          "Failed to convert Firestore result to Java object. Target class '" + target + "'";
      throw new RuntimeException(failureMessage, e);
    }
  }
}
