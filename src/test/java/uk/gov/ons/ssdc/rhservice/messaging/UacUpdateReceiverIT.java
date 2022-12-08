package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
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
  void testUacUpdateReceived() throws UacNotFoundException {

    // GIVEN
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(UUID.randomUUID().toString());
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    uacUpdateDTO.setQid("000000000001");
    uacUpdateDTO.setUacHash(String.valueOf(Math.random()));

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUacUpdate(uacUpdateDTO);

    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToSharedProject(uacUpdateTopic, event);

    // THEN
    Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash());
    Assertions.assertTrue(uacOpt.isPresent());
    assertThat(uacOpt.get()).isEqualTo(uacUpdateDTO);
  }

  @Test
  void testUacUpdateReceivedPHMFields() throws UacNotFoundException {
    // GIVEN
    //
    //        LaunchDataFieldDTO launchDataFieldDTO = new LaunchDataFieldDTO();
    //        launchDataFieldDTO.setSampleField("PARTICIPANT_ID");
    //        launchDataFieldDTO.setLaunchDataFieldName("participant_id");
    //        Map<String, Object> eqLaunchSettings = new HashMap<>();
    //
    //
    //        eqLaunchSettings.put("eqLaunchDataSettings", launchDataFieldDTO);

    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();
    //        collectionExerciseUpdateDTO.setCollectionInstrumentRules(eqLaunchSettings);

    collectionExerciseUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(UUID.randomUUID().toString());

    caseUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());

    Map<String, String> sampleData = new HashMap<>();
    sampleData.put("PARTICIPANT_ID", "1111");
    sampleData.put("FIRST_NAME", "Hugh");
    caseUpdateDTO.setSample(sampleData);

    caseRepository.writeCaseUpdate(caseUpdateDTO);

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(caseUpdateDTO.getCaseId());
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
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

  //    @Test
  //    public void newCopyOfActiveUAacDoesNotOverwriteLaunchData() throws UacNotFoundException {
  //      // GIVEN
  //      CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
  //      caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
  //
  //      Map<String, String> sampleLaunchData = new HashMap<>();
  //      sampleLaunchData.put("SWAB_BARCODE", "OriginalBarCode");
  //      sampleLaunchData.put("BLOOD_BARCODE", "98765");
  //      sampleLaunchData.put("PARTICIPANT_ID", "1111");
  //      sampleLaunchData.put("LONGITUDINAL_QUESTIONS", "AYE");
  //      sampleLaunchData.put("FIRST_NAME", "BOB");
  //      caseUpdateDTO.setSample(sampleLaunchData);
  //
  //      caseRepository.writeCaseUpdate(caseUpdateDTO);
  //
  //      UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
  //      uacUpdateDTO.setCaseId(caseUpdateDTO.getCaseId());
  //      uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
  //      uacUpdateDTO.setQid("000000000001");
  //      uacUpdateDTO.setUacHash(String.valueOf(Math.random()));
  //      uacUpdateDTO.setActive(true);
  //      uacUpdateDTO.setLaunchData(sampleLaunchData);
  //
  //      uacRepository.writeUAC(uacUpdateDTO);
  //
  //
  //
  //      // Now fire in a case update with a new Barcode, then a 'double delivered UAC' -
  // technically
  //      // possible, very unlikely
  //      sampleLaunchData.put("SWAB_BARCODE", "UpdatedLaunchBarCode");
  //      caseUpdateDTO.setSample(sampleLaunchData);
  //      caseRepository.writeCaseUpdate(caseUpdateDTO);
  //
  //      PayloadDTO payloadDTO = new PayloadDTO();
  //      payloadDTO.setUacUpdate(uacUpdateDTO);
  //      EventDTO eventDTO = new EventDTO();
  //      eventDTO.setPayload(payloadDTO);
  //
  //      uacUpdateDTO.setQid("SomethingToUpdateToCheckItsUpdated");
  //
  //      // Now send in the copy of UACUpdateDTO
  //      pubsubTestHelper.sendMessageToSharedProject(uacUpdateTopic, eventDTO);
  //
  //      // without this method of changing and checking the QUD we'll often get the old already
  //
  //      // UAC Udpdate
  //      Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUACByHashAndQID(
  //              uacUpdateDTO.getUacHash(), "SomethingToUpdateToCheckItsUpdated");
  //      Assertions.assertTrue(uacOpt.isPresent());
  //
  //      assertThat(uacOpt.get().getLaunchData().get("SWAB_BARCODE")).isEqualTo("OriginalBarCode");
  //    }
  //
  //    @Test
  //    void testUacUpdateInactiveBlanksLaunchData() throws UacNotFoundException {
  //      // GIVEN
  //      CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
  //      caseUpdateDTO.setCaseId(UUID.randomUUID().toString());
  //
  //      Map<String, String> sampleLaunchData = new HashMap<>();
  //      sampleLaunchData.put("SWAB_BARCODE", "01234");
  //      sampleLaunchData.put("BLOOD_BARCODE", "98765");
  //      sampleLaunchData.put("PARTICIPANT_ID", "1111");
  //      sampleLaunchData.put("LONGITUDINAL_QUESTIONS", "AYE");
  //      sampleLaunchData.put("FIRST_NAME", "BOB");
  //      caseUpdateDTO.setSample(sampleLaunchData);
  //
  //      caseRepository.writeCaseUpdate(caseUpdateDTO);
  //
  //      UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
  //      uacUpdateDTO.setCaseId(caseUpdateDTO.getCaseId());
  //      uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
  //      uacUpdateDTO.setQid("000000000001");
  //      uacUpdateDTO.setUacHash(String.valueOf(Math.random()));
  //      uacUpdateDTO.setActive(true);
  //
  //      PayloadDTO payloadDTO = new PayloadDTO();
  //      payloadDTO.setUacUpdate(uacUpdateDTO);
  //
  //      EventDTO event = new EventDTO();
  //      event.setPayload(payloadDTO);
  //
  //      pubsubTestHelper.sendMessageToSharedProject(uacUpdateTopic, event);
  //
  //      Optional<UacUpdateDTO> uacOpt = fireStorePoller.getUacByHash(uacUpdateDTO.getUacHash());
  //      Assertions.assertTrue(uacOpt.isPresent());
  //
  //      UacUpdateDTO actualUacUpdateDTO = uacOpt.get();
  //
  //      assertThat(actualUacUpdateDTO.getLaunchData()).isEqualTo(sampleLaunchData);
  //
  //      // WHEN - now make the UAC inactive with no LaunchData
  //      uacUpdateDTO.setActive(false);
  //      uacUpdateDTO.setLaunchData(null);
  //      payloadDTO.setUacUpdate(uacUpdateDTO);
  //      event.setPayload(payloadDTO);
  //      pubsubTestHelper.sendMessageToSharedProject(uacUpdateTopic, event);
  //
  //      uacOpt = fireStorePoller.getUacByHashUacActiveValue(uacUpdateDTO.getUacHash(), false);
  //      Assertions.assertTrue(uacOpt.isPresent());
  //
  //      actualUacUpdateDTO = uacOpt.get();
  //      assertThat(actualUacUpdateDTO.isActive()).isFalse();
  //      assertThat(actualUacUpdateDTO.getLaunchData()).isNull();
  //    }
}
