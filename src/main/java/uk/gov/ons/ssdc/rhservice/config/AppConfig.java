package uk.gov.ons.ssdc.rhservice.config;

import com.godaddy.logging.LoggingConfigs;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.PublisherFactory;
import com.google.cloud.spring.pubsub.support.SubscriberFactory;
import com.google.cloud.spring.pubsub.support.converter.SimplePubSubMessageConverter;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Value("${logging.profile}")
  private String loggingProfile;

  @Bean
  public PubSubTemplate pubSubTemplate(
      PublisherFactory publisherFactory,
      SubscriberFactory subscriberFactory,
      SimplePubSubMessageConverter simplePubSubMessageConverter) {
    PubSubTemplate pubSubTemplate = new PubSubTemplate(publisherFactory, subscriberFactory);
    pubSubTemplate.setMessageConverter(simplePubSubMessageConverter);
    return pubSubTemplate;
  }

  @Bean
  public SimplePubSubMessageConverter messageConverter() {
    return new SimplePubSubMessageConverter();
  }

  @PostConstruct
  public void init() {
    if ("STRUCTURED".equals(loggingProfile)) {
      LoggingConfigs.setCurrent(LoggingConfigs.getCurrent().useJson());
    }

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
}
