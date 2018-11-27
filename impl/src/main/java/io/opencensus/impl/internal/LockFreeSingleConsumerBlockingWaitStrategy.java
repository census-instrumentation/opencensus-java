package io.opencensus.impl.internal;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.util.ThreadHints;
import java.util.concurrent.locks.LockSupport;

/**
 * A lock-free single-consumer thread wait strategy that parks the thread while waiting for events. This is similar to
 * the {@link com.lmax.disruptor.BlockingWaitStrategy} but aims to incur less synchronization costs on the publisher.
 */
class LockFreeSingleConsumerBlockingWaitStrategy implements WaitStrategy {

  private final Thread thread;

  private volatile boolean unparked;

  LockFreeSingleConsumerBlockingWaitStrategy(Thread thread) {
    if (thread == null) {
      throw new AssertionError();
    }
    this.thread = thread;
  }

  @Override
  public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence, SequenceBarrier barrier)
      throws AlertException, InterruptedException {
    if (thread != Thread.currentThread()) {
      throw new AssertionError("Unexpected thread: " + Thread.currentThread() + " != " + thread);
    }
    long availableSequence;
    if (cursorSequence.get() < sequence) {
      unparked = false;
      while (cursorSequence.get() < sequence) {
        barrier.checkAlert();
        LockSupport.park(this);
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        unparked = false;
      }
    }

    while ((availableSequence = dependentSequence.get()) < sequence) {
      barrier.checkAlert();
      ThreadHints.onSpinWait();
    }

    return availableSequence;
  }

  @Override
  public void signalAllWhenBlocking() {
    // Try to make as few calls to unpark as necessary
    if (unparked) {
      return;
    }
    unparked = true;
    LockSupport.unpark(thread);
  }
}
