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

import com.google.auto.service.AutoService;
import io.opencensus.contrib.agent.Settings;
import io.opencensus.contrib.agent.bootstrap.TraceStrategy;
import io.opencensus.contrib.agent.bootstrap.TraceTrampoline;
import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * Initializes the {@link TraceTrampoline} with a concrete {@link TraceStrategy}.
 *
 * @since 0.9
 */
@AutoService(Instrumenter.class)
public final class TraceTrampolineInitializer implements Instrumenter {

  @Override
  public AgentBuilder instrument(AgentBuilder agentBuilder, Settings settings) {
    // TODO(stschmidt): Gracefully handle the case of missing trace API at runtime,
    // maybe load the missing classes from a JAR that comes with the agent JAR.
    TraceTrampoline.setTraceStrategy(new TraceStrategyImpl());

    return agentBuilder;
  }
}
