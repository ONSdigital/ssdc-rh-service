package uk.gov.ons.ssdc.rhservice.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.exceptions.DataStoreContentionException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class RetryableCloudDataStoreIT {

    /*
     Why are we IT testing at this level? because we need to check the retry functionality,
     this is difficult or impossible to do without mocking out the actual firestore
     */

    @MockBean
    FirestoreProvider firestoreProvider;

    @Autowired
    private RetryableCloudDataStore retryableCloudDataStore;

    @Value("${cloud-storage.case-schema-name}")
    private String caseSchemaName;

    @Test
    public void testRetryTimesOut() throws DataStoreContentionException {
        CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
        caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
        caseUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
        caseUpdateDTO.setSample(Map.of("Hello", "friends"));

        StatusRuntimeException statusRuntimeException = new StatusRuntimeException(Status.UNAVAILABLE);

        when(firestoreProvider.get()).thenThrow(statusRuntimeException);

        RuntimeException thrown =
                assertThrows(RuntimeException.class,
                        () -> retryableCloudDataStore.storeObject(caseSchemaName, caseUpdateDTO.getCaseId(),
                                caseUpdateDTO, caseUpdateDTO.getCaseId()));

        assertThat(thrown.getMessage())
                .isEqualTo("Failed to Store Object");

        verify(firestoreProvider, times(5)).get();
    }

}