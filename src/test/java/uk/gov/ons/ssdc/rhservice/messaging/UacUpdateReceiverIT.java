package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.exceptions.UacNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchSettings;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;
import uk.gov.ons.ssdc.rhservice.testutils.FireStorePoller;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubTestHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class UacUpdateReceiverIT {
  @Value("${queueconfig.uac-update-topic}")
  private String uacUpdateTopic;

  @Autowired private PubsubTestHelper pubsubTestHelper;
  @Autowired private FireStorePoller fireStorePoller;
  @Autowired private CaseRepository caseRepository;
  @Autowired private UacRepository uacRepository;
  @Autowired private CollectionExerciseRepository collectionExerciseRepository;

  @Test
  void testUacUpdateReceivedWithNoELaunchDataSettings() throws UacNotFoundException {
    // GIVEN
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(new CollectionInstrumentSelectionRule("testUrl1", null)),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);

    CaseUpdateDTO caseUpdateDTO =
        new CaseUpdateDTO(
            UUID.randomUUID().toString(),
            collectionExerciseUpdateDTO.getCollectionExerciseId(),
            false,
            "",
            Map.of("PARTICIPANT_ID", "1111", "FIRST_NAME", "Hugh"));
    caseRepository.writeCaseUpdate(caseUpdateDTO);

    // The object we actually care about
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(caseUpdateDTO.getCaseId());
    uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
    uacUpdateDTO.setCollectionInstrumentUrl("testUrl1");
    uacUpdateDTO.setQid("000000000001");
    uacUpdateDTO.setUacHash(String.valueOf(Math.random()));
    uacUpdateDTO.setActive(true);

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUacUpdate(uacUpdateDTO);

    EventDTO eventDTO = new EventDTO(null, payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToPubsubProject(uacUpdateTopic, eventDTO);

    // THEN
    Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash(), true);
    Assertions.assertTrue(uacOpt.isPresent());
    assertThat(uacOpt.get()).isEqualTo(uacUpdateDTO);
  }

  @Test
  void testUacUpdateReceivedWithEqLaunchSettingsCollex() throws UacNotFoundException {
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(
                new CollectionInstrumentSelectionRule(
                    "testUrl1",
                    List.of(
                        new EqLaunchSettings("PARTICIPANT_ID", "participant_id", true),
                        new EqLaunchSettings("FIRST_NAME", "first_name", true))),
                new CollectionInstrumentSelectionRule("differentUrl1", null)),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);

    CaseUpdateDTO caseUpdateDTO =
        new CaseUpdateDTO(
            UUID.randomUUID().toString(),
            collectionExerciseUpdateDTO.getCollectionExerciseId(),
            false,
            "",
            Map.of("PARTICIPANT_ID", "1111", "FIRST_NAME", "Hugh"));
    caseRepository.writeCaseUpdate(caseUpdateDTO);

    // The object we actually care about
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(caseUpdateDTO.getCaseId());
    uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
    uacUpdateDTO.setCollectionInstrumentUrl("testUrl1");
    uacUpdateDTO.setQid("000000000001");
    uacUpdateDTO.setUacHash(String.valueOf(Math.random()));
    uacUpdateDTO.setActive(true);

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUacUpdate(uacUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToPubsubProject(uacUpdateTopic, event);

    // THEN
    Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash(), true);
    Assertions.assertTrue(uacOpt.isPresent());

    UacUpdateDTO actualUacUpdateDTO = uacOpt.get();

    // add our expected fields on here
    Map<String, String> expectedLaunchData = new HashMap<>();
    expectedLaunchData.put("participant_id", "1111");
    expectedLaunchData.put("first_name", "Hugh");
    uacUpdateDTO.setLaunchData(expectedLaunchData);

    assertThat(actualUacUpdateDTO).isEqualTo(uacUpdateDTO);
  }

  @Test
  void testInactiveUACBlanksLaunchSettings() throws UacNotFoundException {
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(
                new CollectionInstrumentSelectionRule(
                    "testUrl1",
                    List.of(
                        new EqLaunchSettings("PARTICIPANT_ID", "participant_id", true),
                        new EqLaunchSettings("FIRST_NAME", "first_name", true))),
                new CollectionInstrumentSelectionRule("differentUrl1", null)),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);

    CaseUpdateDTO caseUpdateDTO =
        new CaseUpdateDTO(
            UUID.randomUUID().toString(),
            collectionExerciseUpdateDTO.getCollectionExerciseId(),
            false,
            "",
            Map.of("PARTICIPANT_ID", "1111", "FIRST_NAME", "Hugh"));
    caseRepository.writeCaseUpdate(caseUpdateDTO);

    // The object we actually care about
    UacUpdateDTO oldUacDTO = new UacUpdateDTO();
    oldUacDTO.setCaseId(caseUpdateDTO.getCaseId());
    oldUacDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
    oldUacDTO.setCollectionInstrumentUrl("testUrl1");
    oldUacDTO.setQid("000000000001");
    oldUacDTO.setUacHash(String.valueOf(Math.random()));
    oldUacDTO.setActive(true);
    oldUacDTO.setLaunchData(Map.of("field", "value"));
    uacRepository.writeUAC(oldUacDTO);

    UacUpdateDTO deactivatedUAC = new UacUpdateDTO();
    deactivatedUAC.setCaseId(caseUpdateDTO.getCaseId());
    deactivatedUAC.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
    deactivatedUAC.setCollectionInstrumentUrl("testUrl1");
    deactivatedUAC.setQid("000000000001");
    deactivatedUAC.setUacHash(oldUacDTO.getUacHash());
    deactivatedUAC.setActive(false);

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUacUpdate(deactivatedUAC);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToPubsubProject(uacUpdateTopic, event);

    // THEN
    Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(oldUacDTO.getUacHash(), false);
    Assertions.assertTrue(uacOpt.isPresent());

    UacUpdateDTO actualUacUpdateDTO = uacOpt.get();

    assertThat(actualUacUpdateDTO).isEqualTo(deactivatedUAC);
  }
}
