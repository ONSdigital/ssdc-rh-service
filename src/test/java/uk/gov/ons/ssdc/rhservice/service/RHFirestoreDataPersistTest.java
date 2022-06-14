package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

@ExtendWith(MockitoExtension.class)
class RHFirestoreDataPersistTest {
  @Mock RHFirestoreProvider RHFirestoreProvider;
  @Mock Firestore firestore;
  @Mock CollectionReference collectionReference;

  @InjectMocks RHFirestoreDataPersist underTest;

  @Test
  void testStoreSuccess()
      throws DataStoreContentionException, ExecutionException, InterruptedException {

    // only used in this test
    ApiFuture<WriteResult> apiFuture = Mockito.mock(ApiFuture.class);
    DocumentReference documentReference = Mockito.mock(DocumentReference.class);

    when(collectionReference.document("ID")).thenReturn(documentReference);
    when(firestore.collection("Schema")).thenReturn(collectionReference);
    when(documentReference.set("Object")).thenReturn(apiFuture);
    when(RHFirestoreProvider.get()).thenReturn(firestore);

    when(apiFuture.get()).thenReturn(null);

    underTest.storeObjectRetryable("Schema", "ID", "Object");

    // Not throwing an exception is a success here
    verify(apiFuture).get();
    verify(RHFirestoreProvider).get();
    verify(documentReference).set("Object");
  }

  @Test
  void testRESOURCE_EXHAUSTED() {
    testRetryableException(Status.RESOURCE_EXHAUSTED);
  }

  @Test
  void testABORTED() {
    testRetryableException(Status.ABORTED);
  }

  @Test
  void testDEADLINE_EXCEEDED() {
    testRetryableException(Status.DEADLINE_EXCEEDED);
  }

  @Test
  void testUNAVAILABLE() {
    testRetryableException(Status.UNAVAILABLE);
  }

  @Test
  void testNonRetryableException() {
    StatusRuntimeException statusRuntimeException =
        new StatusRuntimeException(Status.UNAUTHENTICATED);
    when(RHFirestoreProvider.get()).thenThrow(statusRuntimeException);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> underTest.storeObjectRetryable("blah", "blah", "blah"));

    assertThat(thrown.getCause().getLocalizedMessage()).isEqualTo("UNAUTHENTICATED");
  }

  private void testRetryableException(Status status) {
    StatusRuntimeException statusRuntimeException = new StatusRuntimeException(status);
    when(RHFirestoreProvider.get()).thenThrow(statusRuntimeException);

    DataStoreContentionException thrown =
        assertThrows(
            DataStoreContentionException.class,
            () -> underTest.storeObjectRetryable("blah", "blah", "blah"));

    assertThat(thrown.getCause().getLocalizedMessage()).isEqualTo(status.getCode().toString());
  }
}
