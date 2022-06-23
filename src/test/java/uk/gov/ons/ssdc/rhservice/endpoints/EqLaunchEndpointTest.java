package uk.gov.ons.ssdc.rhservice.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JWSObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.ssdc.rhservice.crypto.EncodeJws;
import uk.gov.ons.ssdc.rhservice.crypto.EncryptJwe;
import uk.gov.ons.ssdc.rhservice.messaging.AuthenicatedMessageSender;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.EqPayloadBuilder;
import uk.gov.ons.ssdc.rhservice.service.UacService;

@ExtendWith(MockitoExtension.class)
public class EqLaunchEndpointTest {

  @Mock private UacService uacService;

  @Mock private EqPayloadBuilder eqPayloadBuilder;

  @Mock private EncodeJws encodeJws;

  @Mock private EncryptJwe encryptJwe;

  @Mock private AuthenicatedMessageSender authenicatedMessageSender;

  @InjectMocks private EqLaunchEndpoint underTest;

  @Test
  public void testCallingEndpointGetsToken() {
    // Given
    Map<String, Object> payload = new HashMap<>();
    JWSObject jwsObject = Mockito.mock(JWSObject.class);
    String expectedToken = "cunninglyEncryptedToken";
    String uacHash = "UAC_HASH";
    String languageCode = "LANGUAGE_CODE";
    String accountServiceUrl = "ACCOUNT_SERVICE_URL";
    String accountServiceLogoutUrl = "ACCOUNT_SERVICE_LOGOUT_URL";
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();

    when(eqPayloadBuilder.buildEqPayloadMap(any(), any(), any(), any(), any())).thenReturn(payload);
    when(encodeJws.encode(any())).thenReturn(jwsObject);
    when(encryptJwe.encrypt(any())).thenReturn(expectedToken);
    when(uacService.getUac(any())).thenReturn(Optional.of(uacUpdateDTO));
    when(uacService.getCaseFromUac(any())).thenReturn(caseUpdateDTO);

    // when
    ResponseEntity<String> response =
        underTest.generateEqLaunchToken(
            uacHash, languageCode, accountServiceUrl, accountServiceLogoutUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(expectedToken);
    verify(uacService).getUac(uacHash);
    verify(uacService).getCaseFromUac(uacUpdateDTO);
    verify(eqPayloadBuilder)
        .buildEqPayloadMap(
            accountServiceUrl, accountServiceLogoutUrl, languageCode, uacUpdateDTO, caseUpdateDTO);
    verify(encodeJws).encode(payload);
    verify(encryptJwe).encrypt(jwsObject);
    verify(authenicatedMessageSender).buildAndSendUacAuthentication(any());
  }
}
