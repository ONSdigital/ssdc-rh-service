package uk.gov.ons.ssdc.rhservice.endpoints;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.ons.ssdc.rhservice.exceptions.CaseNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;
import uk.gov.ons.ssdc.rhservice.testutils.FireStorePoller;
import uk.gov.ons.ssdc.rhservice.testutils.PubsubHelper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class EqLaunchEndpointIT {
    public static final String UAC_HASH = "UAC_HASH";
    public static final String TEST_QID = "TEST_QID";
    public static final String CASE_ID = UUID.randomUUID().toString();

    @LocalServerPort
    private int port;

    @Autowired
    private PubsubHelper pubsubHelper;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private UacRepository uacRepository;

    @Test
    void testEqLaunchUrlSuccessfullyReturned() throws UnirestException {

        CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
        caseUpdateDTO.setCaseId(CASE_ID);
        caseRepository.writeCaseUpdate(caseUpdateDTO);

        UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
        uacUpdateDTO.setUacHash(UAC_HASH);
        uacUpdateDTO.setQid(TEST_QID);
        uacUpdateDTO.setCaseId(CASE_ID);
        uacRepository.writeUAC(uacUpdateDTO);

        HttpResponse<String> response = Unirest.get(createUrl("http://localhost:%d/eqLaunchs/%s", port, UAC_HASH))
                .header("accept", "application/json")
                .queryString("languageCode", "en")
                .queryString("accountServiceUrl", "http://xyz.com")
                .queryString("accountServiceLogoutUrl", "http://logggedOut.com")
                .queryString("clientIP", "XXX.XXX.XXX.XXX")
                .asString();

        //TODO: Check if the authenicated message sent ot PubSub
    }

    private String createUrl(String urlFormat, int port, String param1) {
        return String.format(urlFormat, port, param1);
    }
}
