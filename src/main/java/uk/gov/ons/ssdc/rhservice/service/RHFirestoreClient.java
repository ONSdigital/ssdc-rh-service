package uk.gov.ons.ssdc.rhservice.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@Component
public class RHFirestoreClient {
  private final RHFirestoreProvider RHFirestoreProvider;
  private final RHFirestoreDataPersist rhFirestoreDataPersist;

  public RHFirestoreClient(
      RHFirestoreProvider RHFirestoreProvider, RHFirestoreDataPersist rhFirestoreDataPersist) {
    this.RHFirestoreProvider = RHFirestoreProvider;
    this.rhFirestoreDataPersist = rhFirestoreDataPersist;
  }

  public void storeObject(final String schema, final String key, final Object value) {
    try {
      rhFirestoreDataPersist.storeObjectRetryable(schema, key, value);
    } catch (DataStoreContentionException e) {
      throw new RuntimeException("Data Contention Error", e);
    }
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
