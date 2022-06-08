package uk.gov.ons.ssdc.rhservice.model.repository;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RetryableCloudDataStore;

@ExtendWith(MockitoExtension.class)
class CaseRepositoryTest {
  public static final String TEST_CASE_SCHEMA = "testCaseSchema";

  @Mock RetryableCloudDataStore retryableCloudDataStore;

  @InjectMocks CaseRepository caseRepository;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(caseRepository, "caseSchemaName", TEST_CASE_SCHEMA);
  }

  @Test
  public void testWriteCaseUpdate() {
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());

    caseRepository.writeCaseUpdate(caseUpdateDTO);
    verify(retryableCloudDataStore)
        .storeObject(
            TEST_CASE_SCHEMA, caseUpdateDTO.getCaseId(), caseUpdateDTO, caseUpdateDTO.getCaseId());
  }
}
