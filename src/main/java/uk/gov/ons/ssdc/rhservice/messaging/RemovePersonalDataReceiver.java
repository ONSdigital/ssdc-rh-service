package uk.gov.ons.ssdc.rhservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

@MessageEndpoint
public class RemovePersonalDataReceiver {

  private static final Logger log = LoggerFactory.getLogger(RemovePersonalDataReceiver.class);

  @ServiceActivator(inputChannel = "removePersonalDataUpdateChannel", adviceChain = "retryAdvice")
  public void receiveMessage(Message<byte[]> message) {
    log.info("Received remove personal data message");
  }
}
