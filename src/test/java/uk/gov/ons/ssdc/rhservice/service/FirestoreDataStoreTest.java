package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class FirestoreDataStoreTest {

    @Mock
    FirestoreProvider firestoreProvider;
    @InjectMocks
    FirestoreDataStore underTest;

    @Test
    public void testStoreSuccess() throws DataStoreContentionException, ExecutionException, InterruptedException {

        // only used in this test
        ApiFuture<WriteResult> apiFuture = Mockito.mock(ApiFuture.class);
        Firestore firestore = Mockito.mock(Firestore.class);
        DocumentReference documentReference = Mockito.mock(DocumentReference.class);
        CollectionReference collectionReference = Mockito.mock(CollectionReference.class);

        when(collectionReference.document("ID")).thenReturn(documentReference);
        when(firestore.collection("Schema")).thenReturn(collectionReference);
        when(documentReference.set("Object")).thenReturn(apiFuture);
        when(firestoreProvider.get()).thenReturn(firestore);

        when(apiFuture.get()).thenReturn(null);

        underTest.storeObject("Schema", "ID", "Object");

        // Not throwing an exception is a success here
        verify(apiFuture).get();
        verify(firestoreProvider).get();
        verify(documentReference).set("Object");
    }

    @Test
    public void test_retry_exceptions() {
        testRetryableException(Status.RESOURCE_EXHAUSTED);
        testRetryableException(Status.ABORTED);
        testRetryableException(Status.DEADLINE_EXCEEDED);
        testRetryableException(Status.UNAVAILABLE);
    }

    private void testRetryableException(Status status) {
        StatusRuntimeException statusRuntimeException = new StatusRuntimeException(status);
        when(firestoreProvider.get()).thenThrow(statusRuntimeException);

        DataStoreContentionException thrown =
                assertThrows(
                        DataStoreContentionException.class,
                        () -> underTest.storeObject("blah", "blah", "blah"));

        assertThat(thrown.getCause().getLocalizedMessage()).isEqualTo(status.getCode().toString());

    }

}
