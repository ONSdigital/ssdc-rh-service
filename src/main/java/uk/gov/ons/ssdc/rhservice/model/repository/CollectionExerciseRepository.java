package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RHFirestoreClient;

@Service
public class CollectionExerciseRepository {
  private final RHFirestoreClient rhFirestoreClient;

  @Value("${cloud-storage.collection-exercise-schema-name}")
  private String collectionExerciseSchemaName;

  public CollectionExerciseRepository(RHFirestoreClient rhFirestoreClient) {
    this.rhFirestoreClient = rhFirestoreClient;
  }

  public void writeCollectionExerciseUpdate(
      final CollectionExerciseUpdateDTO collectionExerciseUpdateDTO) {
    String id = collectionExerciseUpdateDTO.getCollectionExerciseId();
    rhFirestoreClient.storeObject(collectionExerciseSchemaName, id, collectionExerciseUpdateDTO);
  }

  public Optional<CollectionExerciseUpdateDTO> readCollectionExerciseUpdate(
      String collectionExerciseId) {
    return rhFirestoreClient.retrieveObject(
        CollectionExerciseUpdateDTO.class, collectionExerciseSchemaName, collectionExerciseId);
  }
}
