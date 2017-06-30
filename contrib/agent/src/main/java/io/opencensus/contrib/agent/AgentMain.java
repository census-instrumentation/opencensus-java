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

import static net.bytebuddy.matcher.ElementMatchers.none;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * The <b>OpenCensus Agent for Java</b> collects and sends latency data about your Java
 * process to OpenCensus backends such as Stackdriver Trace for analysis and visualization.
 *
 * <p>To enable the *OpenCensus Agent for Java* for your application, add the option
 * <code>-javaagent:path/to/opencensus-agent.jar</code> to the invocation of the
 * <code>java</code> executable as shown in the following example:
 *
 * <pre>
 * java -javaagent:path/to/opencensus-agent.jar ...
 * </pre>
 *
 * @see <a href="https://github.com/census-instrumentation/instrumentation-java/tree/master/agent">https://github.com/census-instrumentation/instrumentation-java/tree/master/agent</a>
 */
public final class AgentMain {

  private static final Logger logger = Logger.getLogger(AgentMain.class.getName());

  private AgentMain() {
  }

  /**
   * Initializes the OpenCensus Agent for Java.
   *
   * @param agentArgs agent options, passed as a single string by the JVM
   * @param inst      the {@link Instrumentation} object provided by the JVM for instrumenting Java
   *                  programming language code
   * @throws Exception if initialization of the agent fails
   *
   * @see java.lang.instrument
   */
  public static void premain(String agentArgs, Instrumentation inst) throws Exception {
    logger.info("Initializing.");

    // The classes in bootstrap.jar will be referenced from classes loaded by the bootstrap
    // classloader. Thus, these classes have to be loaded by the bootstrap classloader, too.
    inst.appendToBootstrapClassLoaderSearch(
            new JarFile(Resources.getResourceAsTempFile("bootstrap.jar")));

    AgentBuilder agentBuilder = new AgentBuilder.Default()
            .disableClassFormatChanges()
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(new AgentBuilderListener())
            .ignore(none());
    // TODO(stschmidt): Add instrumentation for context propagation.
    agentBuilder.installOn(inst);

    logger.info("Initialized.");
  }
}
