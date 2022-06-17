package uk.gov.ons.ssdc.rhservice.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.rhservice.crypto.JweDecryptor;
import uk.gov.ons.ssdc.rhservice.crypto.KeyStore;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
class EqLaunchEndpointIT {
  public static final String UAC_HASH = "UAC_HASH";
  public static final String QID = "QID";
  public static final String CASE_ID = UUID.randomUUID().toString();

  @LocalServerPort private int port;

  @Autowired private PubsubHelper pubsubHelper;

  @Autowired private CaseRepository caseRepository;

  @Autowired private UacRepository uacRepository;

  @Autowired private KeyStore keyStore;

  @Test
  void testEqLaunchUrlSuccessfullyReturned() throws UnirestException {

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId(CASE_ID);
    caseUpdateDTO.setCollectionExerciseId("COLLEX_ID");
    caseRepository.writeCaseUpdate(caseUpdateDTO);

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setUacHash(UAC_HASH);
    uacUpdateDTO.setQid(QID);
    uacUpdateDTO.setCaseId(CASE_ID);
    uacRepository.writeUAC(uacUpdateDTO);

    HttpResponse<String> response =
        Unirest.get(createUrl("http://localhost:%d/eqLaunch/%s", port, UAC_HASH))
            .header("accept", "application/json")
            .queryString("languageCode", "en")
            .queryString("accountServiceUrl", "http://xyz.com")
            .queryString("accountServiceLogoutUrl", "http://logggedOut.com")
            .queryString("clientIP", "XXX.XXX.XXX.XXX")
            .asString();

    assertThat(response.getStatus()).isEqualTo(OK.value());
    assertThat(response.getBody())
        .startsWith(
            "eyJraWQiOiI3NWRjMmNlYjZhMDIyNDZiMjkwOWY2YjdmNzcxNmU0MDkzMjE1NDlkIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6IlJTQS1PQUVQIn0");

    // TODO: Can we decrypt for 'nicer' test?
//    assertThat(decryptToken(response.getBody())).isEqualTo("blah");

    // TODO: Check if the authenicated message sent ot PubSub
  }

  private String createUrl(String urlFormat, int port, String param1) {
    return String.format(urlFormat, port, param1);
  }

  private String decryptToken(String token) {
    JweDecryptor jweDecryptor = new JweDecryptor(keyStore);
    return jweDecryptor.decrypt(token);
  }
}
