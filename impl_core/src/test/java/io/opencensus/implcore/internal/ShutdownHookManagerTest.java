/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.implcore.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import io.opencensus.implcore.internal.ShutdownHookManager.Hook;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Unit tests for {@link ShutdownHookManager}. */
@RunWith(JUnit4.class)
public class ShutdownHookManagerTest {

  private static class NoopHook extends Hook {
    public NoopHook(String name, int priority) {
      super(name, priority);
    }

    public NoopHook(String name) {
      super(name);
    }

    @Override
    public void run() {
      throw new UnsupportedOperationException();
    }
  }

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void addShutdownHookDisallowNull() {
    thrown.expect(NullPointerException.class);
    ShutdownHookManager.getInstance().addShutdownHook(null);
  }

  @Test
  public void sortHooksOrder() {
    Hook hook1 = new NoopHook("hook1", 200);
    Hook hook2 = new NoopHook("hook2", 100);
    Hook hook3 = new NoopHook("hook3", 200);
    Hook hook4 = new NoopHook("hook4", 300);
    List<Hook> hooks = Lists.newArrayList(hook1, hook2, hook3, hook4);
    ShutdownHookManager.getInstance().sortHooks(hooks);
    assertThat(hooks).containsExactly(hook4, hook1, hook3, hook2);
  }

  @Test
  public void invokeShutdownHooksShouldIgnoreHookError() {
    Hook exceptionHook = new NoopHook("exceptionHook");
    Hook mockHook = Mockito.spy(new NoopHook("mockHook"));
    ShutdownHookManager.getInstance().addShutdownHook(exceptionHook);
    ShutdownHookManager.getInstance().addShutdownHook(mockHook);
    ShutdownHookManager.getInstance().invokeShutdownHooks();
    // exception of a previous hook does not affect others.
    verify(mockHook).run();
  }

  @Test
  public void hookCreationAndGetter() {
    Hook hook1 = new NoopHook("Test1");
    Hook hook2 = new NoopHook("Test2", 300);
    assertThat(hook1.getName()).isEqualTo("Test1");
    assertThat(hook1.getPriority()).isEqualTo(Hook.DEFAULT_PRIORITY);
    assertThat(hook2.getName()).isEqualTo("Test2");
    assertThat(hook2.getPriority()).isEqualTo(300);
  }
}
