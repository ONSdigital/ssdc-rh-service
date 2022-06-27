package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;

@MessageEndpoint
public class CaseUpdateReceiver {
  private final CaseRepository caseRepository;

  public CaseUpdateReceiver(CaseRepository caseRepository) {
    this.caseRepository = caseRepository;
  }

  @Transactional
  @ServiceActivator(inputChannel = "caseUpdateInputChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    CaseUpdateDTO caseUpdate = event.getPayload().getCaseUpdate();
    caseRepository.writeCaseUpdate(caseUpdate);
  }
}
