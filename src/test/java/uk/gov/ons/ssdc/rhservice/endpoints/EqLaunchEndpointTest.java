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
import uk.gov.ons.ssdc.rhservice.messaging.EqLaunchSender;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacOr4xxResponseEntity;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.EqPayloadBuilder;
import uk.gov.ons.ssdc.rhservice.service.UacValidationService;

@ExtendWith(MockitoExtension.class)
class EqLaunchEndpointTest {

  @Mock private UacValidationService uacValidationService;

  @Mock private EqPayloadBuilder eqPayloadBuilder;

  @Mock private EncodeJws encodeJws;

  @Mock private EncryptJwe encryptJwe;

  @Mock private EqLaunchSender eqLaunchSender;

  @InjectMocks private EqLaunchEndpoint underTest;

  @Test
  void testCallingEndpointGetsToken() {
    // Given
    Map<String, Object> eqPayload = new HashMap<>();
    JWSObject jwsObject = Mockito.mock(JWSObject.class);
    String expectedToken = "cunninglyEncryptedToken";
    String uacHash = "UAC_HASH";
    String languageCode = "LANGUAGE_CODE";
    String accountServiceUrl = "ACCOUNT_SERVICE_URL";

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();

    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();

    UacOr4xxResponseEntity uacOr4xxResponseEntity = new UacOr4xxResponseEntity();
    uacOr4xxResponseEntity.setUacUpdateDTO(uacUpdateDTO);
    uacOr4xxResponseEntity.setCaseUpdateDTO(caseUpdateDTO);
    uacOr4xxResponseEntity.setCollectionExerciseUpdateDTO(collectionExerciseUpdateDTO);
    uacOr4xxResponseEntity.setResponseEntityOptional(Optional.empty());

    when(eqPayloadBuilder.buildEqPayloadMap(any(), any(), any(), any(), any()))
        .thenReturn(eqPayload);
    when(encodeJws.encode(any())).thenReturn(jwsObject);
    when(encryptJwe.encrypt(any())).thenReturn(expectedToken);
    when(uacValidationService.getUac(any())).thenReturn(uacOr4xxResponseEntity);

    // when
    ResponseEntity<?> response =
        underTest.generateEqLaunchToken(uacHash, languageCode, accountServiceUrl);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(expectedToken);
    verify(uacValidationService).getUac(uacHash);
    verify(eqPayloadBuilder)
        .buildEqPayloadMap(
            accountServiceUrl,
            languageCode,
            uacUpdateDTO,
            caseUpdateDTO,
            collectionExerciseUpdateDTO);
    verify(encodeJws).encode(eqPayload);
    verify(encryptJwe).encrypt(jwsObject);
    verify(eqLaunchSender).buildAndSendEqLaunchEvent(any(), any());
  }

  @Test
  void testCallingEndpointGetsResponseEntityReturned() {
    // Given
    Map<String, Object> payload = new HashMap<>();
    JWSObject jwsObject = Mockito.mock(JWSObject.class);
    String expectedToken = "cunninglyEncryptedToken";
    String uacHash = "UAC_HASH";
    String languageCode = "LANGUAGE_CODE";
    String accountServiceUrl = "ACCOUNT_SERVICE_URL";
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(true);
    uacUpdateDTO.setActive(false);
    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    UacOr4xxResponseEntity uacOr4xxResponseEntity = new UacOr4xxResponseEntity();
    uacOr4xxResponseEntity.setUacUpdateDTO(uacUpdateDTO);
    uacOr4xxResponseEntity.setCaseUpdateDTO(caseUpdateDTO);
    uacOr4xxResponseEntity.setResponseEntityOptional(
        Optional.of(new ResponseEntity<>("UAC_RECEIPTED", HttpStatus.BAD_REQUEST)));

    when(uacValidationService.getUac(any())).thenReturn(uacOr4xxResponseEntity);

    // when
    ResponseEntity<?> response =
        underTest.generateEqLaunchToken(uacHash, languageCode, accountServiceUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("UAC_RECEIPTED");
    verify(uacValidationService).getUac(uacHash);
  }
}
