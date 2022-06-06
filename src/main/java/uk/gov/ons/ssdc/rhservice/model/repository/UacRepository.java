package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RetryableCloudDataStore;

/** A Repository implementation for CRUD operations on UAC data entities */
@Service
public class UacRepository {

  private RetryableCloudDataStore retryableCloudDataStore;

  @Value("${spring.cloud.gcp.firestore.project-id}")
  private String gcpProject;

  @Value("${cloud-storage.uac-schema-name}")
  private String uacSchemaName;

  private String uacSchema;

  @PostConstruct
  public void init() {
    uacSchema = gcpProject + "-" + uacSchemaName.toLowerCase();
  }

  @Autowired
  public UacRepository(RetryableCloudDataStore retryableCloudDataStore) {
    this.retryableCloudDataStore = retryableCloudDataStore;
  }

  public void writeUAC(final UacUpdateDTO uac) {
    retryableCloudDataStore.storeObject(uacSchema, uac.getUacHash(), uac, uac.getCaseId());
  }

  public Optional<UacUpdateDTO> readUAC(final String universalAccessCodeHash) {
    return retryableCloudDataStore.retrieveObject(
        UacUpdateDTO.class, uacSchema, universalAccessCodeHash);
  }
}
