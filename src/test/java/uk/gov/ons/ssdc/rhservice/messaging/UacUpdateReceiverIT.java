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
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubHelper;
import uk.gov.ons.ssdc.rhservice.utils.FireStorePoller;
import uk.gov.ons.ssdc.rhservice.utils.UacNotFoundException;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UacUpdateReceiverIT {
  @Value("${queueconfig.uac-update-topic}")
  private String uacUpdateTopic;

  @Autowired private PubsubHelper pubsubHelper;

  @Autowired private FireStorePoller fireStorePoller;

  @Test
  public void testUacUpdateReceived() throws CTPException, UacNotFoundException {

    // GIVEN
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(UUID.randomUUID().toString());
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    uacUpdateDTO.setQid("000000000001");
    uacUpdateDTO.setUacHash("blah");

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUacUpdateDTO(uacUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubHelper.sendMessageToSharedProject(uacUpdateTopic, event);

    // THEN

    Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash());

    Assertions.assertTrue(uacOpt.isPresent());

    System.out.println("FOUND UAC");
    assertThat(uacOpt.get()).isEqualTo(uacUpdateDTO);
  }
}
