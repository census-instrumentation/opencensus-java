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
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.google.auto.service.AutoService;
import com.google.errorprone.annotations.MustBeClosed;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opencensus.contrib.agent.bootstrap.TraceTrampoline;
import java.io.Closeable;
import java.io.IOException;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 * Wraps the execution of {@link java.net.URL#getContent()} in a trace span.
 *
 * <p>TODO(stschmidt): Replace this preliminary, java.net.URL-specific implementation with a
 * generic, configurable implementation.
 */
@AutoService(Instrumenter.class)
public final class UrlInstrumentation implements Instrumenter {

  @Override
  public AgentBuilder instrument(AgentBuilder agentBuilder) {
    checkNotNull(agentBuilder, "agentBuilder");

    return agentBuilder.type(named("java.net.URL")).transform(new Transformer());
  }

  private static class Transformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(
        DynamicType.Builder<?> builder,
        TypeDescription typeDescription,
        ClassLoader classLoader,
        JavaModule module) {
      return builder.visit(Advice.to(GetContent.class).on(named("getContent")));
    }
  }

  private static class GetContent {

    /**
     * Starts a new span and sets it as the current span when entering the method.
     *
     * <p>NB: This method is never called as is. Instead, Byte Buddy copies the method's bytecode
     * into Executor#execute.
     *
     * @see Advice
     */
    @Advice.OnMethodEnter
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @MustBeClosed
    private static Closeable enter(@Advice.Origin("#t\\##m") String classAndMethodName) {
      return TraceTrampoline.startScopedSpan(classAndMethodName);
    }

    /**
     * Closes the current span when exiting the method.
     *
     * <p>NB: This method is never called as is. Instead, Byte Buddy copies the method's bytecode
     * into Executor#execute.
     *
     * <p>NB: By default, any {@link Throwable} thrown during the advice's execution is silently
     * suppressed.
     *
     * @see Advice
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private static void exit(@Advice.Enter Closeable scopedSpanHandle) throws IOException {
      scopedSpanHandle.close();
    }
  }
}
