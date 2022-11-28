package uk.gov.ons.ssdc.rhservice.messaging;

import static uk.gov.ons.ssdc.rhservice.utils.JsonHelper.convertJsonBytesToEvent;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.rhservice.model.dto.EventDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.SurveyRepository;

@MessageEndpoint
public class SurveyUpdateReceiver {
  private final SurveyRepository surveyRepository;

  public SurveyUpdateReceiver(SurveyRepository surveyRepository) {
    this.surveyRepository = surveyRepository;
  }

  @Transactional
  @ServiceActivator(inputChannel = "surveyUpdateChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {
    EventDTO event = convertJsonBytesToEvent(message.getPayload());
    surveyRepository.writeSurveyUpdate(event.getPayload().getSurveyUpdate());
  }
}
