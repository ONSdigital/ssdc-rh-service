package uk.gov.ons.ssdc.rhservice.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class CloudRetryListener extends RetryListenerSupport {

  private static final Logger log = LoggerFactory.getLogger(CloudRetryListener.class);

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    Object operationName = context.getAttribute(RetryContext.NAME);
    log.warn("Retry failed: " + operationName);
  }

  @Override
  public <T, E extends Throwable> void close(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

    // Spring retries have completed. Report on outcome if retries have been used.
    if (context.getRetryCount() > 0) {
      Object operationName = context.getAttribute(RetryContext.NAME);

      if (throwable != null) {

        // On failure the retryCount actually holds the number of attempts
        int numAttempts = context.getRetryCount();
        log.warn(
            String.format("%s Transaction failed after %s attempts", operationName, numAttempts));
      }
    }
  }
}
