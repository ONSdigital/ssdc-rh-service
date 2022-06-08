package uk.gov.ons.ssdc.rhservice.model.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RetryableCloudDataStore;

import java.util.Optional;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class CaseRepositoryTest {
    public static final String TEST_CASE_SCHEMA = "testCaseSchema";

    @Mock
    RetryableCloudDataStore retryableCloudDataStore;

    @InjectMocks
    CaseRepository caseRepository;

    @BeforeAll
    public void setUp() {
        ReflectionTestUtils.setField(caseRepository, "caseSchemaName", TEST_CASE_SCHEMA);
    }

    @Test
    public void testWriteCaseUpdate() {
        CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
        caseUpdateDTO.setCaseId(UUID.randomUUID().toString());

        caseRepository.writeCaseUpdate(caseUpdateDTO);
    }

//
//
//    @Value("${cloud-storage.case-schema-name}")
//    private String caseSchemaName;
//

//
//    public void writeCaseUpdate(final CaseUpdateDTO caseUpdate) {
//        String id = caseUpdate.getCaseId();
//        retryableCloudDataStore.storeObject(caseSchemaName, id, caseUpdate, id);
//    }
//
//    public Optional<CaseUpdateDTO> readCaseUpdate(final String caseId) {
//        return retryableCloudDataStore.retrieveObject(CaseUpdateDTO.class, caseSchemaName, caseId);
//    }




}