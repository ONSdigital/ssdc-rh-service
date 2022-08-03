package uk.gov.ons.ssdc.rhservice.testutils;

import com.google.cloud.pubsub.v1.Subscriber;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class QueueSpy<T> implements AutoCloseable {
  @Getter private BlockingQueue<T> queue;
  private Subscriber subscriber;

  @Override
  public void close() {
    subscriber.stopAsync();
  }

  public T checkExpectedMessageReceived() throws InterruptedException {
    return queue.poll(20, TimeUnit.SECONDS);
  }
}
