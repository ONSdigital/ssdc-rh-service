package uk.gov.ons.ssdc.rhservice.testutils;

import com.google.cloud.pubsub.v1.Subscriber;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNull;

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

  public void checkMessageIsNotReceived(int timeOut) throws InterruptedException {
    T actualMessage = queue.poll(timeOut, TimeUnit.SECONDS);
    assertNull(actualMessage, "Message received when not expected");
  }
}
