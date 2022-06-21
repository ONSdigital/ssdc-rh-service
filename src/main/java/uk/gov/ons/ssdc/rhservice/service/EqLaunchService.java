package uk.gov.ons.ssdc.rhservice.service;

import com.nimbusds.jose.JWSObject;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.crypto.EncodeJws;
import uk.gov.ons.ssdc.rhservice.crypto.EncryptJwe;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventHeaderDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacAuthenticationDTO;
import uk.gov.ons.ssdc.rhservice.utils.PubsubHelper;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertObjectToJson;

@Service
public class EqLaunchService {
    public static final String OUTBOUND_EVENT_SCHEMA_VERSION = "0.5.0";

    @Value("${queueconfig.uac-authentication-topic}")
    private String uacAuthenticationTopic;

    private final EqPayloadBuilder eqPayloadBuilder;
    private final EncodeJws encodeJws;
    private final EncryptJwe encryptJwe;
    private final PubsubHelper pubsubHelper;

    public EqLaunchService(
            EqPayloadBuilder eqPayloadBuilder, EncodeJws encodeJws, EncryptJwe encryptJwe, PubsubHelper pubsubHelper) {
        this.eqPayloadBuilder = eqPayloadBuilder;

        this.encodeJws = encodeJws;
        this.encryptJwe = encryptJwe;
        this.pubsubHelper = pubsubHelper;
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

        String encryptedPayload = encrypt(payload);

        buildAndSendUacAuthentication(payload);

        return encryptedPayload;
    }

    private void buildAndSendUacAuthentication(Map<String, Object> payload) {
        EventDTO eqLaunchedEvent = new EventDTO();
        EventHeaderDTO eventHeader = new EventHeaderDTO();
        eventHeader.setVersion(OUTBOUND_EVENT_SCHEMA_VERSION);
        eventHeader.setTopic(uacAuthenticationTopic);
        eqLaunchedEvent.setHeader(eventHeader);

        UacAuthenticationDTO uacAuthentication = new UacAuthenticationDTO();
        uacAuthentication.setQid(payload.get("questionnaire_id").toString());
        PayloadDTO payloadDTO = new PayloadDTO();
        payloadDTO.setUacAuthenticationDTO(uacAuthentication);
        eqLaunchedEvent.setPayload(payloadDTO);

        String messageJson = convertObjectToJson(eqLaunchedEvent);

        pubsubHelper.sendMessageToSharedProject(uacAuthenticationTopic, messageJson);
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
