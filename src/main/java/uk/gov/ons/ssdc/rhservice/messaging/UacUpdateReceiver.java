package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;
import uk.gov.ons.ssdc.rhservice.survey.specific.LaunhDataFieldSetter;

@MessageEndpoint
public class UacUpdateReceiver {
  private final UacRepository uacRepository;
  private final LaunhDataFieldSetter launhDataFieldSetter;

  public UacUpdateReceiver(UacRepository uacRepository, LaunhDataFieldSetter launhDataFieldSetter) {
    this.uacRepository = uacRepository;
    this.launhDataFieldSetter = launhDataFieldSetter;
  }

  @ServiceActivator(inputChannel = "uacUpdateInputChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    UacUpdateDTO uacUpdateDTO = event.getPayload().getUacUpdate();

    if (uacUpdateDTO.isActive()) {
      launhDataFieldSetter.stampLaunchDataFieldsOnUAC(uacUpdateDTO);
    }

    uacRepository.writeUAC(uacUpdateDTO);
  }
}
