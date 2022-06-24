package uk.gov.ons.ssdc.rhservice.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nimbusds.jose.JWSObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;
import uk.gov.ons.ssdc.rhservice.testutils.DecryptJwt;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubTestHelper;
import uk.gov.ons.ssdc.rhservice.testutils.QueueSpy;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
class EqLaunchEndpointIT {
  public static final String QID = "QID";
  public static final String CASE_ID = UUID.randomUUID().toString();
  public static final String COLLEX_ID = UUID.randomUUID().toString();
  public static final String OUTBOUND_EQ_LAUNCH_SUBSCRIPTION = "event_eq-launch";

  @LocalServerPort private int port;

  @Autowired private PubsubTestHelper pubsubTestHelper;

  @Autowired private CaseRepository caseRepository;

  @Autowired private UacRepository uacRepository;

  @Value("${jws_decode}")
  private String jws_decode_key;

  @Value("${jwe_decrypt}")
  private String jwe_decrypt_key;

  @Test
  void testEqLaunchUrlSuccessfullyReturned()
      throws UnirestException, JsonProcessingException, InterruptedException {
    String uacHash = RandomStringUtils.randomAlphabetic(10);

    try (QueueSpy<EventDTO> outboundCaseQueueSpy =
        pubsubTestHelper.sharedProjectListen(OUTBOUND_EQ_LAUNCH_SUBSCRIPTION, EventDTO.class)) {
      CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
      caseUpdateDTO.setCaseId(CASE_ID);
      caseUpdateDTO.setCollectionExerciseId(COLLEX_ID);
      caseRepository.writeCaseUpdate(caseUpdateDTO);

      UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
      uacUpdateDTO.setUacHash(uacHash);
      uacUpdateDTO.setQid(QID);
      uacUpdateDTO.setCaseId(CASE_ID);
      uacRepository.writeUAC(uacUpdateDTO);

      HttpResponse<String> response =
          Unirest.get(createUrl("http://localhost:%d/eqLaunch/%s", port, uacHash))
              .header("accept", "application/json")
              .queryString("languageCode", "en")
              .queryString("accountServiceUrl", "http://xyz.com")
              .queryString("accountServiceLogoutUrl", "http://logggedOut.com")
              .asString();

      assertThat(response.getStatus()).isEqualTo(OK.value());

      String decryptedToken = decryptToken(response.getBody());
      Map<String, String> tokenData = new ObjectMapper().readValue(decryptedToken, HashMap.class);

      assertThat(tokenData)
          .containsEntry("case_id", CASE_ID)
          .containsEntry("questionnaire_id", QID)
          .containsEntry("collection_exercise_sid", COLLEX_ID)
          .containsEntry("language_code", "en");

      EventDTO actualEvent = outboundCaseQueueSpy.checkExpectedMessageReceived();
      assertThat(actualEvent.getPayload().getEqLaunchDTO().getQid()).isEqualTo(QID);
    }
  }

  @Test
  public void testUacNotFound() throws UnirestException {
    String uacHash = RandomStringUtils.randomAlphabetic(10);

    HttpResponse<String> response =
        Unirest.get(createUrl("http://localhost:%d/eqLaunch/%s", port, uacHash))
            .header("accept", "application/json")
            .queryString("languageCode", "en")
            .queryString("accountServiceUrl", "http://xyz.com")
            .queryString("accountServiceLogoutUrl", "http://logggedOut.com")
            .asString();

    assertThat(response.getStatus()).isEqualTo(NOT_FOUND.value());
    assertThat(response.getBody()).endsWith("UAC Not Found");
  }

  @Test
  public void testCaseNotFoundThrows500() throws UnirestException {
    String uacHash = RandomStringUtils.randomAlphabetic(10);
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setUacHash(uacHash);
    uacUpdateDTO.setQid(QID);
    uacUpdateDTO.setCaseId(CASE_ID);
    uacRepository.writeUAC(uacUpdateDTO);

    HttpResponse<String> response =
        Unirest.get(createUrl("http://localhost:%d/eqLaunch/%s", port, uacHash))
            .header("accept", "application/json")
            .queryString("languageCode", "en")
            .queryString("accountServiceUrl", "http://xyz.com")
            .queryString("accountServiceLogoutUrl", "http://logggedOut.com")
            .asString();

    assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
  }

  private String createUrl(String urlFormat, int port, String param1) {
    return String.format(urlFormat, port, param1);
  }

  private String decryptToken(String token) {
    JWSObject jwsObject = DecryptJwt.decryptJwe(token, jwe_decrypt_key);
    return DecryptJwt.decodeJws(jwsObject, jws_decode_key);
  }
}
