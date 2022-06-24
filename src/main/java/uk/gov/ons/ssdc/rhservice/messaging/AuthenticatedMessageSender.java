package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertObjectToJson;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventHeaderDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.rhservice.utils.PubsubHelper;

@Component
public class AuthenticatedMessageSender {
  public static final String OUTBOUND_EVENT_SCHEMA_VERSION = "0.5.0";
  private final PubsubHelper pubsubHelper;

  @Value("${queueconfig.eq-launch-topic}")
  private String eqLaunchTopic;

  public AuthenticatedMessageSender(PubsubHelper pubsubHelper) {
    this.pubsubHelper = pubsubHelper;
  }

  public void buildAndSendEqLaunchEvent(Map<String, Object> payload) {
    EventDTO eqLaunchedEvent = new EventDTO();
    EventHeaderDTO eventHeader = new EventHeaderDTO();
    eventHeader.setVersion(OUTBOUND_EVENT_SCHEMA_VERSION);
    eventHeader.setTopic(eqLaunchTopic);
    eventHeader.setSource("RESPONDENT HOME");
    eventHeader.setChannel("RH");
    eventHeader.setDateTime(OffsetDateTime.now());
    eventHeader.setMessageId(UUID.randomUUID());
    eventHeader.setCorrelationId(UUID.fromString(payload.get("tx_id").toString()));
    eventHeader.setOriginatingUser("RH");

    eqLaunchedEvent.setHeader(eventHeader);

    EqLaunchDTO eqLaunchDTO = new EqLaunchDTO();
    eqLaunchDTO.setQid(payload.get("questionnaire_id").toString());
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setEqLaunchDTO(eqLaunchDTO);
    eqLaunchedEvent.setPayload(payloadDTO);

    String messageJson = convertObjectToJson(eqLaunchedEvent);
    pubsubHelper.sendMessageToSharedProject(eqLaunchTopic, messageJson);
  }
}
