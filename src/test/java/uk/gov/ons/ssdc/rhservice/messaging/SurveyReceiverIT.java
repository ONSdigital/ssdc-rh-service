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
import uk.gov.ons.ssdc.rhservice.exceptions.SurveyNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.SurveyDto;
import uk.gov.ons.ssdc.rhservice.testutils.FireStorePoller;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubTestHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class SurveyReceiverIT {

  @Value("${queueconfig.survey-update-topic}")
  private String surveyTopic;

  @Autowired private PubsubTestHelper pubsubTestHelper;

  @Autowired private FireStorePoller fireStorePoller;

  @Test
  void testSurveyReceived() throws CollectionExerciseNotFoundException, SurveyNotFoundException {
    // GIVEN
    // Just trying out Builder pattern as a change to see what team thinks.
    // Pros: more concise, and in someways more clear: it shows the hierarchy cleanly.
    // Cons: different to normal, harder to access single object later - probably bad form to do
    // this? even though I do it all the time
    // Note: you can format differently - more compressed with builds or/and field setting on 1
    // line.  Looks clearer like this
    //
    //        EventDTO event = EventDTO.builder()
    //                .payload(PayloadDTO.builder()
    //                        .surveyDto(SurveyDto.builder()
    //                                .id(UUID.randomUUID())
    //                                .name("TestSurvey")
    //                                .metadata("{\"launchFields:\" { \"field1\": \"potatoes\"}}")
    //                                .build()).
    //                        build()).
    //                build();

    //        normal way
    SurveyDto surveyDto = new SurveyDto();
    surveyDto.setId(UUID.randomUUID().toString());
    surveyDto.setName("Name");
    surveyDto.setMetadata(null);
    //        surveyDto.setMetadata(null);
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setSurveyDto(surveyDto);
    EventDTO event = new EventDTO();
    event.setPayload(payloadDTO);

    // WHEN
    pubsubTestHelper.sendMessageToSharedProject(surveyTopic, event);

    // THEN
    Optional<SurveyDto> surveyOpt = fireStorePoller.getSurveyById(surveyDto.getId().toString());

    Assertions.assertTrue(surveyOpt.isPresent());
    assertThat(surveyOpt.get()).isEqualTo(event.getPayload().getSurveyDto());
  }
}
