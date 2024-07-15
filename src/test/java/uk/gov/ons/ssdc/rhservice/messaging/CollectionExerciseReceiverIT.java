package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.exceptions.CollectionExerciseNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.testutils.FireStorePoller;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubTestHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CollectionExerciseReceiverIT {

  @Value("${queueconfig.collection-exercise-update-topic}")
  private String collectionExerciseTopic;

  @Autowired private PubsubTestHelper pubsubTestHelper;

  @Autowired private FireStorePoller fireStorePoller;

  @Test
  void testCollectionExerciseUpdateReceived() throws CollectionExerciseNotFoundException {
    // GIVEN
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();
    collectionExerciseUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setCollectionExerciseUpdate(collectionExerciseUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToPubsubProject(collectionExerciseTopic, event);

    // THEN
    Optional<CollectionExerciseUpdateDTO> collexOpt =
        fireStorePoller.getCollectionExerciseById(
            collectionExerciseUpdateDTO.getCollectionExerciseId());
    Assertions.assertTrue(collexOpt.isPresent());
    assertThat(collexOpt.get()).isEqualTo(collectionExerciseUpdateDTO);
  }
}
