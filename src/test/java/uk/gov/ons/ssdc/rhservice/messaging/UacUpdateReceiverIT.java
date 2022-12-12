package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
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
import uk.gov.ons.ssdc.rhservice.exceptions.UacNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.LaunchDataFieldDTO;
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

    @Autowired
    private PubsubTestHelper pubsubTestHelper;
    @Autowired
    private FireStorePoller fireStorePoller;
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private UacRepository uacRepository;
    @Autowired
    private CollectionExerciseRepository collectionExerciseRepository;

    @Test
    void testUacUpdateReceivedWithNoELaunchDataSettings() throws UacNotFoundException {
        // GIVEN
        CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO(
                (UUID.randomUUID().toString()
                List.of(new CollectionInstrumentSelectionRule("testUrl1", null))
        );
        collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);

        CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO(
                UUID.randomUUID().toString(),
                collectionExerciseUpdateDTO.getCollectionExerciseId(),
                Map.of("PARTICIPANT_ID", "1111", "FIRST_NAME", "Hugh");
        caseRepository.writeCaseUpdate(caseUpdateDTO);

        UacUpdateDTO uacUpdateDTO = new UacUpdateDTO(
                caseUpdateDTO.getCaseId(),
                collectionExerciseUpdateDTO.getCollectionExerciseId(),
                "000000000001",
                String.valueOf(Math.random()),
                .active(true)
                .collectionInstrumentUrl("testUrl1")
                .build();

        EventDTO eventDTO = EventDTO.builder()
                .payload(PayloadDTO.builder().uacUpdate(uacUpdateDTO).build())
                .build();

         WHEN
        pubsubTestHelper.sendMessageToSharedProject(uacUpdateTopic, eventDTO);

        // THEN
        Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash());
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
                                                new LaunchDataFieldDTO("PARTICIPANT_ID", "participant_id", true),
                                                new LaunchDataFieldDTO("FIRST_NAME", "first_name", true))),
                                new CollectionInstrumentSelectionRule("differentUrl1", null)));
        collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);

        CaseUpdateDTO caseUpdateDTO =
                new CaseUpdateDTO(
                        UUID.randomUUID().toString(),
                        collectionExerciseUpdateDTO.getCollectionExerciseId(),
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
        pubsubTestHelper.sendMessageToSharedProject(uacUpdateTopic, event);

        // THEN
        Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash());
        Assertions.assertTrue(uacOpt.isPresent());

        UacUpdateDTO actualUacUpdateDTO = uacOpt.get();

        // add our expected fields on here
        Map<String, String> expectedLaunchData = new HashMap<>();
        expectedLaunchData.put("participant_id", "1111");
        expectedLaunchData.put("first_name", "Hugh");
        uacUpdateDTO.setLaunchData(expectedLaunchData);

        assertThat(actualUacUpdateDTO).isEqualTo(uacUpdateDTO);
    }
}
