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
import uk.gov.ons.ssdc.rhservice.messaging.ManagedMessageRecoverer;

@Configuration
public class MessageConsumerConfig {
  private final ManagedMessageRecoverer managedMessageRecoverer;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  @Value("${queueconfig.case-update-subscription}")
  private String caseUpdateSubscription;

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
  public PubSubInboundChannelAdapter newCaseInbound(
      @Qualifier("caseUpdateInputChannel") MessageChannel channel) {
    String subscription =
        toProjectSubscriptionName(caseUpdateSubscription, sharedPubsubProject).toString();
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
}
