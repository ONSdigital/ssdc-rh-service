package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.RemoveDataDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@MessageEndpoint
public class RemovePersonalDataReceiver {

  private final CaseRepository caseRepository;

  private final UacRepository uacRepository;

  public RemovePersonalDataReceiver(CaseRepository caseRepository, UacRepository uacRepository) {
    this.caseRepository = caseRepository;
    this.uacRepository = uacRepository;
  }

  private static final Logger log = LoggerFactory.getLogger(RemovePersonalDataReceiver.class);

  @Transactional
  @ServiceActivator(inputChannel = "removePersonalDataUpdateChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {

    log.info("Received remove personal data, processing...");

    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    RemoveDataDTO removeData = event.getPayload().getRemoveData();

    log.info(event.toString());
    log.info(removeData.toString());
    caseRepository.deleteCaseBatchUpdate(removeData.getCollectionExerciseId());
    log.info("Finished deleting cases");

    log.info("deleting UACs...");
    uacRepository.deleteCaseBatchUpdate(removeData.getCollectionExerciseId());
    log.info("Finished deleting UACs");
  }
}
