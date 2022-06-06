package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
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

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CaseUpdateReceiverIT {

  @Value("${queueconfig.case-update-topic}")
  private String caseUpdateTopic;

  @Autowired private PubsubHelper pubsubHelper;
  //  @Autowired private DeleteDataHelper deleteDataHelper;
  //  @Autowired private JunkDataHelper junkDataHelper;

  @Autowired private CaseRepository caseRepository;

  @BeforeEach
  public void setUp() {
    //    deleteDataHelper.deleteAllData();
  }

  @Test
  public void testDeactivateUacReceiver() throws InterruptedException, CTPException {
    // GIVEN
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setCaseUpdateDTO(caseUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubHelper.sendMessageToSharedProject(caseUpdateTopic, event);

    for (int i = 0; i < 5; i++) {
      Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(caseUpdateDTO.getCaseId());

      if (cazeOpt.isPresent()) {
        System.out.println("FOUND CASE");
        assertThat(cazeOpt.get().getCaseId()).isEqualTo(caseUpdateDTO.getCaseId());
        break;
      }

      System.out.println("WAITING: ... " + i);
      Thread.sleep(1000);
    }

    // THEN
    //      EventDTO actualEvent = uacRhQueue.checkExpectedMessageReceived();
    //
    //      UacUpdateDTO uac = actualEvent.getPayload().getUacUpdate();
    //      assertThat(uac.getQid()).isEqualTo(TEST_QID);
    //      assertThat(uac.isActive()).isFalse();
    //
    //      UacQidLink sentUacQidLinkUpdated = uacQidLinkRepository.findByQid(TEST_QID).get();
    //
    //      assertThat(sentUacQidLinkUpdated.isActive()).isFalse();
    //
    //      Event databaseEvent = eventRepository.findAll().get(0);
    //      assertThat(databaseEvent.getType()).isEqualTo(EventType.DEACTIVATE_UAC);
  }
}
