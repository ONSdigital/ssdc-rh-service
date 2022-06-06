package uk.gov.ons.ssdc.rhservice.service;

import static java.util.stream.Collectors.toList;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@Service
public class FirestoreDataStore {
  private static final Logger log = LoggerFactory.getLogger(FirestoreDataStore.class);

  @Autowired private FirestoreProvider provider;

  public void storeObject(final String schema, final String key, final Object value)
      throws RuntimeException, DataStoreContentionException {

    // Store the object
    ApiFuture<WriteResult> result = provider.get().collection(schema).document(key).set(value);

    // Wait for Firestore to complete
    try {
      result.get();
    } catch (Exception e) {
      log.error("Failed to store Object: " + e.getMessage());

      if (isRetryableFirestoreException(e)) {
        throw new DataStoreContentionException(
            "Firestore contention on schema '" + schema + "'", e);
      }

      throw new RuntimeException(
          "Failed to create object in Firestore. Schema: " + schema + " with key " + key);
    }
  }

  // This method supports logging which aims to protect against future unexpected changes in
  // how google throw exceptions for retryable operations. If Google change Firestore behaviour
  // and we don't detect a retryable operation then we want our logging to be good enough to
  // allow a code fix.
  private String describeExceptionChain(Throwable e) {
    StringBuilder builder = new StringBuilder();

    while (e != null) {
      builder.append("caused by " + e.getClass().getName() + " ");
      e = e.getCause();
    }

    return builder.toString().trim();
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
          retryable = true;
          break;
        }
        // Else Retryable Exception
      }

      t = t.getCause();
    }

    return retryable;
  }

  public <T> Optional<T> retrieveObject(Class<T> target, final String schema, final String key)
      throws RuntimeException {
    //    log.info("Fetching object from Firestore", kv("schema", schema), kv("key", key));

    // Submit read request to firestore
    FieldPath fieldPathForId = FieldPath.documentId();
    List<T> documents = runSearch(target, schema, fieldPathForId, key);

    // Squash results down to single document
    Optional<T> result = null;
    if (documents.isEmpty()) {
      result = Optional.empty();
    } else if (documents.size() == 1) {
      result = Optional.of(documents.get(0));
    } else {
      String failureMessage =
          "Firestore returned more than 1 result object. Returned "
              + documents.size()
              + " objects for Schema '"
              + schema
              + "' with key '"
              + key
              + "'";
      throw new RuntimeException(failureMessage);
    }
    return result;
  }

  public <T> List<T> list(Class<T> target, String schema) throws RuntimeException {
    try {
      ApiFuture<QuerySnapshot> query = provider.get().collection(schema).get();
      QuerySnapshot querySnapshot = query.get();
      List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
      return documents.stream().map(d -> d.toObject(target)).collect(toList());
    } catch (Exception e) {
      String failureMessage =
          "Failed to list Firestore items. Target class: '"
              + target
              + "', schema: '"
              + schema
              + "'";
      throw new RuntimeException(failureMessage);
    }
  }

  public <T> List<T> search(
      Class<T> target, final String schema, String[] fieldPathElements, String searchValue)
      throws RuntimeException {

    // Run a query for a custom search path
    FieldPath fieldPath = FieldPath.of(fieldPathElements);
    List<T> r = runSearch(target, schema, fieldPath, searchValue);
    return r;
  }

  private <T> List<T> runSearch(
      Class<T> target, final String schema, FieldPath fieldPath, String searchValue)
      throws RuntimeException {
    // Run a query
    ApiFuture<QuerySnapshot> query =
        provider.get().collection(schema).whereEqualTo(fieldPath, searchValue).get();

    // Wait for query to complete and get results
    QuerySnapshot querySnapshot;
    try {
      querySnapshot = query.get();
    } catch (Exception e) {
      String failureMessage =
          "Failed to search schema '" + schema + "' by field '" + "'" + fieldPath;
      throw new RuntimeException(failureMessage);
    }
    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    // Convert the results to Java objects
    List<T> results;
    try {
      results = documents.stream().map(d -> d.toObject(target)).collect(Collectors.toList());
    } catch (Exception e) {
      String failureMessage =
          "Failed to convert Firestore result to Java object. Target class '" + target + "'";
      throw new RuntimeException(failureMessage);
    }

    return results;
  }

  public void deleteObject(final String schema, final String key) throws RuntimeException {
    //    log.info("Deleting object from Firestore", kv("schema", schema), kv("key", key));

    // Tell firestore to delete object
    DocumentReference docRef = provider.get().collection(schema).document(key);
    ApiFuture<WriteResult> result = docRef.delete();

    // Wait for delete to complete
    try {
      result.get();
    } catch (Exception e) {
      String failureMessage =
          "Failed to delete object from Firestore. Schema: " + schema + " with key " + key;
      throw new RuntimeException(failureMessage);
    }
  }

  public Set<String> getCollectionNames() {
    Set<String> collectionNames = new HashSet<>();
    provider.get().listCollections().forEach(c -> collectionNames.add(c.getId()));
    return collectionNames;
  }
}
