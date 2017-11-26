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

import static com.google.common.base.Preconditions.checkNotNull;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.google.auto.service.AutoService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opencensus.contrib.agent.Settings;
import io.opencensus.contrib.agent.bootstrap.ContextTrampoline;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 * Propagates the context of the caller of {@link Thread#start} to the new thread, just like the
 * Microsoft .Net Framework propagates the <a
 * href="https://msdn.microsoft.com/en-us/library/system.threading.executioncontext(v=vs.110).aspx">System.Threading.ExecutionContext</a>.
 *
 * <p>NB: A similar effect could be achieved with {@link InheritableThreadLocal}, but the semantics
 * are different: {@link InheritableThreadLocal} inherits values when the thread object is
 * initialized as opposed to when {@link Thread#start()} is called.
 */
@AutoService(Instrumenter.class)
public final class ThreadInstrumentation implements Instrumenter {

  @Override
  public AgentBuilder instrument(AgentBuilder agentBuilder, Settings settings) {
    checkNotNull(agentBuilder, "agentBuilder");
    checkNotNull(settings, "settings");

    if (!settings.isEnabled("context-propagation.thread")) {
      return agentBuilder;
    }

    return agentBuilder.type(isSubTypeOf(Thread.class)).transform(new Transformer());
  }

  private static class Transformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(
        DynamicType.Builder<?> builder,
        TypeDescription typeDescription,
        ClassLoader classLoader,
        JavaModule module) {
      return builder
          .visit(Advice.to(Start.class).on(named("start")))
          .visit(Advice.to(Run.class).on(named("run")));
    }
  }

  private static class Start {

    /**
     * Saves the context that is associated with the current scope.
     *
     * <p>The context will be attached when entering the thread's {@link Thread#run()} method.
     *
     * <p>NB: This method is never called as is. Instead, Byte Buddy copies the method's bytecode
     * into Thread#start.
     *
     * @see Advice
     */
    @Advice.OnMethodEnter
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private static void enter(@Advice.This Thread thread) {
      ContextTrampoline.saveContextForThread(thread);
    }
  }

  private static class Run {

    /**
     * Attaches the context that was previously saved for this thread.
     *
     * <p>NB: This method is never called as is. Instead, Byte Buddy copies the method's bytecode
     * into Thread#run.
     *
     * @see Advice
     */
    @Advice.OnMethodEnter
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private static void enter(@Advice.This Thread thread) {
      ContextTrampoline.attachContextForThread(thread);
    }
  }
}
