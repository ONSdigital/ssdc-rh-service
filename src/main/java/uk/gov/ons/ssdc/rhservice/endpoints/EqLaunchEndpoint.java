package uk.gov.ons.ssdc.rhservice.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.rhservice.service.EqLaunchService;
import uk.gov.ons.ssdc.rhservice.service.UacService;

@RestController
@RequestMapping(value = "/eqLaunch", produces = "application/json")
public class EqLaunchEndpoint {

  private final UacService uacService;
  private final EqLaunchService eqLaunchService;

  public EqLaunchEndpoint(UacService uacService, EqLaunchService eqLaunchService) {
    this.uacService = uacService;
    this.eqLaunchService = eqLaunchService;
  }

  @GetMapping(value = "/{uacHash}")
  public ResponseEntity<String> generateEqLaunchToken(
      @PathVariable("uacHash") final String uacHash,
      @RequestParam String languageCode,
      @RequestParam String accountServiceUrl,
      @RequestParam String accountServiceLogoutUrl,
      @RequestParam String clientIP) {

    uacService.validateUacHash(uacHash);

    String launchToken =
        eqLaunchService.generateEqLaunchToken(
            uacHash, accountServiceUrl, accountServiceLogoutUrl, clientIP, languageCode);
    return ResponseEntity.ok(launchToken);
  }
}
