/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.agent.instrumentation;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration tests for {@link ThreadInstrumentation}.
 *
 * <p>The integration tests are executed in a separate JVM that has the OpenCensus agent enabled
 * via the {@code -javaagent} command line option.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ThreadInstrumentationIT {

  private static final Context.Key<String> KEY = Context.key("mykey");

  private Context previousContext;

  @After
  public void afterMethod() {
    Context.current().detach(previousContext);
  }

  @Test(timeout = 5000)
  public void start_Runnable() throws Exception {
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);

    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);
      }
    });

    thread.start();
    thread.join();

    assertThat(tested.get()).isTrue();
  }

  @Test(timeout = 5000)
  public void start_Subclass() throws Exception {
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);

    class MyThread extends Thread {

      @Override
      public void run() {
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);
      }
    }

    Thread thread = new MyThread();

    thread.start();
    thread.join();

    assertThat(tested.get()).isTrue();
  }

  /**
   * Tests that the automatic context propagation added by {@link ThreadInstrumentation} does not
   * interfere with the automatically propagated context from Executor#execute.
   */
  @Test(timeout = 5000)
  public void start_automaticallyWrappedRunnable() throws Exception {
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);

    Executor newThreadExecutor = new Executor() {
      @Override
      public void execute(Runnable command) {
        // Attach a new context before starting a new thread. This new context will be propagated to
        // the new thread as in #start_Runnable. However, since the Runnable has been wrapped in a
        // different context (by automatic instrumentation of Executor#execute), that context will
        // be attached when executing the Runnable.
        Context context2 = Context.current().withValue(KEY, "wrong context");
        context2.attach();
        Thread thread = new Thread(command);
        thread.start();
        try {
          thread.join();
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
        context2.detach(context);
      }
    };

    newThreadExecutor.execute(new Runnable() {
      @Override
      public void run() {
        // Assert that the automatic context propagation added by ThreadInstrumentation did not
        // interfere with the automatically propagated context from Executor#execute.
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);
      }
    });

    assertThat(tested.get()).isTrue();
  }
}
