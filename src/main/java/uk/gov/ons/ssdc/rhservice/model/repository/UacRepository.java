package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
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

  /**
   * Stores a UAC object into the cloud data store.
   *
   * @param uac - object to be stored in the cloud
   * @throws CTPException - if a cloud exception was detected.
   */
  public void writeUAC(final UacUpdateDTO uac) throws CTPException {
    retryableCloudDataStore.storeObject(uacSchema, uac.getUacHash(), uac, uac.getCaseId());
  }

  /**
   * Read a UAC object from cloud.
   *
   * @param universalAccessCodeHash - the hash of the unique id of the object stored
   * @return - deserialised version of the stored object
   * @throws CTPException - if a cloud exception was detected.
   */
  public Optional<UacUpdateDTO> readUAC(final String universalAccessCodeHash) throws CTPException {
    return retryableCloudDataStore.retrieveObject(
        UacUpdateDTO.class, uacSchema, universalAccessCodeHash);
  }
}
