package uk.gov.ons.ssdc.rhservice.service;

import com.nimbusds.jose.JWSObject;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.crypto.EncodeJws;
import uk.gov.ons.ssdc.rhservice.crypto.EncryptJwe;

@Service
public class EqLaunchService {
  private final EqPayloadBuilder eqPayloadBuilder;
  private final EncodeJws encodeJws;
  private final EncryptJwe encryptJwe;

  public EqLaunchService(
      EqPayloadBuilder eqPayloadBuilder, EncodeJws encodeJws, EncryptJwe encryptJwe) {
    this.eqPayloadBuilder = eqPayloadBuilder;

    this.encodeJws = encodeJws;
    this.encryptJwe = encryptJwe;
  }

  public String generateEqLaunchToken(
      String uacHash,
      String accountServiceUrl,
      String accountServiceLogoutUrl,
      String clientIP,
      String languageCode) {
    Map<String, Object> payload =
        eqPayloadBuilder.buildEqPayloadMap(
            uacHash, accountServiceUrl, accountServiceLogoutUrl, languageCode);

    // eventPublisher.sendEvent(TopicType.EQ_LAUNCH, Source.RESPONDENT_HOME, Channel.RH, eqLaunch);
    return encrypt(payload);
  }

  // TODO, make this all work, not urgent yet - but needs completing for ticket.
  //    private void sendUacAuthenticationEvent(String caseId, String qid) {
  //
  //        log.info(
  //                "Generating UacAuthentication event for caseId",
  //                kv("caseId", caseId),
  //                kv("questionnaireId", qid));
  //
  //        UacAuthentication uacAuthentication = UacAuthentication.builder().qid(qid).build();
  //
  //        UUID messageId =
  //                eventPublisher.sendEvent(
  //                        TopicType.UAC_AUTHENTICATION, Source.RESPONDENT_HOME, Channel.RH,
  // uacAuthentication);
  //
  //        log.debug(
  //                "UacAuthentication event published for qid: "
  //                        + uacAuthentication.getQid()
  //                        + ", messageId: "
  //                        + messageId);
  //    }

  private String encrypt(Map<String, Object> payload) {
    JWSObject jws = encodeJws.encode(payload);
    return encryptJwe.encrypt(jws);
  }
}
