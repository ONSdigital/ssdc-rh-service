package uk.gov.ons.ssdc.rhservice.config;

import static com.google.cloud.spring.pubsub.support.PubSubSubscriptionUtils.toProjectSubscriptionName;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.RetryListener;
import uk.gov.ons.ssdc.rhservice.messaging.ManagedMessageRecoverer;
import uk.gov.ons.ssdc.rhservice.service.CloudRetryListener;

@Configuration
public class MessageConsumerConfig {
  private final ManagedMessageRecoverer managedMessageRecoverer;
  private final PubSubTemplate pubSubTemplate;

  @Value("${spring.cloud.gcp.pubsub.project-id}")
  private String pubsubProject;

  @Value("${queueconfig.case-update-subscription}")
  private String caseUpdateSubscription;

  @Value("${queueconfig.uac-update-subscription}")
  private String uacUpdateSubscription;

  @Value("${queueconfig.collection-exercise-update-subscription}")
  private String collectionExerciseSubscription;

  public MessageConsumerConfig(
      ManagedMessageRecoverer managedMessageRecoverer, PubSubTemplate pubSubTemplate) {
    this.managedMessageRecoverer = managedMessageRecoverer;
    this.pubSubTemplate = pubSubTemplate;
  }

  @Bean
  public MessageChannel caseUpdateInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel uacUpdateInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel collectionExerciseUpdateChannel() {
    return new DirectChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter newCaseInbound(
      @Qualifier("caseUpdateInputChannel") MessageChannel channel) {
    String subscription =
        toProjectSubscriptionName(caseUpdateSubscription, pubsubProject).toString();
    return makeAdapter(channel, subscription);
  }

  @Bean
  public PubSubInboundChannelAdapter newUacInbound(
      @Qualifier("uacUpdateInputChannel") MessageChannel channel) {
    String subscription =
        toProjectSubscriptionName(uacUpdateSubscription, pubsubProject).toString();
    return makeAdapter(channel, subscription);
  }

  @Bean
  public PubSubInboundChannelAdapter newCollectionExerciseInbound(
      @Qualifier("collectionExerciseUpdateChannel") MessageChannel channel) {
    String subscription =
        toProjectSubscriptionName(collectionExerciseSubscription, pubsubProject).toString();
    return makeAdapter(channel, subscription);
  }

  private PubSubInboundChannelAdapter makeAdapter(MessageChannel channel, String subscriptionName) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
    adapter.setOutputChannel(channel);
    adapter.setAckMode(AckMode.AUTO);
    return adapter;
  }

  @Bean
  public RequestHandlerRetryAdvice retryAdvice() {
    RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
    requestHandlerRetryAdvice.setRecoveryCallback(managedMessageRecoverer);
    return requestHandlerRetryAdvice;
  }

  @Bean
  public RetryListener retryListener() {
    RetryListener retryListener = new CloudRetryListener();

    return retryListener;
  }
}
