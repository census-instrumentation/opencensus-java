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

package io.opencensus.contrib.agent.bootstrap;

import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link ContextManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextManagerTest {

  private static final ContextStrategy mockContextStrategy;

  static {
    mockContextStrategy = mock(ContextStrategy.class);
    ContextManager.setContextStrategy(mockContextStrategy);
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private Runnable runnable;

  @Mock
  private Thread thread;

  @Test
  public void setContextStrategy_already_initialized() {
    exception.expect(IllegalStateException.class);

    ContextManager.setContextStrategy(mockContextStrategy);
  }

  @Test
  public void wrapInCurrentContext() {
    ContextManager.wrapInCurrentContext(runnable);

    Mockito.verify(mockContextStrategy).wrapInCurrentContext(runnable);
  }

  @Test
  public void saveContextForThread() {
    ContextManager.saveContextForThread(thread);

    Mockito.verify(mockContextStrategy).saveContextForThread(thread);
  }

  @Test
  public void attachContextForThread() {
    ContextManager.attachContextForThread(thread);

    Mockito.verify(mockContextStrategy).attachContextForThread(thread);
  }
}
