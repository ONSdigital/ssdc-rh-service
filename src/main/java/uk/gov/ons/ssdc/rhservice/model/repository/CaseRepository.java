package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RetryableCloudDataStore;

/** A Repository implementation for CRUD operations on Case data entities */
@Service
public class CaseRepository {
  private RetryableCloudDataStore retryableCloudDataStore;

  @Value("${spring.cloud.gcp.firestore.project-id}")
  private String gcpProject;

  @Value("${cloud-storage.case-schema-name}")
  private String caseSchemaName;

  String caseSchema;

  private static final String SEARCH_SAMPLE_PATH = "sample";

  @PostConstruct
  public void init() {
    caseSchema = gcpProject + "-" + caseSchemaName.toLowerCase();
  }

  @Autowired
  public CaseRepository(RetryableCloudDataStore retryableCloudDataStore) {
    this.retryableCloudDataStore = retryableCloudDataStore;
  }

  public void writeCaseUpdate(final CaseUpdateDTO caseUpdate) {
    String id = caseUpdate.getCaseId();
    retryableCloudDataStore.storeObject(caseSchema, id, caseUpdate, id);
  }

  public Optional<CaseUpdateDTO> readCaseUpdate(final String caseId) throws CTPException {
    return retryableCloudDataStore.retrieveObject(CaseUpdateDTO.class, caseSchema, caseId);
  }

  //  public List<CaseUpdateDTO> findCaseUpdatesBySampleAttribute(
  //      final String searchAttributeName, final String searchValue, boolean onlyValid)
  //      throws CTPException {
  //
  //    String[] searchPath = {SEARCH_SAMPLE_PATH, searchAttributeName};
  //
  //    List<CaseUpdateDTO> searchResults =
  //        retryableCloudDataStore.search(CaseUpdateDTO.class, caseSchema, searchPath,
  // searchValue);
  //    return filterValidCaseUpdateSearchResults(searchResults, onlyValid);
  //  }
  //
  //  private List<CaseUpdateDTO> filterValidCaseUpdateSearchResults(
  //      final List<CaseUpdateDTO> searchResults, boolean onlyValid) {
  //    return searchResults.stream()
  //        .filter(c -> !onlyValid || !c.isInvalid())
  //        .collect(Collectors.toList());
  //  }
}
