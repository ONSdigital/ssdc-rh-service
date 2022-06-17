package uk.gov.ons.ssdc.rhservice.service;

import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.crypto.JwtEncryptor;
import uk.gov.ons.ssdc.rhservice.crypto.keys.KeyStore;
import uk.gov.ons.ssdc.rhservice.model.EqLaunchRequestDTO;

import java.util.Map;

@Service
public class EqLaunchService {
    private final EqPayloadBuilder eqPayloadBuilder;

    private final JwtEncryptor jwtEncryptor;


    public EqLaunchService(EqPayloadBuilder eqPayloadBuilder, KeyStore keyStore) {
        this.eqPayloadBuilder = eqPayloadBuilder;
        this.jwtEncryptor = new JwtEncryptor(keyStore, "authentication");
    }

    public String generateEqLaunchToken(String uacHash, EqLaunchRequestDTO eqLaunchedDTO) {
        Map<String, Object> payload = eqPayloadBuilder.buildEqPayloadMap(uacHash, eqLaunchedDTO);

        return jwtEncryptor.encrypt(payload);


        // TODO: IMPLEMENT this the SRM way
//    EqLaunch eqLaunch = new EqLaunch();
//    eqLaunch.setQid(launchData.getUacUpdateDTO().getQid());
        // eventPublisher.sendEvent(TopicType.EQ_LAUNCH, Source.RESPONDENT_HOME, Channel.RH, eqLaunch);
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


}
