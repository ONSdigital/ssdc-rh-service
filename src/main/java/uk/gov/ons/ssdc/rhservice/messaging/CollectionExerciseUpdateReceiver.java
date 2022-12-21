package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;

@MessageEndpoint
public class CollectionExerciseUpdateReceiver {

  private final CollectionExerciseRepository collectionExerciseRepository;

  public CollectionExerciseUpdateReceiver(
      CollectionExerciseRepository collectionExerciseRepository) {
    this.collectionExerciseRepository = collectionExerciseRepository;
  }

  @Transactional
  @ServiceActivator(inputChannel = "collectionExerciseUpdateChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        event.getPayload().getCollectionExerciseUpdate();
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
  }
}
