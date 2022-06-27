package uk.gov.ons.ssdc.rhservice.endpoints;

import com.nimbusds.jose.JWSObject;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.EqPayloadBuilder;
import uk.gov.ons.ssdc.rhservice.service.UacService;

@RestController
@RequestMapping(value = "/eqLaunch", produces = "application/json")
public class EqLaunchEndpoint {
  private final UacService uacService;
  private final EqPayloadBuilder eqPayloadBuilder;
  private final EncodeJws encodeJws;
  private final EncryptJwe encryptJwe;
  private final EqLaunchSender eqLaunchSender;

  public EqLaunchEndpoint(
      UacService uacService,
      EqPayloadBuilder eqPayloadBuilder,
      EncodeJws encodeJws,
      EncryptJwe encryptJwe,
      EqLaunchSender eqLaunchSender) {
    this.uacService = uacService;
    this.eqPayloadBuilder = eqPayloadBuilder;
    this.encodeJws = encodeJws;
    this.encryptJwe = encryptJwe;
    this.eqLaunchSender = eqLaunchSender;
  }

  @GetMapping(value = "/{uacHash}")
  public ResponseEntity<String> generateEqLaunchToken(
      @PathVariable("uacHash") final String uacHash,
      @RequestParam String languageCode,
      @RequestParam String accountServiceUrl,
      @RequestParam String accountServiceLogoutUrl) {

    Optional<UacUpdateDTO> uacOpt = uacService.getUac(uacHash);

    if (uacOpt.isEmpty()) {
      return new ResponseEntity<>("UAC Not Found", HttpStatus.NOT_FOUND);
    }

    CaseUpdateDTO caseUpdateDTO = uacService.getCaseFromUac(uacOpt.get());

    Map<String, Object> payload =
        eqPayloadBuilder.buildEqPayloadMap(
            accountServiceUrl, accountServiceLogoutUrl, languageCode, uacOpt.get(), caseUpdateDTO);

    String launchToken = encrypt(payload);

    // TODO: outside scope now, but...
    // If this fails (it's retryable) then it will throw an Exception
    // It's unlikely, but do we want to do that? Stopping a launch
    // We could go down MessageSender route, but that's more complex and can in theory still fail?
    eqLaunchSender.buildAndSendEqLaunchEvent(payload);

    return ResponseEntity.ok(launchToken);
  }

  private String encrypt(Map<String, Object> payload) {
    JWSObject jws = encodeJws.encode(payload);
    return encryptJwe.encrypt(jws);
  }
}
