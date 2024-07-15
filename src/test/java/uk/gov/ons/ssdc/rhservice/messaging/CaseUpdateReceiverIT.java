package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
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
import uk.gov.ons.ssdc.rhservice.exceptions.CaseNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.testutils.FireStorePoller;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubTestHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CaseUpdateReceiverIT {

  @Value("${queueconfig.case-update-topic}")
  private String caseUpdateTopic;

  @Autowired private PubsubTestHelper pubsubTestHelper;

  @Autowired private FireStorePoller fireStorePoller;

  @Test
  void testCaseUpdateReceived() throws CaseNotFoundException {
    // GIVEN

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
    caseUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    caseUpdateDTO.setSample(Map.of("Hello", "friends"));
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setCaseUpdate(caseUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToPubsubProject(caseUpdateTopic, event);

    // THEN
    Optional<CaseUpdateDTO> cazeOpt = fireStorePoller.getCaseById(caseUpdateDTO.getCaseId());
    Assertions.assertTrue(cazeOpt.isPresent());
    assertThat(cazeOpt.get()).isEqualTo(caseUpdateDTO);
  }
}
