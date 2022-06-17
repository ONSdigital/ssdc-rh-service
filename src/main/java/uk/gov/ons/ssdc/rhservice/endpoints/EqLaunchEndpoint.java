package uk.gov.ons.ssdc.rhservice.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.rhservice.model.EqLaunchRequestDTO;
import uk.gov.ons.ssdc.rhservice.service.EqLaunchService;
import uk.gov.ons.ssdc.rhservice.service.UacService;
import uk.gov.ons.ssdc.rhservice.utils.Language;

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
      @RequestParam(required = true) String languageCode,
      @RequestParam(required = true) String accountServiceUrl,
      @RequestParam(required = true) String accountServiceLogoutUrl,
      @RequestParam(required = true) String clientIP) {

    uacService.validateUacHash(uacHash);

    Language language = validateAndGetLanguage(languageCode);

    EqLaunchRequestDTO eqLaunchedDTO =
        buildLaunchRRequestDTO(accountServiceUrl, accountServiceLogoutUrl, clientIP, language);

    String launchToken = eqLaunchService.generateEqLaunchToken(uacHash, eqLaunchedDTO);
    return ResponseEntity.ok(launchToken);
  }

  private EqLaunchRequestDTO buildLaunchRRequestDTO(
      String accountServiceUrl,
      String accountServiceLogoutUrl,
      String clientIP,
      Language language) {
    EqLaunchRequestDTO eqLaunchedDTO = new EqLaunchRequestDTO();
    eqLaunchedDTO.setLanguageCode(language);
    eqLaunchedDTO.setAccountServiceUrl(accountServiceUrl);
    eqLaunchedDTO.setAccountServiceLogoutUrl(accountServiceLogoutUrl);
    eqLaunchedDTO.setClientIP(clientIP);
    return eqLaunchedDTO;
  }

  private Language validateAndGetLanguage(String languageCode) {
    Language language = Language.lookup(languageCode);
    if (language == null) {
      throw new RuntimeException("Invalid language code: '" + languageCode + "'");
    }
    return language;
  }
}
