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

package io.opencensus.exporter.stats.signalfx;

import io.opencensus.common.Duration;
import java.util.Properties;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SignalFxStatsExporter}. */
@RunWith(JUnit4.class)
public class SignalFxStatsExporterTest {

  private static final String TEST_TOKEN = "token";
  private static final String TEST_ENDPOINT = "https://example.com";
  private static final Duration ONE_SECOND = Duration.create(1, 0);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @After
  public void tearDown() {
    System.getProperties().remove(SignalFxStatsExporter.SIGNALFX_HOST_PROPERTY);
    System.getProperties().remove(SignalFxStatsExporter.SIGNALFX_TOKEN_PROPERTY);
    SignalFxStatsExporter.unsafeResetExporter();
  }

  @Test
  public void createWithNullTokenFromSystemProperties() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid SignalFx token");
    SignalFxStatsExporter.create();
  }

  @Test
  public void createWithNullTokenFromGivenProperties() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid SignalFx token");
    SignalFxStatsExporter.create(new Properties());
  }

  @Test
  public void createWithTokenFromSystemProperties() {
    System.setProperty(SignalFxStatsExporter.SIGNALFX_TOKEN_PROPERTY, TEST_TOKEN);
    SignalFxStatsExporter.create();
  }

  @Test
  public void createWithTokenFromGivenProperties() {
    Properties props = new Properties();
    props.put(SignalFxStatsExporter.SIGNALFX_TOKEN_PROPERTY, TEST_TOKEN);
    SignalFxStatsExporter.create(props);
  }

  @Test
  public void createWithNullToken() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid SignalFx token");
    SignalFxStatsExporter.create(TEST_ENDPOINT, null, ONE_SECOND);
  }

  @Test
  public void createWithNullHost() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid SignalFx endpoint URL");
    SignalFxStatsExporter.create(null, TEST_TOKEN, ONE_SECOND);
  }

  @Test
  public void createWithNegativeDuration() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Duration must be positive");
    SignalFxStatsExporter.create(TEST_ENDPOINT, TEST_TOKEN, Duration.create(-1, 0));
  }

  @Test
  public void createExporterTwice() {
    SignalFxStatsExporter.create(TEST_ENDPOINT, TEST_TOKEN, ONE_SECOND);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("SignalFx stats exporter is already created.");
    SignalFxStatsExporter.create(TEST_ENDPOINT, TEST_TOKEN, ONE_SECOND);
  }
}
