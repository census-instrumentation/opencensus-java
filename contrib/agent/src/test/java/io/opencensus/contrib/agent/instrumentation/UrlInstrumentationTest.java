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

import static com.google.common.truth.Truth.assertThat;

import com.typesafe.config.ConfigFactory;
import io.opencensus.contrib.agent.Settings;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link UrlInstrumentation}. */
@RunWith(MockitoJUnitRunner.class)
public class UrlInstrumentationTest {

  private final UrlInstrumentation instrumentation = new UrlInstrumentation();

  private final AgentBuilder agentBuilder = new AgentBuilder.Default();

  private static final String FEATURE = "trace.java.net.URL.getContent";

  @Test
  public void instrument_disabled() {
    Settings settings = new Settings(ConfigFactory.parseString(FEATURE + ".enabled = false"));

    AgentBuilder agentBuilder2 = instrumentation.instrument(agentBuilder, settings);

    assertThat(agentBuilder2).isSameAs(agentBuilder);
  }

  @Test
  public void instrument_enabled() {
    Settings settings = new Settings(ConfigFactory.parseString(FEATURE + ".enabled = true"));

    AgentBuilder agentBuilder2 = instrumentation.instrument(agentBuilder, settings);

    assertThat(agentBuilder2).isNotSameAs(agentBuilder);
  }
}
