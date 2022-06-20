package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RHFirestoreClient;

@Service
public class UacRepository {

  private final RHFirestoreClient rhFirestoreClient;

  @Value("${cloud-storage.uac-schema-name}")
  private String uacSchemaName;

  @Autowired
  public UacRepository(RHFirestoreClient rhFirestoreClient) {
    this.rhFirestoreClient = rhFirestoreClient;
  }

  public void writeUAC(final UacUpdateDTO uac) {
    rhFirestoreClient.storeObject(uacSchemaName, uac.getUacHash(), uac);
  }

  public Optional<UacUpdateDTO> readUAC(final String universalAccessCodeHash) {
    return rhFirestoreClient.retrieveObject(
        UacUpdateDTO.class, uacSchemaName, universalAccessCodeHash);
  }
}
