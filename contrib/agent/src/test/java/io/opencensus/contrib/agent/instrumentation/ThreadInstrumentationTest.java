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
import static org.mockito.Mockito.when;

import com.typesafe.config.Config;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link ThreadInstrumentation}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ThreadInstrumentationTest {

  @Mock
  private Config config;

  private final ThreadInstrumentation instrumentation = new ThreadInstrumentation();

  private final AgentBuilder agentBuilder = new AgentBuilder.Default();

  @Test
  public void instrument_disabled() {
    when(config.getBoolean("context-propagation.thread")).thenReturn(false);

    AgentBuilder agentBuilder2 = instrumentation.instrument(agentBuilder, config);

    assertThat(agentBuilder2).isSameAs(agentBuilder);
  }

  @Test
  public void instrument_enabled() {
    when(config.getBoolean("context-propagation.thread")).thenReturn(true);

    AgentBuilder agentBuilder2 = instrumentation.instrument(agentBuilder, config);

    assertThat(agentBuilder2).isNotSameAs(agentBuilder);
  }
}
