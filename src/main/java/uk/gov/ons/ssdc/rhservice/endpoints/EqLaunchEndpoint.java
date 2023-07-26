package uk.gov.ons.ssdc.rhservice.endpoints;

import com.nimbusds.jose.JWSObject;
import io.micrometer.core.annotation.Timed;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.rhservice.crypto.EncodeJws;
import uk.gov.ons.ssdc.rhservice.crypto.EncryptJwe;
import uk.gov.ons.ssdc.rhservice.messaging.EqLaunchSender;
import uk.gov.ons.ssdc.rhservice.model.dto.UacOr4xxResponseEntity;
import uk.gov.ons.ssdc.rhservice.service.EqPayloadBuilder;
import uk.gov.ons.ssdc.rhservice.service.UacValidationService;

@RestController
@Timed
@RequestMapping(value = "/eqLaunch", produces = "application/json")
public class EqLaunchEndpoint {
  private final UacValidationService uacValidationService;
  private final EqPayloadBuilder eqPayloadBuilder;
  private final EncodeJws encodeJws;
  private final EncryptJwe encryptJwe;
  private final EqLaunchSender eqLaunchSender;

  public EqLaunchEndpoint(
      UacValidationService uacValidationService,
      EqPayloadBuilder eqPayloadBuilder,
      EncodeJws encodeJws,
      EncryptJwe encryptJwe,
      EqLaunchSender eqLaunchSender) {
    this.uacValidationService = uacValidationService;
    this.eqPayloadBuilder = eqPayloadBuilder;
    this.encodeJws = encodeJws;
    this.encryptJwe = encryptJwe;
    this.eqLaunchSender = eqLaunchSender;
  }

  @GetMapping(value = "/{uacHash}")
  public ResponseEntity<?> generateEqLaunchToken(
      @PathVariable("uacHash") final String uacHash,
      @RequestParam String languageCode,
      @RequestParam String accountServiceUrl) {

    UacOr4xxResponseEntity uacOr4xxResponseEntity = uacValidationService.getUac(uacHash);

    if (uacOr4xxResponseEntity.getResponseEntityOptional().isPresent()) {
      return uacOr4xxResponseEntity.getResponseEntityOptional().get();
    }

    Map<String, Object> payload =
        eqPayloadBuilder.buildEqPayloadMap(
            accountServiceUrl,
            languageCode,
            uacOr4xxResponseEntity.getUacUpdateDTO(),
            uacOr4xxResponseEntity.getCaseUpdateDTO(),
            uacOr4xxResponseEntity.getCollectionExerciseUpdateDTO());

    String launchToken = encrypt(payload);

    // TODO: outside scope now, but...
    // If this fails (it's retryable) then it will throw an Exception
    // It's unlikely, but do we want to do that? Stopping a launch
    // We could go down MessageSender route, but that's more complex and can in theory still fail?
    eqLaunchSender.buildAndSendEqLaunchEvent(
        payload, uacOr4xxResponseEntity.getUacUpdateDTO().getQid());

    return new ResponseEntity<>(launchToken, HttpStatus.OK);
  }

  private String encrypt(Map<String, Object> payload) {
    JWSObject jws = encodeJws.encode(payload);
    return encryptJwe.encrypt(jws);
  }
}
