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

import static com.google.common.base.Preconditions.checkState;
import static net.bytebuddy.matcher.ElementMatchers.none;

import io.opencensus.contrib.agent.bootstrap.ContextProxy;
import java.lang.instrument.Instrumentation;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * The <b>Google Instrumentation Agent for Java</b> collects and sends latency data about your Java
 * process to Stackdriver Trace for analysis and visualization in the Google Cloud Console.
 *
 * <p>To enable the *Google Instrumentation Agent for Java* for your application, add the option
 * <code>-javaagent:path/to/google-instrumentation-agent.jar</code> to the invocation of the
 * <code>java</code> executable as shown in the following example:
 *
 * <pre>
 * java -javaagent:path/to/google-instrumentation-agent.jar ...
 * </pre>
 * 
 * @see <a href="https://github.com/census-instrumentation/instrumentation-java/tree/master/agent">https://github.com/census-instrumentation/instrumentation-java/tree/master/agent</a>
 */
public final class AgentMain {

  private static final Logger logger = Logger.getLogger(AgentMain.class.getName());

  private AgentMain() {
  }

  /**
   * Initializes the Google Instrumentation Agent for Java.
   *
   * @param agentArgs agent options, passed as a single string by the JVM
   * @param inst      the {@link Instrumentation} object provided by the JVM for instrumenting Java
   *                  programming language code
   * @throws Exception if initialization of the agent fails
   *
   * @see java.lang.instrument
   */
  public static void premain(String agentArgs, Instrumentation inst) throws Exception {
    logger.fine("Initializing.");

    // The classes in bootstrap.jar will be referenced from classes loaded by the bootstrap
    // classloader. Thus, these classes have to be loaded by the bootstrap classloader, too.
    // NB: The Boot-Class-Path MANIFEST.MF entry can only refer to files outside the agent's JAR.
    // Also, the JAR file must be an actual file. See
    // sun.instrument.InstrumentationImpl.appendToBootstrapClassLoaderSearch(
    // InstrumentationImpl.java:195), where the JarFile's name is passed verbatim to native code.
    inst.appendToBootstrapClassLoaderSearch(new JarFile(Resources.toTempFile("bootstrap.jar")));

    checkState(ContextProxy.class.getClassLoader() == null);

    AgentBuilder agentBuilder = new AgentBuilder.Default()
            .disableClassFormatChanges()
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(new AgentBuilderListener())
            .ignore(none());
    agentBuilder = LazyLoaded.addContextPropagation(agentBuilder);
    agentBuilder.installOn(inst);

    logger.fine("Initialized.");
  }

  private static class LazyLoaded {

    // Avoid triggering Errorprone's warning about using {@link Class#forName} on a
    // class that is on the compile-time classpath. Though the class is on the compile-time
    // classpath, it may be missing at run time, in which case the bytecode will not be instrumented
    // for automatic context progagation.
    private static final String CONTEXT_CLASS_NAME = "io.grpc.".toLowerCase(Locale.US) + "Context";

    /**
     * Adds automatic context propagation, if {@link io.grpc.Context} is available.
     */
    static AgentBuilder addContextPropagation(AgentBuilder agentBuilder) {
      try {
        Class.forName(CONTEXT_CLASS_NAME);

        ContextProxy.init(new ContextProxyImpl());

        agentBuilder = agentBuilder
                .type(ExecutorInstrumentation.matcher())
                .transform(ExecutorInstrumentation.transformer());

        agentBuilder = agentBuilder
                .type(ThreadInstrumentation.matcher())
                .transform(ThreadInstrumentation.transformer());
      } catch (ClassNotFoundException e) {
        logger.log(Level.FINE, "Could not load io.grpc.Context, therefore not instrumenting "
                + "the bytecode for automatic context propagation.", e);
      }

      return agentBuilder;
    }
  }
}
