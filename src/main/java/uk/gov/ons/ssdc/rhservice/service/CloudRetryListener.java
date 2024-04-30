package uk.gov.ons.ssdc.rhservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class CloudRetryListener implements RetryListener {

  private static final Logger log = LoggerFactory.getLogger(CloudRetryListener.class);

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    Object operationName = context.getAttribute(RetryContext.NAME);
    log.atWarn().setMessage("Retry failed: " + operationName).log();
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
        log.atWarn()
            .setMessage(
                String.format(
                    "%s Transaction failed after %s attempts", operationName, numAttempts))
            .log();
      }
    }
  }

  @Override
  public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
    return RetryListener.super.open(context, callback);
  }
}
