package uk.gov.ons.ssdc.rhservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ssdc.rhservice.messaging.EqLaunchSender.OUTBOUND_EVENT_SCHEMA_VERSION;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EventHeaderDTO;
import uk.gov.ons.ssdc.rhservice.utils.ObjectMapperFactory;
import uk.gov.ons.ssdc.rhservice.utils.PubsubHelper;

@ExtendWith(MockitoExtension.class)
class EqLaunchSenderTest {

  public static final String TEST_QID = "TEST_QID";
  public static final String TEST_TOPIC = "Test-Topic";
  public static final UUID CORRELATION_ID = UUID.randomUUID();

  @Mock PubsubHelper pubsubHelper;

  @InjectMocks EqLaunchSender underTest;

  @Test
  void testMessageSent() throws JsonProcessingException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("qid", TEST_QID);
    payload.put("tx_id", CORRELATION_ID);
    OffsetDateTime testStart = OffsetDateTime.now();

    ReflectionTestUtils.setField(underTest, "eqLaunchTopic", TEST_TOPIC);

    underTest.buildAndSendEqLaunchEvent(payload, TEST_QID);

    ArgumentCaptor<String> eventArgCaptor = ArgumentCaptor.forClass(String.class);
    verify(pubsubHelper).sendMessageToPubsubProject(eq(TEST_TOPIC), eventArgCaptor.capture());

    EventDTO eventDTO =
        ObjectMapperFactory.objectMapper().readValue(eventArgCaptor.getValue(), EventDTO.class);

    EventHeaderDTO eventHeaderDTO = eventDTO.getHeader();
    assertThat(eventHeaderDTO.getVersion()).isEqualTo(OUTBOUND_EVENT_SCHEMA_VERSION);
    assertThat(eventHeaderDTO.getTopic()).isEqualTo(TEST_TOPIC);
    assertThat(eventHeaderDTO.getSource()).isEqualTo("RESPONDENT HOME");
    assertThat(eventHeaderDTO.getChannel()).isEqualTo("RH");
    assertThat(eventHeaderDTO.getDateTime()).isBetween(testStart, OffsetDateTime.now());
    assertThat(eventHeaderDTO.getMessageId()).isNotNull();
    assertThat(eventHeaderDTO.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(eventHeaderDTO.getOriginatingUser()).isEqualTo("RH");
  }
}
