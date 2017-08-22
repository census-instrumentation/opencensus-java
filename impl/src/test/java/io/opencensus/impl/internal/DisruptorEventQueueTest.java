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

import io.opencensus.impl.internal.DisruptorEventQueue;
import io.opencensus.implcore.internal.EventQueue;
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
    private int count;
    private volatile long id; // stores thread ID used in first increment operation.

    public Counter() {
      count = 0;
      id = -1;
    }

    // Increments counter by 1. Will fail in assertion if multiple different threads are used
    // (the EventQueue backend should be single-threaded).
    public void increment() {
      long tid = Thread.currentThread().getId();
      if (id == -1) {
        assertThat(count).isEqualTo(0);
        id = tid;
      } else {
        assertThat(id).isEqualTo(tid);
      }
      count++;
    }

    // Check the current value of the counter. Assert if it is not the expected value.
    public void check(int value) {
      assertThat(count).isEqualTo(value);
    }
  }

  // EventQueueEntry for incrementing a Counter.
  private static class IncrementEvent implements EventQueue.Entry {
    private final Counter counter;

    IncrementEvent(Counter counter) {
      this.counter = counter;
    }

    @Override
    public void process() {
      counter.increment();
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
    Counter counter = new Counter();
    for (int i = 0; i < tenK; i++) {
      IncrementEvent ie = new IncrementEvent(counter);
      DisruptorEventQueue.getInstance().enqueue(ie);
    }
    // Sleep briefly, to allow background operations to complete.
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    counter.check(tenK);
  }
}
