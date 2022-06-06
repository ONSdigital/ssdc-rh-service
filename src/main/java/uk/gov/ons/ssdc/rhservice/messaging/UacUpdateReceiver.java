package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

/**
 * Service implementation responsible for receipt of UAC Events. See Spring Integration flow for
 * details of in bound queue.
 */
@MessageEndpoint
public class UacUpdateReceiver {
  private UacRepository respondentUacRepo;

  public UacUpdateReceiver(UacRepository respondentUacRepo) {
    this.respondentUacRepo = respondentUacRepo;
  }

  @ServiceActivator(inputChannel = "uacUpdateInputChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) throws CTPException {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    UacUpdateDTO uacUpdateDTO = event.getPayload().getUacUpdateDTO();
    respondentUacRepo.writeUAC(uacUpdateDTO);
  }
}
