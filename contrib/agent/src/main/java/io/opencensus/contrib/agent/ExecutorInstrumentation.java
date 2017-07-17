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

import static net.bytebuddy.matcher.ElementMatchers.isAbstract;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opencensus.contrib.agent.bootstrap.ContextManager;
import java.util.concurrent.Executor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

/**
 * Propagates the context of the caller of {@link Executor#execute} to the submitted
 * {@link Runnable}, just like the Microsoft .Net Framework propagates the <a
 * href="https://msdn.microsoft.com/en-us/library/system.threading.executioncontext(v=vs.110).aspx">System.Threading.ExecutionContext</a>.
 */
final class ExecutorInstrumentation {

  private static class Transformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
            TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
      return builder.visit(Advice.to(Execute.class).on(named("execute")));
    }
  }

  static ElementMatcher.Junction<TypeDescription> createMatcher() {
    // TODO(stschmidt): Exclude known call sites that already propagate the context, such as
    // io.grpc.Context#currentContextExecutor.

    return isSubTypeOf(Executor.class).and(not(isAbstract()));
  }

  static AgentBuilder.Transformer createTransformer() {
    return new Transformer();
  }

  private static class Execute {

    /**
     * Wraps a {@link Runnable} so that it executes with the context that is associated with the
     * current scope.
     *
     * <p>NB: This method is never called as is. Instead, Byte Buddy copies the method's bytecode
     * into Executor#execute.
     *
     * @see Advice
     */
    @Advice.OnMethodEnter
    @SuppressWarnings(value = "UnusedAssignment")
    @SuppressFBWarnings(value = {"DLS_DEAD_LOCAL_STORE", "UPM_UNCALLED_PRIVATE_METHOD",})
    private static void enter(@Advice.Argument(value = 0, readOnly = false) Runnable runnable) {
      runnable = ContextManager.wrapInCurrentContext(runnable);
    }
  }
}
