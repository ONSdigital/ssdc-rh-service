package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ssdc.rhservice.service.AuthenicatedMessageSender.OUTBOUND_EVENT_SCHEMA_VERSION;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthenicatedMessageSenderTest {

  public static final String TEST_QID = "TEST_QID";
  public static final String TEST_TOPIC = "Test-Topic";
  public static final UUID CORRELATION_ID = UUID.randomUUID();

  @Mock PubsubHelper pubsubHelper;

  @InjectMocks AuthenicatedMessageSender underTest;

  @Test
  public void testMessageSent() throws JsonProcessingException {
    Map<String, Object> payload = new HashMap<>();
    payload.put("questionnaire_id", TEST_QID);
    payload.put("tx_id", CORRELATION_ID);

    ReflectionTestUtils.setField(underTest, "uacAuthenticationTopic", TEST_TOPIC);

    underTest.buildAndSendUacAuthentication(payload);

    ArgumentCaptor<String> eventArgCaptor = ArgumentCaptor.forClass(String.class);
    verify(pubsubHelper).sendMessageToSharedProject(eq(TEST_TOPIC), eventArgCaptor.capture());

    EventDTO eventDTO = ObjectMapperFactory.objectMapper().readValue(eventArgCaptor.getValue(), EventDTO.class);

    EventHeaderDTO eventHeaderDTO = eventDTO.getHeader();
    assertThat(eventHeaderDTO.getVersion()).isEqualTo(OUTBOUND_EVENT_SCHEMA_VERSION);
    assertThat(eventHeaderDTO.getTopic()).isEqualTo(TEST_TOPIC);
    assertThat(eventHeaderDTO.getSource()).isEqualTo("RESPONDENT HOME");
    assertThat(eventHeaderDTO.getChannel()).isEqualTo("RH");
    assertThat(eventHeaderDTO.getDateTime())
        .isCloseTo(OffsetDateTime.now(), within(5, ChronoUnit.SECONDS));
    assertThat(eventHeaderDTO.getMessageId()).isNotNull();
    assertThat(eventHeaderDTO.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(eventHeaderDTO.getOriginatingUser()).isEqualTo("RH");
  }
}
