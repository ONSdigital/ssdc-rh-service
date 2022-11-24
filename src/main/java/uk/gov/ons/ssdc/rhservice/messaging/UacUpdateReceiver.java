package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@MessageEndpoint
public class UacUpdateReceiver {
  private final UacRepository uacRepository;

  public UacUpdateReceiver(UacRepository uacRepository) {
    this.uacRepository = uacRepository;
  }

  @ServiceActivator(inputChannel = "uacUpdateInputChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    UacUpdateDTO uacUpdateDTO = event.getPayload().getUacUpdate();


    // If this UAC is being marked inactive, then don't stamp fields


    // This isn't 100% idempotent - PubSub does 'at least once' delivery.  It's possible that we could get the same
    // UacUpdate message twice, as we're relying on case data from a different table, we could stamp different data on the 2nd one
    // However this seems highly unlikely timings wise.

    //  If we checked if the UAC already existed, then for PHM the desired behaviour if still active is to preserve the original launchMetaData?
    // test how Firestore works with this.



        Map<String,String> phmFieldsToStamp = getPHMFieldsToStampFromCase(uacUpdateDTO))

    uacRepository.writeUAC(uacUpdateDTO);
  }


}
