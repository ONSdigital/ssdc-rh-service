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
  private final CaseRepository respondentCaseRepo;

  public CaseUpdateReceiver(CaseRepository respondentCaseRepo) {
    this.respondentCaseRepo = respondentCaseRepo;
  }

  @Transactional
  //  @ServiceActivator(inputChannel = "caseUpdateInputChannel", adviceChain = "retryAdvice")
  @ServiceActivator(inputChannel = "caseUpdateInputChannel")
  public void receiveMessage(Message<byte[]> message) {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());

    CaseUpdateDTO caseUpdate = event.getPayload().getCaseUpdateDTO();

    System.out.println("CASE UPDATE, ID: " + caseUpdate.getCaseId());

    respondentCaseRepo.writeCaseUpdate(caseUpdate);
  }
}
