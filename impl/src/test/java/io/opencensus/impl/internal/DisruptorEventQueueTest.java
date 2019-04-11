/*
 * Copyright 2016-17, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.impl.internal;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.internal.EventQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DisruptorEventQueue}. */
@RunWith(JUnit4.class)
public class DisruptorEventQueueTest {

  // Simple class to use that keeps an incrementing counter. Will fail with an assertion if
  // increment is used from multiple threads, or if the stored value is different from that expected
  // by the caller.
  private static class Counter {

    @Nullable private final CountDownLatch latch;
    private final AtomicInteger count = new AtomicInteger();
    private volatile long id; // stores thread ID used in first increment operation.

    public Counter() {
      this(null);
    }

    public Counter(@Nullable CountDownLatch latch) {
      this.latch = latch;
      id = -1;
    }

    // Increments counter by 1. Will fail in assertion if multiple different threads are used
    // (the EventQueue backend should be single-threaded).
    public void increment() {
      if (latch != null) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      long tid = Thread.currentThread().getId();
      if (id == -1) {
        assertThat(count.get()).isEqualTo(0);
        id = tid;
      } else {
        assertThat(id).isEqualTo(tid);
      }
      count.incrementAndGet();
    }

    // Check the current value of the counter. Assert if it is not the expected value.
    public void check(int value) {
      assertThat(count.get()).isEqualTo(value);
    }
  }

  // EventQueueEntry for incrementing a Counter.
  private static class IncrementEvent implements EventQueue.Entry {

    private final Counter counter;
    private volatile boolean isRejected = false;

    IncrementEvent(Counter counter) {
      this.counter = counter;
    }

    @Override
    public void process() {
      counter.increment();
    }

    @Override
    public void rejected() {
      isRejected = true;
    }
  }

  @Test
  public void incrementOnce() {
    Counter counter = new Counter();
    IncrementEvent ie = new IncrementEvent(counter);
    DisruptorEventQueue.getInstance().enqueue(ie);
    // Sleep briefly, to allow background operations to complete.
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    counter.check(1);
  }

  @Test
  public void incrementTenK() {
    final int tenK = 10000;
    final int sleepEach = 1000;
    Counter counter = new Counter();
    for (int i = 0; i < tenK; i++) {
      IncrementEvent ie = new IncrementEvent(counter);
      DisruptorEventQueue.getInstance().enqueue(ie);

      // Sleep to prevent queue overflow
      if (i % sleepEach == 0) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }
    // Sleep briefly, to allow background operations to complete.
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    counter.check(tenK);
  }

  @Test(timeout = 30 * 1000)
  public void shouldNotBlockWhenOverflow() {
    long overflowCountBefore = DisruptorEventQueue.overflowCount.get();

    // Queue blocking events to fill queue
    CountDownLatch latch = new CountDownLatch(1);
    Counter counter = new Counter(latch);
    for (int i = 0; i < DisruptorEventQueue.DISRUPTOR_BUFFER_SIZE; i++) {
      IncrementEvent ie = new IncrementEvent(counter);
      DisruptorEventQueue.getInstance().enqueue(ie);
    }
    counter.check(0);

    // Queue event into filled queue to test overflow behavior
    IncrementEvent ie = new IncrementEvent(counter);
    DisruptorEventQueue.getInstance().enqueue(ie);
    assertThat(ie.isRejected).isTrue();
    assertThat(DisruptorEventQueue.overflowCount.get()).isEqualTo(overflowCountBefore + 1);

    // Cleanup events
    latch.countDown();
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    counter.check(DisruptorEventQueue.DISRUPTOR_BUFFER_SIZE);
  }
}
