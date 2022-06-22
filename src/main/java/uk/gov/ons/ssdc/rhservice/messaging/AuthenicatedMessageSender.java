package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertObjectToJson;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventHeaderDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacAuthenticationDTO;
import uk.gov.ons.ssdc.rhservice.utils.PubsubHelper;

@Component
public class AuthenicatedMessageSender {
  public static final String OUTBOUND_EVENT_SCHEMA_VERSION = "0.5.0";
  private final PubsubHelper pubsubHelper;

  @Value("${queueconfig.uac-authentication-topic}")
  private String uacAuthenticationTopic;

  public AuthenicatedMessageSender(PubsubHelper pubsubHelper) {
    this.pubsubHelper = pubsubHelper;
  }

  public void buildAndSendUacAuthentication(Map<String, Object> payload) {
    EventDTO eqLaunchedEvent = new EventDTO();
    EventHeaderDTO eventHeader = new EventHeaderDTO();
    eventHeader.setVersion(OUTBOUND_EVENT_SCHEMA_VERSION);
    eventHeader.setTopic(uacAuthenticationTopic);
    eventHeader.setSource("RESPONDENT HOME");
    eventHeader.setChannel("RH");
    eventHeader.setDateTime(OffsetDateTime.now());
    eventHeader.setMessageId(UUID.randomUUID());
    eventHeader.setCorrelationId(UUID.fromString(payload.get("tx_id").toString()));
    eventHeader.setOriginatingUser("RH");

    eqLaunchedEvent.setHeader(eventHeader);

    UacAuthenticationDTO uacAuthentication = new UacAuthenticationDTO();
    uacAuthentication.setQid(payload.get("questionnaire_id").toString());
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUacAuthenticationDTO(uacAuthentication);
    eqLaunchedEvent.setPayload(payloadDTO);

    String messageJson = convertObjectToJson(eqLaunchedEvent);
    pubsubHelper.sendMessageToSharedProject(uacAuthenticationTopic, messageJson);
  }
}
