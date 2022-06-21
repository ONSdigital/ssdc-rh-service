package uk.gov.ons.ssdc.rhservice.utils;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

@Component
@EnableRetry
public class PubsubHelper {

  @Autowired
  private PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  public void sendMessageToSharedProject(String topicName, Object message) {
    String fullyQualifiedTopic = toProjectTopicName(topicName, sharedPubsubProject).toString();
    sendMessage(fullyQualifiedTopic, message);
  }

  @Retryable(
      value = {IOException.class},
      maxAttempts = 10,
      backoff = @Backoff(delay = 5000))
  public void sendMessage(String topicName, Object message) {
    ListenableFuture<String> future = pubSubTemplate.publish(topicName, message);

    try {
      future.get(30, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
