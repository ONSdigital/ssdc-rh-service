package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RetryableCloudDataStore;

/** A Repository implementation for CRUD operations on Case data entities */
@Service
public class CaseRepository {
  private RetryableCloudDataStore retryableCloudDataStore;

  @Value("${cloud-storage.case-schema-name}")
  private String caseSchemaName;

  @Autowired
  public CaseRepository(RetryableCloudDataStore retryableCloudDataStore) {
    this.retryableCloudDataStore = retryableCloudDataStore;
  }

  public void writeCaseUpdate(final CaseUpdateDTO caseUpdate) {
    String id = caseUpdate.getCaseId();
    retryableCloudDataStore.storeObject(caseSchemaName, id, caseUpdate, id);
  }

  public Optional<CaseUpdateDTO> readCaseUpdate(final String caseId) {
    return retryableCloudDataStore.retrieveObject(CaseUpdateDTO.class, caseSchemaName, caseId);
  }
}
