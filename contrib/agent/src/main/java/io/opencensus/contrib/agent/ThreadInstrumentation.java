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

import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opencensus.contrib.agent.bootstrap.ThreadContextPropagator;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

/**
 * Propagates the context of the caller of {@link Thread#start} to the new thread, just like the
 * Microsoft .Net Framework propagates the <a
 * href="https://msdn.microsoft.com/en-us/library/system.threading.executioncontext(v=vs.110).aspx">System.Threading.ExecutionContext</a>.
 */
final class ThreadInstrumentation {

  private static class Transformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
            TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
      return builder
              .visit(Advice.to(Start.class).on(named("start")))
              .visit(Advice.to(Run.class).on(named("run")));
    }
  }

  static ElementMatcher.Junction<TypeDescription> matcher() {
    return isSubTypeOf(Thread.class);
  }

  static AgentBuilder.Transformer transformer() {
    return new Transformer();
  }

  private static class Start {

    @Advice.OnMethodEnter
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private static void enter(@Advice.This Thread thread) {
      ThreadContextPropagator.beforeThreadStart(thread);
    }
  }

  private static class Run {

    @Advice.OnMethodEnter
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private static void enter(@Advice.This Thread thread) {
      ThreadContextPropagator.beforeThreadRun(thread);
    }
  }
}
