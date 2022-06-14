package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;

@ExtendWith(MockitoExtension.class)
class RHFirestoreClientTest {

  @Mock RHFirestoreProvider RHFirestoreProvider;
  @Mock Firestore firestore;
  @Mock CollectionReference collectionReference;
  @Mock ApiFuture<QuerySnapshot> querySnapshotApiFuture;
  @Mock Query query;
  @Mock QuerySnapshot querySnapshot;
  @Mock List<QueryDocumentSnapshot> queryDocumentSnapshotList;

  @InjectMocks RHFirestoreClient underTest;

  @Test
  public void testRetrieveSuccess1Document() throws ExecutionException, InterruptedException {
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());

    List<QueryDocumentSnapshot> queryDocumentSnapshotList = new ArrayList<>();
    QueryDocumentSnapshot doc1 = Mockito.mock(QueryDocumentSnapshot.class);
    when(doc1.toObject(eq(CaseUpdateDTO.class))).thenReturn(caseUpdateDTO);
    queryDocumentSnapshotList.add(doc1);

    when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
    when(querySnapshot.getDocuments()).thenReturn(queryDocumentSnapshotList);

    //    firestoreProvider.get().collection(schema).whereEqualTo(fieldPath, searchValue).get();
    FieldPath fieldPathForId = FieldPath.documentId();
    when(query.get()).thenReturn(querySnapshotApiFuture);
    when(collectionReference.whereEqualTo(eq(fieldPathForId), any())).thenReturn(query);
    when(firestore.collection(any())).thenReturn(collectionReference);
    when(RHFirestoreProvider.get()).thenReturn(firestore);

    Optional<CaseUpdateDTO> caseOpt = underTest.retrieveObject(CaseUpdateDTO.class, "CASE", "ID");

    assertThat(caseOpt).isPresent();
    assertThat(caseOpt.get().getCaseId()).isEqualTo(caseUpdateDTO.getCaseId());
  }

  @Test
  public void noDocuments() throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> queryDocumentSnapshotList = new ArrayList<>();

    when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
    when(querySnapshot.getDocuments()).thenReturn(queryDocumentSnapshotList);

    FieldPath fieldPathForId = FieldPath.documentId();
    when(query.get()).thenReturn(querySnapshotApiFuture);
    when(collectionReference.whereEqualTo(eq(fieldPathForId), any())).thenReturn(query);
    when(firestore.collection(any())).thenReturn(collectionReference);
    when(RHFirestoreProvider.get()).thenReturn(firestore);

    Optional<CaseUpdateDTO> caseOpt = underTest.retrieveObject(CaseUpdateDTO.class, "CASE", "ID");

    assertThat(caseOpt).isEmpty();
  }

  @Test
  public void moreThan1DocumentFailure() throws ExecutionException, InterruptedException {
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());

    List<QueryDocumentSnapshot> queryDocumentSnapshotList = new ArrayList<>();
    QueryDocumentSnapshot queryDocumentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
    when(queryDocumentSnapshot.toObject(eq(CaseUpdateDTO.class))).thenReturn(caseUpdateDTO);
    queryDocumentSnapshotList.add(queryDocumentSnapshot);
    queryDocumentSnapshotList.add(queryDocumentSnapshot);

    when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
    when(querySnapshot.getDocuments()).thenReturn(queryDocumentSnapshotList);

    FieldPath fieldPathForId = FieldPath.documentId();
    when(query.get()).thenReturn(querySnapshotApiFuture);
    when(collectionReference.whereEqualTo(eq(fieldPathForId), any())).thenReturn(query);
    when(firestore.collection(any())).thenReturn(collectionReference);
    when(RHFirestoreProvider.get()).thenReturn(firestore);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> underTest.retrieveObject(CaseUpdateDTO.class, "CASE", "ID"));

    assertThat(thrown.getMessage())
        .isEqualTo(
            "Firestore returned more than 1 result object. Returned: 2 objects for Schema: CASE with key: ID");
  }

  @Test
  public void testRetrieveDocumentMappingException()
      throws ExecutionException, InterruptedException {

    when(queryDocumentSnapshotList.stream()).thenThrow(new RuntimeException("TEST exception"));

    when(querySnapshotApiFuture.get()).thenReturn(querySnapshot);
    when(querySnapshot.getDocuments()).thenReturn(queryDocumentSnapshotList);

    FieldPath fieldPathForId = FieldPath.documentId();
    when(query.get()).thenReturn(querySnapshotApiFuture);
    when(collectionReference.whereEqualTo(eq(fieldPathForId), any())).thenReturn(query);
    when(firestore.collection(any())).thenReturn(collectionReference);
    when(RHFirestoreProvider.get()).thenReturn(firestore);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> underTest.retrieveObject(CaseUpdateDTO.class, "CASE", "ID"));

    assertThat(thrown.getMessage())
        .isEqualTo(
            "Failed to convert Firestore result to Java object. Target class 'class uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO'");
  }
}
