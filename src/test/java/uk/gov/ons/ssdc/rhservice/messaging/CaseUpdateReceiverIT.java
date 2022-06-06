package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubHelper;
import uk.gov.ons.ssdc.rhservice.utils.CaseNotFoundException;
import uk.gov.ons.ssdc.rhservice.utils.FireStorePoller;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CaseUpdateReceiverIT {

  @Value("${queueconfig.case-update-topic}")
  private String caseUpdateTopic;

  @Autowired private PubsubHelper pubsubHelper;

  @Autowired private FireStorePoller fireStorePoller;

  @Autowired private CaseRepository caseRepository;

  @BeforeEach
  public void setUp() {
    //    deleteDataHelper.deleteAllData();
  }

  @Test
  public void testCaseUpdateReceived() throws CTPException, CaseNotFoundException {
    // GIVEN
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
    caseUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    caseUpdateDTO.setSample(Map.of("Hello", "friends"));
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setCaseUpdateDTO(caseUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubHelper.sendMessageToSharedProject(caseUpdateTopic, event);

    Optional<CaseUpdateDTO> cazeOpt = fireStorePoller.getCaseById(caseUpdateDTO.getCaseId());

    Assertions.assertTrue(cazeOpt.isPresent());

    System.out.println("FOUND CASE");
    assertThat(cazeOpt.get()).isEqualTo(caseUpdateDTO);
  }
}
