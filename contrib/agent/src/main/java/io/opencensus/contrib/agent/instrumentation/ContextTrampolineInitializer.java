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
import com.typesafe.config.Config;
import io.opencensus.contrib.agent.bootstrap.ContextStrategy;
import io.opencensus.contrib.agent.bootstrap.ContextTrampoline;
import net.bytebuddy.agent.builder.AgentBuilder;

/** Initializes the {@link ContextTrampoline} with a concrete {@link ContextStrategy}. */
@AutoService(Instrumenter.class)
public final class ContextTrampolineInitializer implements Instrumenter {

  @Override
  public AgentBuilder instrument(AgentBuilder agentBuilder, Config config) {
    // TODO(stschmidt): Gracefully handle the case of missing io.grpc.Context at runtime,
    // maybe load the missing classes from a JAR that comes with the agent JAR.
    ContextTrampoline.setContextStrategy(new ContextStrategyImpl());

    return agentBuilder;
  }
}
