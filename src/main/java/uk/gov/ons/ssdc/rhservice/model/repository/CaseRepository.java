package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RHFirestoreClient;

@Service
public class CaseRepository {
  private final RHFirestoreClient rhFirestoreClient;

  @Value("${cloud-storage.case-schema-name}")
  private String caseSchemaName;

  public CaseRepository(RHFirestoreClient rhFirestoreClient) {
    this.rhFirestoreClient = rhFirestoreClient;
  }

  public void writeCaseUpdate(final CaseUpdateDTO caseUpdate) {
    String id = caseUpdate.getCaseId();
    rhFirestoreClient.storeObject(caseSchemaName, id, caseUpdate);
  }

  public Optional<CaseUpdateDTO> readCaseUpdate(String caseId) {
    return rhFirestoreClient.retrieveObject(CaseUpdateDTO.class, caseSchemaName, caseId);
  }
}
