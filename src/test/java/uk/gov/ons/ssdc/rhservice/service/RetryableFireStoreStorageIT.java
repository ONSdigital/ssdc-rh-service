package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.google.cloud.firestore.Firestore;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class RetryableFireStoreStorageIT {

  /*
  Why are we IT testing at this level? because we need to check the retry functionality,
  this is difficult or impossible to do without mocking out the actual firestore
  */

  @MockitoBean RHFirestoreProvider RHFirestoreProvider;

  @MockitoBean Firestore firestore;

  @Autowired private RHFirestoreClient rhFirestoreClient;

  @Value("${cloud-storage.case-schema-name}")
  private String caseSchemaName;

  @Test
  void testRetryTimesOut() {
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
    caseUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    caseUpdateDTO.setSample(Map.of("Hello", "friends"));

    StatusRuntimeException statusRuntimeException = new StatusRuntimeException(Status.UNAVAILABLE);

    when(RHFirestoreProvider.get()).thenThrow(statusRuntimeException);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () ->
                rhFirestoreClient.storeObject(
                    caseSchemaName, caseUpdateDTO.getCaseId(), caseUpdateDTO));

    assertThat(thrown.getMessage()).isEqualTo("Data Contention Error");

    verify(RHFirestoreProvider, times(5)).get();
  }
}
