/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.common;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.Executors;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A low-latency event queue for background updating of (possibly contended) objects. This is
 * intended for use by instrumentation methods to ensure that they do not block foreground
 * activities. To customize the action taken on reading the queue, derive a new class from {@link
 * EventQueue.Entry} and pass it to the {@link #enqueue(Entry)} method. The {@link Entry#process()}
 * method of your class will be called and executed in a background thread. This class is a
 * Singleton.
 *
 * <p>Example Usage: Given a class as follows:
 *
 * <pre>
 * public class someClass {
 *   public void doSomething() {
 *     // Do the work of the method. One result is a measurement of something.
 *     int measurement = doSomeWork();
 *     // Make an update to the class state, based on this measurement. This work can take some
 *     // time, but can be done asynchronously, in the background.
 *     update(measurement);
 *   }
 *
 *   public void update(int arg) {
 *     // do something
 *   }
 * }
 * </pre>
 *
 * <p>The work of calling {@code someClass.update()} can be executed in the backgound as follows:
 *
 * <pre>
 * public class someClass {
 *   // Add a EventQueueEntry class that will process the update call.
 *   private static final class SomeClassUpdateEvent implements EventQueueEntry {
 *     private final SomeClass someClassInstance;
 *     private final int arg;
 *
 *     SomeObjectUpdateEvent(SomeObject someClassInstance, int arg) {
 *       this.someClassInstance = someClassInstance;
 *       this.arg = arg;
 *     }
 *
 *     &#064;Override
 *     public void process() {
 *       someClassInstance.update(arg);
 *     }
 *   }
 *
 *   public void doSomething() {
 *     int measurement = doSomeWork();
 *     // Instead of calling update() directly, create an event to do the processing, and insert
 *     // it into the EventQueue. It will be processed in a background thread, and doSomething()
 *     // can return immediately.
 *     EventQueue.getInstance.enqueue(new SomeClassUpdateEvent(this, measurement));
 *   }
 * }
 * </pre>
 */
@ThreadSafe
public final class DisruptorEventQueue implements EventQueue {
  // An event in the {@link EventQueue}. Just holds a reference to an EventQueueEntry.
  private static final class InstrumentationEvent {
    private Entry entry = null;

    // Sets the EventQueueEntry associated with this InstrumentationEvent.
    void setEntry(Entry entry) {
      this.entry = entry;
    }

    // Returns the EventQueueEntry associated with this InstrumentationEvent.
    Entry getEntry() {
      return entry;
    }
  }

  // Factory for InstrumentationEvent.
  private static final class InstrumentationEventFactory
      implements EventFactory<InstrumentationEvent> {
    @Override
    public InstrumentationEvent newInstance() {
      return new InstrumentationEvent();
    }
  }

  // Every event that gets added to {@link EventQueue} will get processed here. Just calls the
  // underlying process() method.
  private static final class InstrumentationEventHandler
      implements EventHandler<InstrumentationEvent> {
    @Override
    public void onEvent(InstrumentationEvent event, long sequence, boolean endOfBatch) {
      event.getEntry().process();
    }
  }

  // The single instance of the class.
  private static final DisruptorEventQueue eventQueue = new DisruptorEventQueue();
  // The event queue is built on this {@link Disruptor}.
  private final Disruptor<InstrumentationEvent> disruptor;
  // Ring Buffer for the {@link Disruptor} that underlies the queue.
  private final RingBuffer<InstrumentationEvent> ringBuffer;

  // Creates a new EventQueue. Private to prevent creation of non-singleton instance.
  // Suppress warnings for disruptor.handleEventsWith and Disruptor constructor
  @SuppressWarnings({"deprecation", "unchecked", "varargs"})
  private DisruptorEventQueue() {
    // Number of events that can be enqueued at any one time. If more than this are enqueued,
    // then subsequent attempts to enqueue new entries will block.
    // TODO(aveitch): consider making this a parameter to the constructor, so the queue can be
    // configured to a size appropriate to the system (smaller/less busy systems will not need as
    // large a queue.
    final int bufferSize = 8192;
    // Create new Disruptor for processing. Note that this uses a single thread for processing; this
    // ensures that the event handler can take unsynchronized actions whenever possible.
    disruptor =
        new Disruptor<InstrumentationEvent>(
            new InstrumentationEventFactory(),
            bufferSize,
            Executors.newSingleThreadExecutor(),
            ProducerType.MULTI,
            new SleepingWaitStrategy());
    disruptor.handleEventsWith(new InstrumentationEventHandler());
    disruptor.start();
    ringBuffer = disruptor.getRingBuffer();
  }

  /**
   * Returns the {@link DisruptorEventQueue} instance.
   *
   * @return the singleton {@code EventQueue} instance.
   */
  public static DisruptorEventQueue getInstance() {
    return eventQueue;
  }

  /**
   * Enqueues an event on the {@link DisruptorEventQueue}.
   *
   * @param entry a class encapsulating the actions to be taken for event processing.
   */
  @Override
  public void enqueue(Entry entry) {
    long sequence = ringBuffer.next();
    try {
      InstrumentationEvent event = ringBuffer.get(sequence);
      event.setEntry(entry);
    } finally {
      ringBuffer.publish(sequence);
    }
  }
}
