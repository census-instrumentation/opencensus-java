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

package io.opencensus.contrib.agent;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration tests for {@link ExecutorInstrumentation}.
 *
 * <p>The integration tests are executed in a separate JVM that has the OpenCensus agent enabled
 * via the {@code -javaagent} command line option.
 */
@RunWith(JUnit4.class)
public class ExecutorInstrumentationTest {

  private static final Context.Key<String> KEY = Context.key("mykey");

  private ExecutorService executor;
  private Context previousContext;

  @Before
  public void beforeMethod() {
    executor = Executors.newCachedThreadPool();
  }

  @After
  public void afterMethod() {
    Context.current().detach(previousContext);
    executor.shutdown();
  }

  @Test(timeout = 5000)
  public void execute() throws Exception {
    final Thread callerThread = Thread.currentThread();
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);

    executor.execute(new Runnable() {
      @Override
      public void run() {
        assertThat(Thread.currentThread()).isNotSameAs(callerThread);
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);

        synchronized (tested) {
          tested.notify();
        }
      }
    });

    synchronized (tested) {
      tested.wait();
    }

    assertThat(tested.get()).isTrue();
  }

  @Test(timeout = 5000)
  public void submit_Callable() throws Exception {
    final Thread callerThread = Thread.currentThread();
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);

    executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        assertThat(Thread.currentThread()).isNotSameAs(callerThread);
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);

        return null;
      }
    }).get();

    assertThat(tested.get()).isTrue();
  }

  @Test(timeout = 5000)
  public void submit_Runnable() throws Exception {
    final Thread callerThread = Thread.currentThread();
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);

    executor.submit(new Runnable() {
      @Override
      public void run() {
        assertThat(Thread.currentThread()).isNotSameAs(callerThread);
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);
      }
    }).get();

    assertThat(tested.get()).isTrue();
  }

  @Test(timeout = 5000)
  public void submit_RunnableWithResult() throws Exception {
    final Thread callerThread = Thread.currentThread();
    final Context context = Context.current().withValue(KEY, "myvalue");
    previousContext = context.attach();

    final AtomicBoolean tested = new AtomicBoolean(false);
    Object result = new Object();

    Future<Object> future = executor.submit(new Runnable() {
      @Override
      public void run() {
        assertThat(Thread.currentThread()).isNotSameAs(callerThread);
        assertThat(Context.current()).isNotSameAs(Context.ROOT);
        assertThat(Context.current()).isSameAs(context);
        assertThat(KEY.get()).isEqualTo("myvalue");
        tested.set(true);
      }
    }, result);

    assertThat(future.get()).isSameAs(result);
    assertThat(tested.get()).isTrue();
  }
}
